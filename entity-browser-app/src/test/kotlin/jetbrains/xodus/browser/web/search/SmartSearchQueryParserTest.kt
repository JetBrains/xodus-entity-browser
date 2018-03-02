package jetbrains.xodus.browser.web.search


import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSearchQueryParserTest {

    @Test
    fun testSimple() {
        val result = "firstName='John' and lastName='McClane' and age!=34".parse()
        assertEquals(3, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
        result[1].assertPropertyValueTerm("lastName", "McClane", true)
        result[2].assertPropertyValueTerm("age", "34", false)
    }

    @Test
    fun testSingleParam() {
        val result = "firstName='John'".parse()
        assertEquals(1, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
    }

    @Test
    fun testWithoutQuotes() {
        val result = "firstName=John and lastName!=McClane and age=43".parse()
        assertEquals(3, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
        result[1].assertPropertyValueTerm("lastName", "McClane", false)
        result[2].assertPropertyValueTerm("age", "43", true)
    }

    @Test
    fun testLike() {
        val result = "firstName=John and lastName~McClane".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
        result[1].assertPropertyLikeTerm("lastName", "McClane")
    }

    @Test
    fun testSingleParamsWithoutQuotes() {
        val result = "firstName=John".parse()
        assertEquals(1, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
    }

    @Test
    fun testEscapingQuotes() {
        val result = "firstName=John and lastName='Mc''Clane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John", true)
        result[1].assertPropertyValueTerm("lastName", "Mc'Clane", true)
    }

    @Test
    fun testOmitAndIntoQuotes() {
        val result = "firstName!='John and Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John and Mike", false)
        result[1].assertPropertyValueTerm("lastName", "McClane", true)
    }

    @Test
    fun testEqualsIntoQuotes() {
        val result = "firstName='John=Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John=Mike", true)
        result[1].assertPropertyValueTerm("lastName", "McClane", true)
    }

    @Test
    fun testSpacesIntoQuotes() {
        val result = "firstName='John Mike' and lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John Mike", true)
        result[1].assertPropertyValueTerm("lastName", "McClane", true)
    }

    @Test
    fun testSpecialSymbols() {
        val result = "'_!@firstName'='John Mike' and '_!@lastName'='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("_!@firstName", "John Mike", true)
        result[1].assertPropertyValueTerm("_!@lastName", "McClane", true)
    }

    @Test
    fun testRange() {
        val result = "firstName!='John Mike' and age=[30,40]".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John Mike", false)
        result[1].assertPropertyRangeTerm("age", 30, 40)
    }

    @Test
    fun testLink() {
        val result = "@linkName!=MyType[3] and firstName~Jo".parse()
        assertEquals(2, result.size)
        result[0].assertLinkTerm("linkName", "MyType", 3, false)
        result[1].assertPropertyLikeTerm("firstName", "Jo")
    }

    @Test
    fun testPropNull() {
        val result = "firstName=null and lastName='null'".parse()
        assertEquals(2, result.size)
        result[0].assertPropertyValueTerm("firstName", null, true)
        result[1].assertPropertyValueTerm("lastName", "null", true)
    }

    @Test
    fun testLinkNull() {
        val result = "@user!=null and @action=MyAction[4]".parse()
        assertEquals(2, result.size)
        result[0].assertLinkTerm("user", null, null, false)
        result[1].assertLinkTerm("action", "MyAction", 4, true)
    }

    @Test(expected = jetbrains.xodus.browser.web.search.ParseException::class)
    @Throws(jetbrains.xodus.browser.web.search.ParseException::class)
    fun testCheckThrowsExceptionOnEmptyMap() {
        jetbrains.xodus.browser.web.search.SmartSearchQueryParser.check(emptyList<SearchTerm>(), "")
    }

    @Test
    fun testCaseInsensitive() {
        val result = "firstName='John Mike' AND lastName='McClane'".parse()
        assertEquals(2, result.size.toLong())
        result[0].assertPropertyValueTerm("firstName", "John Mike", true)
        result[1].assertPropertyValueTerm("lastName", "McClane", true)
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

    private fun SearchTerm.assertPropertyValueTerm(expectedName: String, expectedValue: String?, expectedEquals: Boolean) {
        val actual = this as PropertyValueSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedValue, actual.value)
        assertEquals(expectedEquals, actual.equals)
    }

    private fun SearchTerm.assertLinkTerm(expectedName: String, expectedTypeName: String?, expectedLocalId: Long?, expectedEquals: Boolean) {
        val actual = this as LinkSearchTerm
        assertEquals(expectedName, actual.name)
        assertEquals(expectedTypeName, actual.oppositeEntityTypeName)
        assertEquals(expectedLocalId, actual.oppositeEntityLocalId)
        assertEquals(expectedEquals, actual.equals)
    }
}
