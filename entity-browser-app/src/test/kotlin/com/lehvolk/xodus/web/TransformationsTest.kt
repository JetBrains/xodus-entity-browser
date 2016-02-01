package com.lehvolk.xodus.web

import org.junit.Assert.*
import org.junit.Test


class TransformationsTest {

    private val VALUE = "123"

    @Test
    @Throws(Exception::class)
    fun testString2valueForShort() {
        val property = ofType(VALUE, "Short")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Short"))
        assertEquals(x, 123.toShort())
    }

    @Test
    @Throws(Exception::class)
    fun testString2valueForInteger() {
        val property = ofType(VALUE, "Integer")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Integer"))
        assertEquals(x, 123)
    }

    @Test
    @Throws(Exception::class)
    fun testString2valueForLong() {
        val property = ofType(VALUE, "Long")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Long"))
        assertEquals(x, 123L)
    }

    @Test
    @Throws(Exception::class)
    fun testString2valueForFloat() {
        val property = ofType(VALUE, "Float")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Float"))
        assertEquals(x as Float, java.lang.Float.valueOf(123f), 0.0E-9f)
    }

    @Test
    @Throws(Exception::class)
    fun testString2valueForDouble() {
        val property = ofType(VALUE, "Double")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Double"))
        assertEquals(x as Double, 123.0, 0.0E-9)
    }

    @Test
    @Throws(Exception::class)
    fun testString2valueForString() {
        val property = ofType(VALUE, "String")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("String"))
        assertEquals(x, VALUE)
    }

    @Test(expected = InvalidFieldException::class)
    @Throws(Exception::class)
    fun testString2valueForIncorrectValue() {
        val property = ofType("123xasadasd", "Double")
        property.string2value()
    }

    @Test
    @Throws(Exception::class)
    fun testValue2stringForNull() {
        val property = ofType(null, "Double")
        assertNull(property.string2value())
    }

    private fun ofType(value: String?, clazz: String): EntityProperty {
        val property = EntityProperty()
        property.name = "dummy"
        property.value = value
        property.type = PropertyType()
        property.type.clazz = javaClass(clazz).name
        return property
    }

    fun javaClass(clazz: String): Class<*> {
        return Class.forName("java.lang." + clazz)
    }

}