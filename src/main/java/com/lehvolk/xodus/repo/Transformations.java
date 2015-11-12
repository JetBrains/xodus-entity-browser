package com.lehvolk.xodus.repo;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.lehvolk.xodus.exceptions.InvalidFieldException;
import com.lehvolk.xodus.repo.UIPropertyTypes.UIPropertyType;
import com.lehvolk.xodus.vo.EntityTypeVO;
import com.lehvolk.xodus.vo.EntityVO;
import com.lehvolk.xodus.vo.EntityVO.BlobPropertyVO;
import com.lehvolk.xodus.vo.EntityVO.LinkPropertyVO;
import com.lehvolk.xodus.vo.LightEntityVO;
import com.lehvolk.xodus.vo.LightEntityVO.BasePropertyVO;
import com.lehvolk.xodus.vo.LightEntityVO.EntityPropertyTypeVO;
import com.lehvolk.xodus.vo.LightEntityVO.EntityPropertyVO;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.entitystore.tables.PropertyType;
import static com.lehvolk.xodus.repo.UIPropertyTypes.uiTypeOf;
import static java.util.stream.Collectors.toList;

/**
 * Transformations between VO and DataBase objects
 * @author Alexey Volkov
 * @since 02.11.15
 */
@Singleton
public class Transformations {

    @Inject
    private PresentationService presentations;

    @NotNull
    public Function<Entity, EntityVO> entity(final PersistentEntityStoreImpl store,
            PersistentStoreTransaction t) {
        return entity -> {
            EntityVO vo = getEntityVO(EntityVO::new, store, t, entity);
            List<BlobPropertyVO> blobs = entity.getBlobNames().stream()
                    .map(blob(entity))
                    .collect(toList());
            List<LinkPropertyVO> links = entity.getLinkNames().stream()
                    .map(link(store, t, entity))
                    .collect(toList());
            vo.setBlobs(blobs);
            vo.setLinks(links);
            return vo;
        };
    }

    @NotNull
    public Function<Entity, LightEntityVO> lightEntity(final PersistentEntityStoreImpl store,
            PersistentStoreTransaction t) {
        return entity -> getEntityVO(LightEntityVO::new, store, t, entity);

    }

    @NotNull
    private <T extends LightEntityVO> T getEntityVO(Supplier<T> supplier, PersistentEntityStoreImpl store,
            PersistentStoreTransaction
                    t, Entity
            entity) {
        List<EntityPropertyVO> properties = entity.getPropertyNames().stream()
                .map(property(store, entity))
                .collect(toList());
        T vo = supplier.get();
        vo.setId(String.valueOf(entity.getId().getLocalId()));
        vo.setProperties(properties);
        int typeId = entity.getId().getTypeId();
        String entityType = store.getEntityType(t, typeId);
        presentations.presentation(typeId, entityType).apply(vo);
        vo.setTypeId(String.valueOf(typeId));
        vo.setType(entityType);
        return vo;
    }

    @NotNull
    public <T extends LightEntityVO> Function<Entity, T> entity(final PersistentEntityStoreImpl store,
            PersistentStoreTransaction t, Supplier<T> supplier) {
        return entity -> {
            List<EntityPropertyVO> properties = entity.getPropertyNames().stream()
                    .map(property(store, entity))
                    .collect(toList());

            T vo = supplier.get();
            vo.setId(String.valueOf(entity.getId().getLocalId()));
            vo.setProperties(properties);
            int typeId = entity.getId().getTypeId();
            String entityType = store.getEntityType(t, typeId);
            presentations.presentation(typeId, entityType).apply(vo);
            vo.setTypeId(String.valueOf(typeId));
            vo.setType(entityType);
            return vo;
        };
    }

    @NotNull
    private Function<String, LinkPropertyVO> link(PersistentEntityStoreImpl store, PersistentStoreTransaction t, Entity
            entity) {
        return name -> {
            LinkPropertyVO vo = newProperty(new LinkPropertyVO(), name);
            Entity link = entity.getLink(name);
            if (link != null) {
                LightEntityVO lightVO = lightEntity(store, t).apply(link);
                EntityId linkId = link.getId();
                vo.setTypeId(linkId.getTypeId());
                vo.setEntityId(linkId.getLocalId());
                vo.setLabel(lightVO.getLabel());
                vo.setType(lightVO.getType());
            }
            return vo;
        };
    }

    @NotNull
    private Function<String, BlobPropertyVO> blob(Entity entity) {
        return name -> {
            BlobPropertyVO vo = newProperty(new BlobPropertyVO(), name);
            vo.setBlobSize(entity.getBlobSize(name));
            return vo;
        };
    }

    @NotNull
    private Function<String, EntityPropertyVO> property(PersistentEntityStoreImpl store, Entity entity) {
        return name -> {
            EntityPropertyVO vo = newProperty(new EntityPropertyVO(), name);
            Comparable<?> value = entity.getProperty(name);
            EntityPropertyTypeVO typeVO = new EntityPropertyTypeVO();
            Class<?> clazz = String.class;
            if (value != null) {
                PropertyType propertyType = store.getPropertyTypes().getPropertyType(value.getClass());
                clazz = propertyType.getClazz();
                vo.setValue(value2string(value));
            }
            typeVO.setReadonly(!UIPropertyTypes.isSupported(clazz));
            typeVO.setClazz(clazz.getName());
            typeVO.setDisplayName(clazz.getSimpleName());
            vo.setType(typeVO);
            return vo;
        };
    }

    public Function<String, EntityTypeVO> entityType(final PersistentEntityStoreImpl store,
            final PersistentStoreTransaction tx) {
        return entityType -> {
            EntityTypeVO vo = new EntityTypeVO();
            int typeId = store.getEntityTypeId(tx, entityType, false);
            vo.setName(entityType);
            vo.setId(String.valueOf(typeId));
            return vo;
        };
    }

    @Nullable
    public Comparable<?> string2value(EntityPropertyVO propertyVO) {
        if (propertyVO.getValue() == null) {
            return null;
        }
        try {
            String clazz = propertyVO.getType().getClazz();
            UIPropertyType<Comparable<?>> type = uiTypeOf(clazz);
            return type == null ? null : type.toValue(propertyVO.getValue());
        } catch (RuntimeException e) {
            throw new InvalidFieldException(e, propertyVO.getName(), propertyVO.getValue());
        }
    }

    @Nullable
    public <T extends Comparable<?>> String value2string(T value) {
        if (value == null) {
            return null;
        }
        try {
            Class<?> clazz = value.getClass();
            UIPropertyType<T> type = uiTypeOf(clazz);
            return type == null ? null : type.toString(value);
        } catch (RuntimeException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T extends BasePropertyVO> T newProperty(T t, String name) {
        t.setName(name);
        return t;
    }
}
