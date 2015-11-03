package com.lehvolk.xodus.repo;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmartSearchQueryParserTest {

    @Test
    public void testSimple() {
        Map<String, String> result = parse("firstName='John' and lastName='McCain'");
        assertEquals(2, result.size());
        assertEquals("John", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
    }

    @Test
    public void testSingleParam() {
        Map<String, String> result = parse("firstName='John'");
        assertEquals(1, result.size());
        assertEquals("John", result.get("firstName"));
    }

    @Test
    public void testWithoutQuotes() {
        Map<String, String> result = parse("firstName=John and lastName=McCain and age=43");
        assertEquals(3, result.size());
        assertEquals("John", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
        assertEquals("43", result.get("age"));
    }

    @Test
    public void testSingleParamsWithoutQuotes() {
        Map<String, String> result = parse("firstName=John");
        assertEquals(1, result.size());
        assertEquals("John", result.get("firstName"));
    }

    @Test
    public void testEscapingQuotes() {
        Map<String, String> result = parse("firstName=John and lastName='Mc''Cain'");
        assertEquals(2, result.size());
        assertEquals("John", result.get("firstName"));
        assertEquals("Mc'Cain", result.get("lastName"));
    }

    @Test
    public void testOmitAndIntoQuotes() {
        Map<String, String> result = parse("firstName='John and Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        assertEquals("John and Mike", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
    }

    @Test
    public void testEqualsIntoQuotes() {
        Map<String, String> result = parse("firstName='John=Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        assertEquals("John=Mike", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
    }

    @Test
    public void testSpacesIntoQuotes() {
        Map<String, String> result = parse("firstName='John Mike' and lastName='McCain'");
        assertEquals(2, result.size());
        assertEquals("John Mike", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
    }

    @Test
    public void testSpecialSymbols() {
        Map<String, String> result = parse("'_!@firstName'='John Mike' and '_!@lastName'='McCain'");
        assertEquals(2, result.size());
        assertEquals("John Mike", result.get("_!@firstName"));
        assertEquals("McCain", result.get("_!@lastName"));
    }

    @Test
    public void testRange() {
        Map<String, String> result =
                parse("firstName='John Mike' and lastName='McCain' and age=[30,40] and "
                        + "birthDate=[2009-10-10T00:00:00Z,2010-10-10T00:00:00Z]");
        assertEquals(4, result.size());
        assertEquals("John Mike", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
        assertEquals("[30,40]", result.get("age"));
        assertEquals("[2009-10-10T00:00:00Z,2010-10-10T00:00:00Z]", result.get("birthDate"));
    }


    @Test(expected = ParseException.class)
    public void testCheckThrowsExceptionOnEmptyMap() throws ParseException {
        SmartSearchQueryParser.check(Collections.<String, String>emptyMap(), "");
    }

    @Test(expected = IllegalStateException.class)
    public void toolkitShouldThrowIllegalStateExceptionOnInit() throws ParseException {
        SmartSearchQueryToolkit.newParser(null);
    }

    private static Map<String, String> parse(String query) {
        try {
            return SmartSearchQueryToolkit.newParser(query).parse();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testCaseInsensitive() {
        Map<String, String> result = parse("firstName='John Mike' AND lastName='McCain'");
        assertEquals(2, result.size());
        assertEquals("John Mike", result.get("firstName"));
        assertEquals("McCain", result.get("lastName"));
    }
}
