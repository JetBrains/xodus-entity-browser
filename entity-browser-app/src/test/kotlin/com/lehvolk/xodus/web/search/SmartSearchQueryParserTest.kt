package com.lehvolk.xodus.web.search


import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSearchQueryParserTest {

    @Test
    fun testSimple() {
        val result = "firstName='John' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
        result[1].assertPropertyValueTerm("lastName", "McClane")
    }

    @Test
    fun testSingleParam() {
        val result = "firstName='John'".parse()
        assertEquals(1, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
    }

    @Test
    fun testWithoutQuotes() {
        val result = "firstName=John and lastName=McClane and age=43".parse()
        assertEquals(3, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
        result[1].assertPropertyValueTerm("lastName", "McClane")
        result[2].assertPropertyValueTerm("age", "43")
    }

    @Test
    fun testLike() {
        val result = "firstName=John and lastName~McClane".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
        result[1].assertPropertyLikeTerm("lastName", "McClane")
    }

    @Test
    fun testSingleParamsWithoutQuotes() {
        val result = "firstName=John".parse()
        assertEquals(1, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
    }

    @Test
    fun testEscapingQuotes() {
        val result = "firstName=John and lastName='Mc''Clane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John")
        result[1].assertPropertyValueTerm("lastName", "Mc'Clane")
    }

    @Test
    fun testOmitAndIntoQuotes() {
        val result = "firstName='John and Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John and Mike")
        result[1].assertPropertyValueTerm("lastName", "McClane")
    }

    @Test
    fun testEqualsIntoQuotes() {
        val result = "firstName='John=Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John=Mike")
        result[1].assertPropertyValueTerm("lastName", "McClane")
    }

    @Test
    fun testSpacesIntoQuotes() {
        val result = "firstName='John Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John Mike")
        result[1].assertPropertyValueTerm("lastName", "McClane")
    }

    @Test
    fun testSpecialSymbols() {
        val result = "'_!@firstName'='John Mike' and '_!@lastName'='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("_!@firstName", "John Mike")
        result[1].assertPropertyValueTerm("_!@lastName", "McClane")
    }

    @Test
    fun testRange() {
        val result = "firstName='John Mike' and age=[30,40]".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John Mike")
        result[1].assertPropertyRangeTerm("age", 30, 40)
    }

    @Test
    fun testLink() {
        val result = "@linkName=MyType[3] and firstName~Jo".parse()
        assertEquals(2, result.size)
        result[0].assertLinkTerm("linkName", "MyType", 3)
        result[1].assertPropertyLikeTerm("firstName", "Jo")
    }

    @Test
    fun testPropNull() {
        val result = "firstName=null and lastName='null'".parse()
        assertEquals(2, result.size)
        result[0].assertPropertyValueTerm("firstName", null)
        result[1].assertPropertyValueTerm("lastName", "null")
    }

    @Test
    fun testLinkNull() {
        val result = "@user=null and @action=MyAction[4]".parse()
        assertEquals(2, result.size)
        result[0].assertLinkTerm("user", null, null)
        result[1].assertLinkTerm("action", "MyAction", 4)
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
        result[0].assertPropertyValueTerm("firstName", "John Mike")
        result[1].assertPropertyValueTerm("lastName", "McClane")
    }

    private fun SearchTerm.assertPropertyLikeTerm(expectedName: String, expectedValue: String) {
        val actual = this as PropertyLikeSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedValue, actual.value)
    }

    private fun SearchTerm.assertPropertyRangeTerm(expectedName: String, expectedStart: Long, expectedEnd: Long) {
        val actual = this as PropertyRangeSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedStart, actual.start)
        assertEquals(expectedEnd, actual.end)
    }

    private fun SearchTerm.assertPropertyValueTerm(expectedName: String, expectedValue: String?) {
        val actual = this as PropertyValueSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedValue, actual.value)
    }

    private fun SearchTerm.assertLinkTerm(expectedName: String, expectedTypeName: String?, expectedLocalId: Long?) {
        val actual = this as LinkSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedTypeName, actual.oppositeEntityTypeName)
        assertEquals(expectedLocalId, actual.oppositeEntityLocalId)
    }
}
