package com.lehvolk.xodus.web

import org.junit.Assert.*
import org.junit.Test


class TransformationsTest {

    private val VALUE = "123"

    @Test
    fun `should convert string to value for short`() {
        val property = ofType(VALUE, "Short")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Short"))
        assertEquals(x, 123.toShort())
    }

    @Test
    fun `should convert string to value for Integer`() {
        val property = ofType(VALUE, "Integer")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Integer"))
        assertEquals(x, 123)
    }

    @Test
    fun `should convert string to value for Long`() {
        val property = ofType(VALUE, "Long")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Long"))
        assertEquals(x, 123L)
    }

    @Test
    fun `should convert string to value for Float`() {
        val property = ofType(VALUE, "Float")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Float"))
        assertEquals(x as Float, java.lang.Float.valueOf(123f), 0.0E-9f)
    }

    @Test
    fun `should convert string to value for Double`() {
        val property = ofType(VALUE, "Double")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("Double"))
        assertEquals(x as Double, 123.0, 0.0E-9)
    }

    @Test
    fun `should convert string to value for String`() {
        val property = ofType(VALUE, "String")
        val x = property.string2value()
        assertNotNull(x)
        assertSame(x?.javaClass, javaClass("String"))
        assertEquals(x, VALUE)
    }

    @Test(expected = InvalidFieldException::class)
    fun `should convert string to value for incorrect value`() {
        val property = ofType("123xasadasd", "Double")
        property.string2value()
    }

    @Test
    fun `should convert value to string for null`() {
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

    private fun javaClass(clazz: String): Class<*> {
        return Class.forName("java.lang." + clazz)
    }

}