package com.lehvolk.xodus.web.search

import jetbrains.exodus.entitystore.EntityId
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityId
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import java.io.ByteArrayInputStream


fun smartSearch(term: String?, type: String, typeId: Int, t: StoreTransaction): EntityIterable {
    if (term == null || term.trim { it <= ' ' }.isEmpty()) {
        return t.getAll(type)
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

private fun searchByTerms(term: String, type: String, typeId: Int, t: StoreTransaction): EntityIterable {
    val termStream = term.parse()
    return termStream
            .map { it.search(t, type, typeId) }
            .reduce { it1, it2 -> it1.intersect(it2) }
}

private fun orEmpty(f: () -> EntityIterable): EntityIterable {
    try {
        return f()
    } catch (e: RuntimeException) {
        return EntityIterableBase.EMPTY
    }
}

private fun toEntityId(typeId: Int, value: String): EntityId? {
    if (value.toLongOrNull() != null) {
        return PersistentEntityId(typeId, value.toLong())
    }
    return null
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