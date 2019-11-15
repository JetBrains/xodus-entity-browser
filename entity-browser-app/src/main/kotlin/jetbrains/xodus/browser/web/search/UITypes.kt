package jetbrains.xodus.browser.web.search


import jetbrains.exodus.bindings.ComparableSet
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

object UIPropertyTypes : KLogging() {

    class TypeTreeNode(private val uiType: UIPropertyType<*>, private val low: TypeTreeNode? = null) {

        fun find(tr: StoreTransaction, type: String, property: String, value: String): EntityIterable {
            try {
                if (!uiType.isValid(value)) {
                    return EntityIterableBase.EMPTY
                }
                val realValue = uiType.toValue(value)
                logger.debug { "searching property '$property' by type '${realValue?.javaClass?.name}' and value '$realValue' " }
                return if (realValue != null) {
                    var result = tr.find(type, property, realValue)
                    logger.debug { "found: ${result.size()} results" }
                    if (low != null) {
                        logger.debug { "searching children of " + low.uiType.clazz }
                        result = result.union(low.find(tr, type, property, value))
                    }
                    result
                } else {
                    EntityIterableBase.EMPTY
                    
                }
            } catch (e: Exception) {
                //ignore
                return EntityIterableBase.EMPTY
            }
        }

        fun find(tr: StoreTransaction, type: String, property: String, start: String, end: String): EntityIterable {
            try {
                if (!uiType.isValid(start) || !uiType.isValid(end)) {
                    return EntityIterableBase.EMPTY
                }
                val realStart = uiType.toValue(start)
                val realEnd = uiType.toValue(end)
                if (realStart != null && realEnd != null) {
                    val result = tr.find(type, property, realStart, realEnd)
                    if (low != null) {
                        return result.union(low.find(tr, type, property, start, end))
                    }
                    return result
                }
                return EntityIterableBase.EMPTY
            } catch (e: Exception) {
                //ignore
                return EntityIterableBase.EMPTY
            }
        }
    }

    private val BY_CLASS = ConcurrentHashMap<Class<out Comparable<*>>, UIPropertyType<*>>()
    private val BY_NAME = ConcurrentHashMap<String, UIPropertyType<*>>()

    private val STRING = newType { it }
    private val BOOLEAN = newType {
        check(it == "true" || it == "false")

        java.lang.Boolean.valueOf(it)
    }

    private val BYTE = newType { java.lang.Byte.valueOf(it) }
    private val SHORT = newType { java.lang.Short.valueOf(it) }
    private val INT = newType { java.lang.Integer.valueOf(it) }
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
            return try {
                function(value)
                true
            } catch (e: RuntimeException) {
                // ignore result if conversion failed
                false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Comparable<*>> newType(noinline function: (String) -> T): UIPropertyType<T> {
        val fullName = T::class.java.name
        val type = UIPropertyType(fullName, function)
        BY_CLASS[Class.forName(fullName) as Class<out Comparable<*>>] = type
        BY_NAME[fullName] = type
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
