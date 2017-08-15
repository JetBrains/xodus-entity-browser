package com.lehvolk.xodus.web.search


import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSearchQueryParserTest {

    private val FIRST_NAME = "firstName"
    private val LAST_NAME = "lastName"


    @Test
    fun testSimple() {
        val result = "firstName='John' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
    }

    @Test
    fun testSingleParam() {
        val result = "firstName='John'".parse()
        assertEquals(1, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
    }

    @Test
    fun testWithoutQuotes() {
        val result = "firstName=John and lastName=McClane and age=43".parse()
        assertEquals(3, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
        verifyTerm(result[2], "age", "43", SearchTermType.VALUE)
    }

    @Test
    fun testLike() {
        val result = "firstName=John and lastName~McClane".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.LIKE)
    }

    @Test
    fun testSingleParamsWithoutQuotes() {
        val result = "firstName=John".parse()
        assertEquals(1, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
    }

    @Test
    fun testEscapingQuotes() {
        val result = "firstName=John and lastName='Mc''Clane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "Mc'Clane", SearchTermType.VALUE)
    }

    @Test
    fun testOmitAndIntoQuotes() {
        val result = "firstName='John and Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John and Mike", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
    }

    @Test
    fun testEqualsIntoQuotes() {
        val result = "firstName='John=Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John=Mike", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
    }

    @Test
    fun testSpacesIntoQuotes() {
        val result = "firstName='John Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John Mike", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
    }

    @Test
    fun testSpecialSymbols() {
        val result = "'_!@firstName'='John Mike' and '_!@lastName'='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], "_!@firstName", "John Mike", SearchTermType.VALUE)
        verifyTerm(result[1], "_!@lastName", "McClane", SearchTermType.VALUE)
    }

    @Test
    fun testRange() {
        val result = "firstName='John Mike' and age=[30,40]".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John Mike", SearchTermType.VALUE)
        verifyRangeTerm(result[1], "age", 30, 40)
    }

    @Test
    fun testLink() {
        val result = "@linkName=MyType[3] and firstName~Jo".parse()
        assertEquals(2, result.size)
        verifyTerm(result[0], "@linkName", "MyType[3]", SearchTermType.VALUE, SearchType.LINK)
        verifyTerm(result[1], FIRST_NAME, "Jo", SearchTermType.LIKE)
    }

    @Test
    fun testPropNull() {
        val result = "firstName=null and lastName='null'".parse()
        assertEquals(2, result.size)
        verifyTerm(result[0], FIRST_NAME, null, SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "null", SearchTermType.VALUE)
    }

    @Test
    fun testLinkNull() {
        val result = "@user=null and @action=MyAction[4]".parse()
        assertEquals(2, result.size)
        verifyTerm(result[0], "@user", null, SearchTermType.VALUE, SearchType.LINK)
        verifyTerm(result[1], "@action", "MyAction[4]", SearchTermType.VALUE, SearchType.LINK)
    }

    @Test(expected = ParseException::class)
    @Throws(ParseException::class)
    fun testCheckThrowsExceptionOnEmptyMap() {
        SmartSearchQueryParser.check(emptyList<SearchTerm>(), "")
    }

    @Test
    fun testCaseInsensitive() {
        val result = "firstName='John Mike' AND lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        verifyTerm(result[0], FIRST_NAME, "John Mike", SearchTermType.VALUE)
        verifyTerm(result[1], LAST_NAME, "McClane", SearchTermType.VALUE)
    }

    private fun verifyTerm(term: SearchTerm, property: String, value: String?, type: SearchTermType, searchType: SearchType = SearchType.PROPERTY) {
        assertEquals(property, term.property)
        assertEquals(value, term.value)
        assertEquals(type, term.termType)
        assertEquals(searchType, term.searchType)
    }

    @SuppressWarnings("unchecked")
    private fun verifyRangeTerm(term: SearchTerm, property: String, start: Long, end: Long) {
        assertEquals(SearchTermType.RANGE, term.termType)
        val rangedTerm = term.value as SearchTerm.Range
        assertEquals(property, term.property)
        assertEquals(start, rangedTerm.start)
        assertEquals(end, rangedTerm.end)
    }

}
