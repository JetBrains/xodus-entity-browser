package com.lehvolk.xodus.repo;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.lehvolk.xodus.dto.EntityPresentationVO;
import com.lehvolk.xodus.dto.EntityTypeVO;
import com.lehvolk.xodus.dto.EntityVO;
import com.lehvolk.xodus.dto.EntityVO.BasePropertyVO;
import com.lehvolk.xodus.dto.EntityVO.BlobPropertyVO;
import com.lehvolk.xodus.dto.EntityVO.EntityPropertyVO;
import com.lehvolk.xodus.dto.EntityVO.InputType;
import com.lehvolk.xodus.dto.EntityVO.LinkPropertyVO;
import com.lehvolk.xodus.dto.SearchPagerVO;
import com.lehvolk.xodus.repo.SearchTerm.Range;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.entitystore.tables.PropertyType;
import jetbrains.exodus.env.Environments;
import lombok.extern.slf4j.Slf4j;
import static com.lehvolk.xodus.repo.Transformations.entityType;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class PersistentStoreService {

    private static final Pattern ID_PATTERN = Pattern.compile("\\d*");
    private PersistentEntityStoreImpl store;

    @Inject
    private PresentationService presentations;

    @PostConstruct
    public void construct() {
        store = PersistentEntityStores.newInstance(Environments.newInstance("d:\\data\\big-data\\"),
                "jetPassServerDb");
    }

    public EntityTypeVO[] getTypes() {
        return callStore(
                tx -> tx.getEntityTypes().stream()
                        .map(entityType(store, tx))
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
                    EntityIterable result = null;
                    if ((term == null) || term.trim().isEmpty()) {
                        result = t.getAll(type);
                    } else {
                        if (ID_PATTERN.matcher(term).matches()) {
                            EntityId id = new PersistentEntityId(typeId, Long.valueOf(term));
                            result = t.getSingletonIterable(t.getEntity(id));
                        } else {
                            result = SmartSearchQueryToolkit.parse(term).stream().map(item -> {
                                switch (item.getType()) {
                                    case RANGE:
                                        Range range = ((Range) item.getValue());
                                        return t.find(type, item.getProperty(), range.getStart(), range.getEnd());
                                    case LIKE:
                                        return t.findStartingWith(type, item.getProperty(),
                                                String.valueOf(item.getValue()));
                                    default:
                                        return t.find(type, item.getProperty(), String.valueOf(item.getValue()));
                                }
                            }).reduce(EntityIterable::intersect).get();
                        }
                    }
                    long totalCount = result.size();
                    EntityPresentationVO[] items = stream(result.skip(offset).take(pageSize).spliterator(), false)
                            .map(presentations.presentation(typeId, type))
                            .toArray(EntityPresentationVO[]::new);
                    return new SearchPagerVO(items, totalCount);
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

    public EntityVO getEntity(int typeId, long entityId) {
        return callStore(
                t -> {
                    Entity entity = t.getEntity(new PersistentEntityId(typeId, entityId));
                    List<EntityPropertyVO> properties = entity.getPropertyNames().stream().map(name -> {
                        EntityPropertyVO vo = newProperty(new EntityPropertyVO(), name);
                        Comparable<?> value = entity.getProperty(name);
                        vo.setValue((Serializable) value);
                        if (value != null) {
                            PropertyType propertyType = store.getPropertyTypes().getPropertyType(value.getClass());
                            vo.setClazz(propertyType.getClazz().getName());
                            vo.setType(InputType.STRING);
                        }
                        return vo;
                    }).collect(toList());
                    List<BlobPropertyVO> blobs = entity.getBlobNames().stream().map(name -> {
                        BlobPropertyVO vo = newProperty(new BlobPropertyVO(), name);
                        vo.setBlobSize(entity.getBlobSize(name));
                        return vo;
                    }).collect(toList());

                    List<LinkPropertyVO> links = entity.getLinkNames().stream().map(name -> {
                        LinkPropertyVO vo = newProperty(new LinkPropertyVO(), name);
                        Entity link = entity.getLink(name);
                        if (link != null) {
                            EntityId linkId = link.getId();
                            vo.setTypeId(linkId.getTypeId());
                            vo.setEntityId(linkId.getLocalId());
                        }
                        return vo;
                    }).collect(toList());

                    EntityVO vo = new EntityVO();
                    vo.setId(entity.getId().getLocalId());
                    vo.setTypeId(typeId);
                    vo.setType(store.getEntityType(t, typeId));
                    vo.setValues(properties);
                    vo.setBlobs(blobs);
                    vo.setLinks(links);
                    return vo;
                }
        );
    }

    private <T extends BasePropertyVO> T newProperty(T t, String name) {
        t.setName(name);
        return t;
    }
}
