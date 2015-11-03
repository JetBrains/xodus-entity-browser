package com.lehvolk.xodus.repo;

import java.util.function.Function;

import com.lehvolk.xodus.dto.EntityTypeVO;
import com.lehvolk.xodus.dto.EntityVO;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;

/**
 * Transformations between VO and DataBase objects
 * @author Alexey Volkov
 * @since 02.11.15
 */
public class Transformations {

    private Transformations() {
    }

    public static Function<Entity, EntityVO> entity(final PersistentEntityStoreImpl store,
            final PersistentStoreTransaction tx) {
        return entity -> {
            EntityVO vo = new EntityVO();

            return vo;
        };
    }

    public static Function<String, EntityTypeVO> entityType(final PersistentEntityStoreImpl store,
            final PersistentStoreTransaction tx) {
        return entityType -> {
            EntityTypeVO vo = new EntityTypeVO();
            int typeId = store.getEntityTypeId(tx, entityType, false);
            vo.setName(entityType);
            vo.setId(typeId);
            return vo;
        };
    }

}
