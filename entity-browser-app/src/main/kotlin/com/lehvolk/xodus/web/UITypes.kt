package com.lehvolk.xodus.web


import java.util.concurrent.ConcurrentHashMap

object UIPropertyTypes {

    private val BY_CLASS = ConcurrentHashMap<Class<out Comparable<*>>, UIPropertyType<*>>()
    private val BY_NAME = ConcurrentHashMap<String, UIPropertyType<*>>()

    private val STRING = newType("java.lang.String", { it })
    private val BOOLEAN = newType("java.lang.Boolean", { java.lang.Boolean.valueOf(it) })

    private val BYTE = newType("java.lang.Byte", { java.lang.Byte.valueOf(it) })
    private val SHORT = newType("java.lang.Short", { java.lang.Short.valueOf(it) })
    private val INT = newType("java.lang.Integer", { Integer.valueOf(it) })
    private val LONG = newType("java.lang.Long", { java.lang.Long.valueOf(it) })
    private val FLOAT = newType("java.lang.Float", { java.lang.Float.valueOf(it) })
    private val DOUBLE = newType("java.lang.Double", { java.lang.Double.valueOf(it) })

    class UIPropertyType<T : Comparable<*>> constructor(private val clazz: String, private val function: (String) -> T) {

        fun toString(value: T?): String? {
            if (value == null) {
                return null
            }
            return value.toString()
        }

        fun toValue(value: String?): T? {
            if (value == null) {
                return null
            }
            return function(value)
        }

        fun isValid(value: String): Boolean {
            try {
                function(value)
                return true
            } catch (e: RuntimeException) {
                // ignore result if conversion failed
                return false
            }

        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<*>> newType(clazz: String, function: (String) -> T): UIPropertyType<T> {
        val type = UIPropertyType(clazz, function)
        BY_CLASS.put(Class.forName(clazz) as Class<out Comparable<*>>, type)
        BY_NAME.put(clazz, type)
        return type
    }

    fun isSupported(clazz: Class<*>): Boolean {
        return BY_CLASS.containsKey(clazz)
    }

    fun isSupported(clazz: String): Boolean {
        return BY_NAME.containsKey(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<*>> uiTypeOf(clazz: Class<*>): UIPropertyType<T> {
        return BY_CLASS[clazz] as UIPropertyType<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<*>> uiTypeOf(clazz: String): UIPropertyType<T> {
        return BY_NAME[clazz] as UIPropertyType<T>
    }
}
