package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.SearchTerm.SearchTermType
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
            when (item.type) {
                SearchTermType.RANGE -> {
                    val value = item.value as SearchTerm.Range
                    itemResult = t.find(type, item.property, value.start, value.end)
                    if (isById) {
                        itemResult = itemResult.union(t.findIds(type, value.start, value.end))
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