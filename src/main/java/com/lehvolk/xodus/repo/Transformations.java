package com.lehvolk.xodus.repo;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lehvolk.xodus.vo.EntityTypeVO;
import com.lehvolk.xodus.vo.EntityVO;
import com.lehvolk.xodus.vo.EntityVO.BasePropertyVO;
import com.lehvolk.xodus.vo.EntityVO.BlobPropertyVO;
import com.lehvolk.xodus.vo.EntityVO.EntityPropertyTypeVO;
import com.lehvolk.xodus.vo.EntityVO.EntityPropertyVO;
import com.lehvolk.xodus.vo.EntityVO.LinkPropertyVO;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.entitystore.tables.PropertyType;
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

    private final ObjectMapper mapper = new ObjectMapper();

    // avoiding leak. using cache for Class.forName
    private Cache<String, Class<?>> classCache = CacheBuilder.newBuilder().maximumSize(100).build();

    @PostConstruct
    public void construct() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @NotNull
    public Function<Entity, EntityVO> entity(final PersistentEntityStoreImpl store,
            PersistentStoreTransaction t) {
        return entity -> {
            List<EntityPropertyVO> properties = entity.getPropertyNames().stream().map(name -> {
                EntityPropertyVO vo = newProperty(new EntityPropertyVO(), name);
                Comparable<?> value = entity.getProperty(name);
                EntityPropertyTypeVO typeVO = new EntityPropertyTypeVO();
                Class<?> clazz = String.class;
                if (value != null) {
                    PropertyType propertyType = store.getPropertyTypes().getPropertyType(value.getClass());
                    clazz = propertyType.getClazz();
                    vo.setValue(value2string(value));
                }
                typeVO.setClazz(clazz.getName());
                typeVO.setDisplayName(clazz.getSimpleName());
                vo.setType(typeVO);
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
            vo.setProperties(properties);
            vo.setBlobs(blobs);
            vo.setLinks(links);
            int typeId = entity.getId().getTypeId();
            String entityType = store.getEntityType(t, typeId);
            presentations.presentation(typeId, entityType).apply(vo);
            vo.setTypeId(typeId);
            vo.setType(entityType);
            return vo;
        };
    }

    public Function<String, EntityTypeVO> entityType(final PersistentEntityStoreImpl store,
            final PersistentStoreTransaction tx) {
        return entityType -> {
            EntityTypeVO vo = new EntityTypeVO();
            int typeId = store.getEntityTypeId(tx, entityType, false);
            vo.setName(entityType);
            vo.setId(typeId);
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
            Class<?> type = classCache.getIfPresent(clazz);
            if (type == null) {
                type = Class.forName(clazz);
                classCache.put(clazz, type);
            }
            return (Comparable<?>) mapper.readValue(propertyVO.getValue(), type);
        } catch (ClassNotFoundException | IOException e) {
            throw new InvalidFieldException(e, propertyVO.getName(), propertyVO.getValue());
        }
    }

    @Nullable
    public String value2string(Comparable<?> value) {
        if (value == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T extends BasePropertyVO> T newProperty(T t, String name) {
        t.setName(name);
        return t;
    }
}
