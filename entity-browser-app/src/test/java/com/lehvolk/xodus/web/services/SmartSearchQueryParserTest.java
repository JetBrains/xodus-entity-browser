package com.lehvolk.xodus.web.services;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.lehvolk.xodus.web.services.SearchTerm.Range;
import com.lehvolk.xodus.web.services.SearchTerm.SearchTermType;
import static org.junit.Assert.assertEquals;

public class SmartSearchQueryParserTest {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";

    @Test
    public void testSimple() {
        List<SearchTerm<?>> result = parse("firstName='John' and lastName='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
    }

    @Test
    public void testSingleParam() {
        List<SearchTerm<?>> result = parse("firstName='John'");
        assertEquals(1, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
    }

    @Test
    public void testWithoutQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName=McClane and age=43");
        assertEquals(3, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
        verifyTerm(result.get(2), "age", "43", SearchTermType.VALUE);
    }

    @Test
    public void testLike() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName~McClane");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.LIKE);
    }

    @Test
    public void testSingleParamsWithoutQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John");
        assertEquals(1, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
    }

    @Test
    public void testEscapingQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName='Mc''Clane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "Mc'Clane", SearchTermType.VALUE);
    }

    @Test
    public void testOmitAndIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John and Mike' and lastName='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John and Mike", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
    }

    @Test
    public void testEqualsIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John=Mike' and lastName='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John=Mike", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
    }

    @Test
    public void testSpacesIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' and lastName='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
    }

    @Test
    public void testSpecialSymbols() {
        List<SearchTerm<?>> result = parse("'_!@firstName'='John Mike' and '_!@lastName'='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), "_!@firstName", "John Mike", SearchTermType.VALUE);
        verifyTerm(result.get(1), "_!@lastName", "McClane", SearchTermType.VALUE);
    }

    @Test
    public void testRange() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' and age=[30,40]");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        verifyRangeTerm(result.get(1), "age", 30, 40);
    }


    @Test(expected = ParseException.class)
    public void testCheckThrowsExceptionOnEmptyMap() throws ParseException {
        SmartSearchQueryParser.check(Collections.emptyList(), "");
    }

    @Test(expected = IllegalStateException.class)
    public void toolkitShouldThrowIllegalStateExceptionOnInit() throws ParseException {
        SmartSearchToolkit.newParser(null);
    }

    private static List<SearchTerm<?>> parse(String query) {
        try {
            return SmartSearchToolkit.newParser(query).parse();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testCaseInsensitive() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' AND lastName='McClane'");
        assertEquals(2, result.size());
        verifyTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        verifyTerm(result.get(1), LAST_NAME, "McClane", SearchTermType.VALUE);
    }

    private void verifyTerm(SearchTerm<?> term, String property, String value, SearchTermType type) {
        assertEquals(property, term.getProperty());
        assertEquals(value, term.getValue());
        assertEquals(type, term.getType());
    }

    @SuppressWarnings("unchecked")
    private void verifyRangeTerm(SearchTerm<?> term, String property, long start, long end) {
        assertEquals(SearchTermType.RANGE, term.getType());
        SearchTerm<Range> rangedTerm = (SearchTerm<Range>) term;
        assertEquals(property, term.getProperty());
        assertEquals(start, rangedTerm.getValue().getStart());
        assertEquals(end, rangedTerm.getValue().getEnd());
    }

}
