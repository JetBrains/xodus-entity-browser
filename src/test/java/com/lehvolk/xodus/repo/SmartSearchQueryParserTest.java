package com.lehvolk.xodus.repo;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.lehvolk.xodus.repo.SearchTerm.Range;
import com.lehvolk.xodus.repo.SearchTerm.SearchTermType;
import static org.junit.Assert.assertEquals;

public class SmartSearchQueryParserTest {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";

    @Test
    public void testSimple() {
        List<SearchTerm<?>> result = parse("firstName='John' and lastName='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
    }

    @Test
    public void testSingleParam() {
        List<SearchTerm<?>> result = parse("firstName='John'");
        assertEquals(1, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
    }

    @Test
    public void testWithoutQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName=McCain and age=43");
        assertEquals(3, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
        checkTerm(result.get(2), "age", "43", SearchTermType.VALUE);
    }

    @Test
    public void testLike() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName~McCain");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.LIKE);
    }

    @Test
    public void testSingleParamsWithoutQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John");
        assertEquals(1, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
    }

    @Test
    public void testEscapingQuotes() {
        List<SearchTerm<?>> result = parse("firstName=John and lastName='Mc''Cain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "Mc'Cain", SearchTermType.VALUE);
    }

    @Test
    public void testOmitAndIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John and Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John and Mike", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
    }

    @Test
    public void testEqualsIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John=Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John=Mike", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
    }

    @Test
    public void testSpacesIntoQuotes() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
    }

    @Test
    public void testSpecialSymbols() {
        List<SearchTerm<?>> result = parse("'_!@firstName'='John Mike' and '_!@lastName'='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), "_!@firstName", "John Mike", SearchTermType.VALUE);
        checkTerm(result.get(1), "_!@lastName", "McCain", SearchTermType.VALUE);
    }

    @Test
    public void testRange() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' and age=[30,40]");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        checkRangeTerm(result.get(1), "age", 30, 40);
    }


    @Test(expected = ParseException.class)
    public void testCheckThrowsExceptionOnEmptyMap() throws ParseException {
        SmartSearchQueryParser.check(Collections.emptyList(), "");
    }

    @Test(expected = IllegalStateException.class)
    public void toolkitShouldThrowIllegalStateExceptionOnInit() throws ParseException {
        SmartSearchQueryToolkit.newParser(null);
    }

    private static List<SearchTerm<?>> parse(String query) {
        try {
            return SmartSearchQueryToolkit.newParser(query).parse();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testCaseInsensitive() {
        List<SearchTerm<?>> result = parse("firstName='John Mike' AND lastName='McCain'");
        assertEquals(2, result.size());
        checkTerm(result.get(0), FIRST_NAME, "John Mike", SearchTermType.VALUE);
        checkTerm(result.get(1), LAST_NAME, "McCain", SearchTermType.VALUE);
    }

    private void checkTerm(SearchTerm<?> term, String property, String value, SearchTermType type) {
        assertEquals(property, term.getProperty());
        assertEquals(value, term.getValue());
        assertEquals(type, term.getType());
    }

    @SuppressWarnings("unchecked")
    private void checkRangeTerm(SearchTerm<?> term, String property, long start, long end) {
        assertEquals(SearchTermType.RANGE, term.getType());
        SearchTerm<Range> rangedTerm = (SearchTerm<Range>) term;
        assertEquals(property, term.getProperty());
        assertEquals(start, rangedTerm.getValue().getStart());
        assertEquals(end, rangedTerm.getValue().getEnd());
    }

}
