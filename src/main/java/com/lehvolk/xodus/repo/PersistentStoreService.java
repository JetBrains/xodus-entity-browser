package com.lehvolk.xodus.repo;

import java.util.function.Function;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.lehvolk.xodus.dto.EntityPresentationVO;
import com.lehvolk.xodus.dto.EntityTypeVO;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.env.Environments;
import lombok.extern.slf4j.Slf4j;
import static com.lehvolk.xodus.repo.Transformations.entityType;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class PersistentStoreService {

    private PersistentEntityStoreImpl current;

    @Inject
    private PresentationService presentations;

    @PostConstruct
    public void construct() {
        current = PersistentEntityStores.newInstance(Environments.newInstance("d:\\data\\big-data\\"),
                "jetPassServerDb");
    }

    public EntityTypeVO[] getTypes() {
        return callStore(
                tx -> tx.getEntityTypes().stream()
                        .map(entityType(current, tx))
                        .toArray(EntityTypeVO[]::new)
        );
    }

    @PreDestroy
    public void destroy() {
        try {
            current.close();
        } catch (RuntimeException e) {
            log.error("error closing persistent store", e);
        }
    }

    public EntityPresentationVO[] searchType(long typeId, String term, int offset, int pageSize) {
        return callStore(
                t -> {
                    String type = current.getEntityType(t, (int) typeId);
                    EntityIterable result = null;
                    if ((term == null) || term.trim().isEmpty()) {
                        result = t.getAll(type);
                    } else {
                        if (term.matches("\\d*")) {
                            EntityId id = new PersistentEntityId((int) typeId, Long.valueOf(term));
                            result = t.getSingletonIterable(t.getEntity(id));
                        } else {
                            //                            List<SearchItem> items = SmartSearchQueryToolkit.parse(term);
                        }
                    }
                    return stream(result.skip(offset).take(pageSize).spliterator(), false)
                            .map(presentations.presentation(typeId, type))
                            .toArray(EntityPresentationVO[]::new);
                }

        );
    }

    private <T> T callStore(Function<PersistentStoreTransaction, T> call) {
        PersistentStoreTransaction tx = current.beginReadonlyTransaction();
        try {
            return call.apply(tx);
        } finally {
            tx.commit();
        }
    }
}
