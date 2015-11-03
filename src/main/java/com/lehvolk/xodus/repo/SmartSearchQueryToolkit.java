package com.lehvolk.xodus.repo;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Toolkit for Smart Search
 * @author Alexey Volkov
 * @since 02.11.15
 */
public class SmartSearchQueryToolkit {

    @Getter
    @AllArgsConstructor
    public static class SearchItem {

        private static final Pattern RANGE_PATTERN = Pattern.compile("\\[\\d*,\\d*\\]");
        private final String property;
        private final String value;

        public boolean isRange() {
            return RANGE_PATTERN.matcher(value).matches();
        }
    }

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

    public static List<SearchItem> parse(String query) {
        try {
            return newParser(query).parse().entrySet().stream().map(x -> new SearchItem(x.getKey(), x.getValue()))
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

}
