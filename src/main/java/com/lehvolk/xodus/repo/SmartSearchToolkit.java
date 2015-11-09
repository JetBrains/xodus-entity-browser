package com.lehvolk.xodus.repo;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.lehvolk.xodus.repo.SearchTerm.Range;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.iterate.EntityIterableBase;


/**
 * Toolkit for Smart Search
 * @author Alexey Volkov
 * @since 02.11.15
 */
public class SmartSearchToolkit {

    private static final Pattern INTEGER = Pattern.compile("\\d*");

    private SmartSearchToolkit() {
    }

    /**
     * @param query query to parse
     * @return parser instance
     */
    public static SmartSearchQueryParser newParser(String query) {
        try {
            return new SmartSearchQueryParser(new ByteArrayInputStream(query.getBytes()));
        } catch (RuntimeException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<SearchTerm<?>> parse(String query) {
        try {
            return newParser(query).parse();
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static EntityIterable doSmartSearch(String term, String type, int typeId, StoreTransaction t) {
        EntityIterable result;
        if ((term == null) || term.trim().isEmpty()) {
            result = t.getAll(type);
        } else {
            EntityId entityId = toEntityId(typeId, term);
            if (entityId != null) {
                result = t.getSingletonIterable(t.getEntity(entityId));
            } else {
                try {
                    result = searchByTerms(term, type, typeId, t);
                } catch (RuntimeException e) {
                    result = EntityIterableBase.EMPTY;
                }
            }
        }
        return result;
    }

    private static EntityIterable searchByTerms(String term, String type, int typeId, StoreTransaction t) {
        Stream<SearchTerm<?>> termStream = parse(term).stream();
        return termStream.map(item -> {
            EntityIterable itemResult = null;
            boolean isById = "id".equalsIgnoreCase(item.getProperty());
            switch (item.getType()) {
                case RANGE:
                    Range range = ((Range) item.getValue());
                    itemResult = t.find(type, item.getProperty(), range.getStart(), range.getEnd());
                    if (isById) {
                        itemResult = itemResult.union(t.findIds(type, range.getStart(), range.getEnd()));
                    }
                    break;
                case LIKE:
                    itemResult = t.findStartingWith(type, item.getProperty(), String.valueOf(item.getValue()));
                    break;
                default:
                    String value = String.valueOf(item.getValue());
                    itemResult = t.find(type, item.getProperty(), value);
                    if (isById) {
                        EntityId byId = toEntityId(typeId, value);
                        if (byId != null) {
                            itemResult = itemResult.union(t.getSingletonIterable(t.getEntity(byId)));
                        }
                    }
            }
            return itemResult;
        }).reduce(EntityIterable::intersect).get();
    }

    private static EntityId toEntityId(int typeId, String value) {
        if (INTEGER.matcher(value).matches()) {
            return new PersistentEntityId(typeId, Long.valueOf(value));
        }
        return null;
    }

}
