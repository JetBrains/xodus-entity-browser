package com.lehvolk.xodus.web

import jetbrains.exodus.entitystore.EntityId
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityId
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import java.io.ByteArrayInputStream
import java.util.regex.Pattern

object SmartSearchToolkit {

    private val INTEGER = Pattern.compile("\\d*")

    fun doSmartSearch(term: String?, type: String, typeId: Int, t: StoreTransaction): EntityIterable {
        if (term == null || term.trim { it <= ' ' }.isEmpty()) {
            return  t.getAll(type)
        } else {
            val entityId = toEntityId(typeId, term)
            if (entityId != null) {
                return orEmpty {
                    t.getSingletonIterable(t.getEntity(entityId))
                }
            } else {
                return orEmpty {
                    searchByTerms(term, type, typeId, t)
                }
            }
        }
    }

    private fun orEmpty (f: () -> EntityIterable) : EntityIterable {
        try {
            return f()
        } catch (e: RuntimeException) {
            return EntityIterableBase.EMPTY
        }
    }

    private fun searchByTerms(term: String, type: String, typeId: Int, t: StoreTransaction): EntityIterable {
        val termStream = term.parse()
        return termStream.map { item ->
            var itemResult: EntityIterable?
            val isById = "id".equals(item.property, ignoreCase = true)
            when (item.value) {
                SearchTermType.RANGE -> {
                    item.value as Range
                    itemResult = t.find(type, item.property, item.value.start, item.value.end)
                    if (isById) {
                        itemResult = itemResult.union(t.findIds(type, item.value.start, item.value.end))
                    }
                }
                SearchTermType.LIKE -> itemResult = t.findStartingWith(type, item.property, item.value.toString())
                else -> {
                    val value = item.value.toString()
                    itemResult = t.find(type, item.property, value)
                    if (isById) {
                        val byId = toEntityId(typeId, value)
                        if (byId != null) {
                            itemResult = itemResult.union(t.getSingletonIterable(t.getEntity(byId)))
                        }
                    }
                }
            }
            itemResult
        }.reduce { it1, it2 -> it1!!.intersect(it2!!) }!!
    }

    private fun toEntityId(typeId: Int, value: String): EntityId? {
        if (INTEGER.matcher(value).matches()) {
            return PersistentEntityId(typeId, java.lang.Long.valueOf(value)!!)
        }
        return null
    }

}

enum class SearchTermType {
    VALUE,
    LIKE,
    RANGE
}

class Range(val start: Long = 0, val end: Long = 0)

class SearchTerm(val property: String, val value: Any, val type: SearchTermType) {

    companion object {

        private val RANGE_PATTERN = Pattern.compile("\\[\\s*(\\d*)\\s*,\\s*(\\d*)\\s*\\]")

        @JvmStatic
        fun from(property: String, operand: String, value: String): SearchTerm {
            val type = if ("~" == operand) SearchTermType.LIKE else SearchTermType.VALUE
            val matcher = RANGE_PATTERN.matcher(value)
            if (matcher.matches()) {
                val start = java.lang.Long.valueOf(matcher.group(1))!!
                val end = java.lang.Long.valueOf(matcher.group(2))!!
                return SearchTerm(property, Range(start, end), SearchTermType.RANGE)
            }
            return SearchTerm(property, value, type)
        }
    }
}

fun String.newParser(): SmartSearchQueryParser {
    try {
        return SmartSearchQueryParser(ByteArrayInputStream(this.toByteArray()))
    } catch (e: RuntimeException) {
        throw IllegalStateException(e)
    }
}

fun String.parse(): List<SearchTerm> {
    try {
        return this.newParser().parse()
    } catch (e: ParseException) {
        throw IllegalStateException(e)
    }
}