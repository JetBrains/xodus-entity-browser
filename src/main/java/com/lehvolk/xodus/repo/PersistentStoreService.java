package com.lehvolk.xodus.repo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lehvolk.xodus.vo.ChangeSummaryVO;
import com.lehvolk.xodus.vo.EntityTypeVO;
import com.lehvolk.xodus.vo.EntityVO;
import com.lehvolk.xodus.vo.LightEntityVO.BasePropertyVO;
import com.lehvolk.xodus.vo.LightEntityVO.EntityPropertyVO;
import com.lehvolk.xodus.vo.SearchPagerVO;
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

    @PostConstruct
    public void construct() {
        store = PersistentEntityStores.newInstance(Environments.newInstance("d:\\data\\big-data\\"),
                "jetPassServerDb");
    }

    public EntityTypeVO[] getTypes() {
        return callStore(
                tx -> tx.getEntityTypes().stream()
                        .map(transformations.entityType(store, tx))
                        .toArray(EntityTypeVO[]::new)
        );
    }

    @PreDestroy
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
                        Entity link = t.getEntity(
                                new PersistentEntityId(property.getTypeId(), property.getEntityId()));
                        entity.addLink(property.getName(), link);
                    });
            return entity.getId().getLocalId();
        });
        return getEntity(typeId, entityId);
    }

    public void getBlob(int typeId, long entityId, String blobName, OutputStream out) throws IOException {
        PersistentStoreTransaction tx = store.beginReadonlyTransaction();
        try {
            Entity entity = tx.getEntity(new PersistentEntityId(typeId, entityId));
            InputStream blob = entity.getBlob(blobName);
            if (blob != null) {
                IOUtils.copy(blob, out);
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
            PersistentEntity entity = t.getEntity(new PersistentEntityId(typeId, entityId));
            List<String> properties = entity.getPropertyNames();
            vo.getProperties().getDeleted().stream()
                    .map(BasePropertyVO::getName)
                    .filter(properties::contains)
                    .forEach(entity::deleteProperty);

            vo.getProperties().getAdded().stream()
                    .filter(propertyVO -> !properties.contains(propertyVO.getName()))
                    .forEach(applyValues(entity));

            vo.getProperties().getModified().stream()
                    .filter(propertyVO -> properties.contains(propertyVO.getName()))
                    .forEach(applyValues(entity));

            List<String> links = entity.getLinkNames();
            vo.getLinks().getDeleted().stream()
                    .filter(link -> links.contains(link.getName()))
                    .forEach(link -> {
                        Entity linked = t.getEntity(new PersistentEntityId(link.getTypeId(), link.getEntityId()));
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
                    Entity entity = t.getEntity(new PersistentEntityId(typeId, entityId));
                    return transformations.entity(store, t).apply(entity);
                }
        );
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
    //
    //    public static void main(String[] args) {
    //        PersistentStoreService service = new PersistentStoreService();
    //        service.construct();
    //
    //        service.modifyStore(t -> {
    //            PersistentEntity entity = t.getEntity(new PersistentEntityId(0, 1));
    //            entity.setBlob("file", new File("d:\\informatica_inspections.xml"));
    //            return null;
    //        });
    //        service.destroy();
    //    }

}
