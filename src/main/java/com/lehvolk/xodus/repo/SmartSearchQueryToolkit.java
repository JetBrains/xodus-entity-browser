package com.lehvolk.xodus.repo;

import java.io.ByteArrayInputStream;
import java.util.List;


/**
 * Toolkit for Smart Search
 * @author Alexey Volkov
 * @since 02.11.15
 */
public class SmartSearchQueryToolkit {

    private SmartSearchQueryToolkit() {
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

}
