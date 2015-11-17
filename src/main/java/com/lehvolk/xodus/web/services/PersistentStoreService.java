package com.lehvolk.xodus.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lehvolk.xodus.web.exceptions.EntityNotFoundException;
import com.lehvolk.xodus.web.vo.ChangeSummaryVO;
import com.lehvolk.xodus.web.vo.EntityTypeVO;
import com.lehvolk.xodus.web.vo.EntityVO;
import com.lehvolk.xodus.web.vo.LightEntityVO.BasePropertyVO;
import com.lehvolk.xodus.web.vo.LightEntityVO.EntityPropertyVO;
import com.lehvolk.xodus.web.vo.SearchPagerVO;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntity;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.env.Environments;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.io.IOUtils.copy;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class PersistentStoreService {

    private PersistentEntityStoreImpl store;

    @Inject
    private Transformations transformations;

    public void construct() {
        try {
            XodusStoreRequisites requisites = XodusStoreRequisites.get();
            store = PersistentEntityStores.newInstance(
                    Environments.newInstance(requisites.getLocation()), requisites.getKey());
        } catch (RuntimeException e) {
            String msg = "Can't get valid Xodus entity store location and store key. Check the configuration";
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    public void destroy() {
        boolean proceed = true;
        int count = 0;
        while (proceed && count < 10) {
            try {
                log.info("trying to close persistent store. attempt {}", count);
                store.close();
                proceed = false;
                log.info("persistent store closed");
            } catch (RuntimeException e) {
                log.error("error closing persistent store", e);
                count++;
            }
        }
    }

    public EntityTypeVO[] getTypes() {
        return callStore(
                tx -> tx.getEntityTypes().stream()
                        .map(transformations.entityType(store, tx))
                        .toArray(EntityTypeVO[]::new)
        );
    }

    public SearchPagerVO searchType(int typeId, String term, int offset, int pageSize) {
        return callStore(
                t -> {
                    String type = store.getEntityType(t, typeId);
                    EntityIterable result = SmartSearchToolkit.doSmartSearch(term, type, typeId, t);
                    long totalCount = result.size();
                    EntityVO[] items = stream(result.skip(offset).take(pageSize).spliterator(), false)
                            .map(transformations.entity(store, t))
                            .toArray(EntityVO[]::new);
                    return new SearchPagerVO(items, totalCount);
                }
        );
    }

    public EntityVO newEntity(int typeId, ChangeSummaryVO vo) {
        long entityId = modifyStore(t -> {
            String type = store.getEntityType(t, typeId);
            PersistentEntity entity = t.newEntity(type);
            vo.getProperties().getAdded().stream()
                    .forEach(applyValues(entity));
            vo.getLinks().getAdded().stream()
                    .forEach(property -> {
                        Entity link = getEntity(property.getTypeId(), property.getEntityId(), t);
                        entity.addLink(property.getName(), link);
                    });
            return entity.getId().getLocalId();
        });
        return getEntity(typeId, entityId);
    }

    public void getBlob(int typeId, long entityId, String blobName, OutputStream out) throws IOException {
        PersistentStoreTransaction tx = store.beginReadonlyTransaction();
        try {
            Entity entity = getEntity(typeId, entityId, tx);
            InputStream blob = entity.getBlob(blobName);
            if (blob != null) {
                copy(blob, out);
            }
        } finally {
            tx.commit();
        }
    }

    @NotNull
    private Consumer<EntityPropertyVO> applyValues(PersistentEntity entity) {
        return property -> {
            property.setValue(safeTrim(property.getValue()));
            Comparable<?> value = transformations.string2value(property);
            if (value != null) {
                entity.setProperty(property.getName(), value);
            }
        };
    }

    public EntityVO updateEntity(int typeId, long entityId, ChangeSummaryVO vo) {
        long localId = modifyStore(t -> {
            PersistentEntity entity = getEntity(typeId, entityId, t);
            vo.getProperties().getDeleted().stream()
                    .filter(inProperties(entity))
                    .map(BasePropertyVO::getName)
                    .forEach(entity::deleteProperty);

            vo.getProperties().getAdded().stream()
                    .filter(notInProperties(entity))
                    .forEach(applyValues(entity));

            vo.getProperties().getModified().stream()
                    .filter(inProperties(entity))
                    .forEach(applyValues(entity));

            List<String> links = entity.getLinkNames();
            vo.getLinks().getDeleted().stream()
                    .filter(link -> links.contains(link.getName()))
                    .forEach(link -> {
                        Entity linked = getEntity(link.getTypeId(), link.getEntityId(), t);
                        entity.deleteLink(link.getName(), linked);
                    });
            vo.getLinks().getAdded().stream()
                    .filter(linkVO -> !links.contains(linkVO.getName()))
                    .forEach(
                            value -> {
                                PersistentEntityId id = new PersistentEntityId(value.getTypeId(), value.getEntityId());
                                Entity link = t.getEntity(id);
                                entity.addLink(value.getName(), link);
                            });
            return entityId;
        });
        return getEntity(typeId, localId);
    }

    public EntityVO getEntity(int typeId, long entityId) {
        return callStore(
                t -> {
                    Entity entity = getEntity(typeId, entityId, t);
                    return transformations.entity(store, t).apply(entity);
                }
        );
    }

    public void deleteEntity(int id, long entityId) {
        modifyStore(t -> {
            Entity entity = getEntity(id, entityId, t);
            entity.delete();
            return null;
        });
    }

    private PersistentEntity getEntity(int typeId, long entityId, PersistentStoreTransaction t) {
        try {
            return t.getEntity(new PersistentEntityId(typeId, entityId));
        } catch (RuntimeException e) {
            log.error("entity not found by type '" + typeId + "' and entityId '" + entityId + "'", e);
            throw new EntityNotFoundException(e, typeId, entityId);
        }
    }

    private <T> T callStore(Function<PersistentStoreTransaction, T> call) {
        PersistentStoreTransaction tx = store.beginReadonlyTransaction();
        try {
            return call.apply(tx);
        } finally {
            tx.commit();
        }
    }

    private <T> T modifyStore(Function<PersistentStoreTransaction, T> call) {
        PersistentStoreTransaction tx = store.beginTransaction();
        try {
            return call.apply(tx);
        } catch (RuntimeException e) {
            tx.revert();
            throw e;
        } finally {
            tx.commit();
        }
    }

    @Nullable
    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T extends BasePropertyVO> Predicate<T> inProperties(Entity entity) {
        List<String> names = entity.getPropertyNames();
        return property -> names.contains(property.getName());
    }

    private <T extends BasePropertyVO> Predicate<T> notInProperties(Entity entity) {
        List<String> names = entity.getPropertyNames();
        return property -> !names.contains(property.getName());
    }

}
