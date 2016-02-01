package com.lehvolk.xodus.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchTerm {
    public static class Range {
        public final long start;
        public final long end;

        public Range(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    public enum SearchTermType {
        VALUE,
        LIKE,
        RANGE
    }


    private static Pattern RANGE_PATTERN = Pattern.compile("\\[\\s*(\\d*)\\s*,\\s*(\\d*)\\s*\\]");

    public String property;
    public Object value;
    public SearchTermType type;

    public SearchTerm(String property, Object value, SearchTermType type) {
        this.property = property;
        this.value = value;
        this.type = type;
    }

    public static SearchTerm from(String property, String operand, String value) {
        SearchTermType type = "~".equals(operand) ? SearchTermType.LIKE : SearchTermType.VALUE;
        Matcher matcher = RANGE_PATTERN.matcher(value);
        if (matcher.matches()) {
            long start = java.lang.Long.valueOf(matcher.group(1));
            long end = java.lang.Long.valueOf(matcher.group(2));
            return new SearchTerm(property, new Range(start, end), SearchTermType.RANGE);
        }
        return new SearchTerm(property, value, type);
    }
}
