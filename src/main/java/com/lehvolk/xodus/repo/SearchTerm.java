package com.lehvolk.xodus.repo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Alexey Volkov
 * @since 07.11.2015
 */
@Getter
@AllArgsConstructor
public class SearchTerm<T> {

    private static final Pattern RANGE_PATTERN = Pattern.compile("\\[(\\d*),\\s*(\\d*)\\]");

    public enum SearchTermType {
        VALUE,
        LIKE,
        RANGE;
    }

    @Getter
    @AllArgsConstructor
    public static class Range {
        private final long start;
        private final long end;
    }

    private final String property;
    private final T value;
    private final SearchTermType type;

    public static SearchTerm<?> from(String property, String operand, String value) {
        SearchTermType type = "~".equals(operand) ? SearchTermType.LIKE : SearchTermType.VALUE;
        Matcher matcher = RANGE_PATTERN.matcher(value);
        if (matcher.matches()) {
            long start = Long.valueOf(matcher.group(1));
            long end = Long.valueOf(matcher.group(2));
            return new SearchTerm<>(property, new Range(start, end), SearchTermType.RANGE);
        }
        return new SearchTerm<>(property, value, type);
    }
}
