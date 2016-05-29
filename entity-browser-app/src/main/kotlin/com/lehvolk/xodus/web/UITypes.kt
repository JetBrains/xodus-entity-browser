package com.lehvolk.xodus.web


import jetbrains.exodus.bindings.ComparableSet
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object UIPropertyTypes {
    val log = LoggerFactory.getLogger(UIPropertyTypes.javaClass)

    class TypeTreeNode(val type: UIPropertyType<*>, val low: TypeTreeNode? = null) {

        fun find(tr: StoreTransaction, type: String, property: String, value: String): EntityIterable {
            try {
                val realValue = this.type.toValue(value)
                log.debug("searching property '{}' by type {} and value {} ", property, realValue?.javaClass?.name, realValue)
                return if (realValue != null) {
                    var result = tr.find(type, property, realValue)
                    log.debug("found: {} results", result.size())
                    if (low != null) {
                        log.debug("searching childs of {}", low.type.clazz)
                        result = result.union(low.find(tr, type, property, value))
                    }
                    result
                } else {
                    EntityIterableBase.EMPTY
                }
            } catch(e: Exception) {
                //ignore
                return EntityIterableBase.EMPTY
            }
        }

        fun find(tr: StoreTransaction, type: String, property: String, start: String, end: String): EntityIterable {
            try {
                val realStart = this.type.toValue(start)
                val realEnd = this.type.toValue(end)
                if (realStart != null && realEnd != null) {
                    var result = tr.find(type, property, realStart, realEnd)
                    if (low != null) {
                        return result.union(low.find(tr, type, property, start, end))
                    }
                    return result
                }
                return EntityIterableBase.EMPTY
            } catch(e: Exception) {
                //ignore
                return EntityIterableBase.EMPTY
            }
        }
    }

    private val BY_CLASS = ConcurrentHashMap<Class<out Comparable<*>>, UIPropertyType<*>>()
    private val BY_NAME = ConcurrentHashMap<String, UIPropertyType<*>>()

    private val STRING = newType { it }
    private val BOOLEAN = newType { java.lang.Boolean.valueOf(it) }

    private val BYTE = newType { java.lang.Byte.valueOf(it) }
    private val SHORT = newType { java.lang.Short.valueOf(it) }
    private val INT = newType { Integer.valueOf(it) }
    private val LONG = newType { java.lang.Long.valueOf(it) }
    private val FLOAT = newType { java.lang.Float.valueOf(it) }
    private val DOUBLE = newType { java.lang.Double.valueOf(it) }
    private val CMP_SET = newType {
        val matchResult = Regex("ComparableSet\\[(.*)\\]").matchEntire(it)
        if (matchResult != null) {
            val (commaSeparatedValues) = matchResult.destructured
            ComparableSet(commaSeparatedValues.split(","))
        } else {
            ComparableSet<String>()
        }
    }

    val rangeTree =
            TypeTreeNode(LONG,
                    TypeTreeNode(INT,
                            TypeTreeNode(SHORT,
                                    TypeTreeNode(BYTE))))

    val tree = arrayOf(
            TypeTreeNode(STRING),
            TypeTreeNode(BOOLEAN),
            TypeTreeNode(DOUBLE,
                    TypeTreeNode(FLOAT)
            ),
            rangeTree)

    class UIPropertyType<T : Comparable<*>>(val clazz: String, val function: (String) -> T) {

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
    inline private fun <reified T : Comparable<*>> newType(noinline function: (String) -> T): UIPropertyType<T> {
        val fullName = T::class.java.name
        val type = UIPropertyType(fullName, function)
        BY_CLASS.put(Class.forName(fullName) as Class<out Comparable<*>>, type)
        BY_NAME.put(fullName, type)
        return type
    }

    fun isSupported(clazz: Class<*>): Boolean {
        return BY_CLASS.containsKey(clazz)
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
