package com.lehvolk.xodus.repo;

import java.util.List;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jetbrains.annotations.Nullable;

import com.lehvolk.xodus.vo.ChangeSummaryVO;
import com.lehvolk.xodus.vo.EntityTypeVO;
import com.lehvolk.xodus.vo.EntityVO;
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
        try {
            store.close();
        } catch (RuntimeException e) {
            log.error("error closing persistent store", e);
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
        return modifyStore(t -> {
            String type = store.getEntityType(t, typeId);
            PersistentEntity entity = t.newEntity(type);
            vo.getProperties().getAdded().stream()
                    .forEach(property -> {
                        property.setValue(safeTrim(property.getValue()));
                        Comparable<?> value = transformations.string2value(property);
                        if (value != null) {
                            entity.setProperty(property.getName(), value);
                        }
                    });
            vo.getLinks().getAdded().stream()
                    .forEach(property -> {
                        Entity link = t.getEntity(
                                new PersistentEntityId(property.getTypeId(), property.getEntityId()));
                        entity.addLink(property.getName(), link);
                    });
            return new EntityVO();
        });
    }

    public EntityVO updateEntity(int typeId, long entityId, ChangeSummaryVO vo) {
        return modifyStore(t -> {
            PersistentEntity entity = t.getEntity(new PersistentEntityId(typeId, entityId));
            List<String> properties = entity.getPropertyNames();
            vo.getProperties().getDeleted().stream()
                    .filter(propertyVO -> properties.contains(propertyVO.getName()))
                    .forEach(property -> entity.deleteProperty(property.getName()));
            vo.getProperties().getAdded().stream()
                    .filter(propertyVO -> !properties.contains(propertyVO.getName()))
                    .forEach(
                            property -> {
                                property.setValue(safeTrim(property.getValue()));
                                Comparable<?> value = transformations.string2value(property);
                                if (value != null) {
                                    entity.setProperty(property.getName(), value);
                                }
                            });
            vo.getProperties().getModified().stream()
                    .filter(propertyVO -> properties.contains(propertyVO.getName()))
                    .forEach(
                            property -> {
                                property.setValue(safeTrim(property.getValue()));
                                Comparable<?> value = transformations.string2value(property);
                                if (value != null) {
                                    entity.setProperty(property.getName(), value);
                                }
                            });
            List<String> links = entity.getLinkNames();
            vo.getLinks().getDeleted().stream()
                    .filter(linkVO -> links.contains(linkVO.getName()))
                    .forEach(
                            link -> entity.deleteProperty(link.getName())
                    );
            vo.getLinks().getAdded().stream()
                    .filter(linkVO -> !links.contains(linkVO.getName()))
                    .forEach(
                            value -> {
                                PersistentEntityId id = new PersistentEntityId(value.getTypeId(), value.getEntityId());
                                Entity link = t.getEntity(id);
                                entity.addLink(value.getName(), link);
                            });
            return new EntityVO();
        });
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
        } finally {
            tx.commit();
        }
    }

    public EntityVO getEntity(int typeId, long entityId) {
        return callStore(
                t -> {
                    Entity entity = t.getEntity(new PersistentEntityId(typeId, entityId));
                    return transformations.entity(store, t).apply(entity);
                }
        );
    }

    @Nullable
    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
