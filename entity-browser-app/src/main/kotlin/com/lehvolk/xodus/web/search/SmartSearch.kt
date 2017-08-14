package com.lehvolk.xodus.web.search

import jetbrains.exodus.entitystore.*
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import java.io.ByteArrayInputStream
import java.util.regex.Pattern



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
    return termStream.map { item ->
        search(t, item, type, typeId)
    }.reduce { it1, it2 -> it1.intersect(it2) }
}

private fun search(tx: StoreTransaction, term: SearchTerm, entityType: String, entityTypeId: Int): EntityIterable {
    val stringValue = term.value.toString()
    val localId = stringValue.toLongOrNull()
    val entityIdMatcher = ENTITY_ID.matcher(stringValue)
    return when {
        // propertyName~value
        term.type == SearchTermType.LIKE -> tx.searchByPropertyLike(entityType, term.property, stringValue)
        // propertyName=[1,8]
        term.type == SearchTermType.RANGE -> tx.searchByPropertyRange(entityType, term.property, term.value as SearchTerm.Range)
        // id=7
        term.property.isIdProperty && localId != null -> {
            val entityId = PersistentEntityId(entityTypeId, localId)
            tx.searchByIdProperty(entityId)
        }
        // linkName=MyType[4]
        entityIdMatcher.matches() && tx is PersistentStoreTransaction -> {
            val oppositeEntityType = entityIdMatcher.group("type")
            val oppositeLocalId = entityIdMatcher.group("id").toInt()
            val oppositeTypeId = tx.store.getEntityTypeId(tx, oppositeEntityType, false)
            if (oppositeTypeId >= 0) {
                val oppositeEntityId = tx.toEntityId("$oppositeTypeId-$oppositeLocalId")
                tx.searchByLinkValue(entityType, term.property, oppositeEntityId)
            } else {
                tx.searchByPropertyValue(entityType, term.property, stringValue)
            }
        }
        else -> {
            val typeInfo = tx.calculateApproxTypeInfo(entityType)
            when {
            // linkName = null
                typeInfo.isLink(term.property) && stringValue.isNullValue -> tx.searchByNullLink(entityType, term.property)
            // propertyName = null
                typeInfo.isProperty(term.property) && stringValue.isNullValue -> tx.searchByNullProperty(entityType, term.property)
            // propertyName = value
                else -> tx.searchByPropertyValue(entityType, term.property, stringValue)
            }
        }
    }
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

private val ENTITY_ID = Pattern.compile("^(?<type>[a-zA-z]\\w*)\\[(?<id>\\d+)]$")

private class EntityTypeInfo(val propertyNames: Set<String>, val linkNames: Set<String>) {
    fun isProperty(name: String) = propertyNames.contains(name)
    fun isLink(name: String) = linkNames.contains(name)
}

private fun StoreTransaction.calculateApproxTypeInfo(entityType: String): EntityTypeInfo {
    val sample = getAll(entityType).take(1000) // TODO: use reverse() in xodus 1.0.7
    val propertyNames = mutableSetOf<String>()
    val linkNames = mutableSetOf<String>()
    sample.forEach { entity ->
        propertyNames.addAll(entity.propertyNames)
        linkNames.addAll(entity.linkNames)
    }
    return EntityTypeInfo(propertyNames, linkNames)
}

private fun StoreTransaction.fullSearch(type: String, property: String, value: String): EntityIterable {
    return UIPropertyTypes.tree.map { it.find(this, type, property, value) }.reduce { it1, it2 -> it1.union(it2) }
}

private fun StoreTransaction.fullSearch(type: String, property: String, start: Long, end: Long): EntityIterable {
    return UIPropertyTypes.rangeTree.find(this, type, property, start.toString(), end.toString())
}

private val String.isIdProperty: Boolean get() = "id".equals(this, ignoreCase = true)

private val String.isNullValue:Boolean get() = "null" == this

private fun StoreTransaction.searchByNullProperty(entityType: String, propertyName: String): EntityIterable {
    return getAll(entityType).minus(findWithProp(entityType, propertyName))
}

private fun StoreTransaction.searchByPropertyLike(entityType: String, propertyName: String, value: String): EntityIterable {
    return findStartingWith(entityType, propertyName, value)
}

private fun StoreTransaction.searchByIdProperty(entityId: EntityId): EntityIterable {
    return getSingletonIterable(getEntity(entityId))
}

private fun StoreTransaction.searchByPropertyRange(entityType: String, propertyName: String, value: SearchTerm.Range): EntityIterable {
    return fullSearch(entityType, propertyName, value.start, value.end).let {
        if (propertyName.isIdProperty) {
            it.union(findIds(entityType, value.start, value.end))
        } else {
            it
        }
    }
}

private fun StoreTransaction.searchByPropertyValue(entityType: String, propertyName: String, value: String): EntityIterable {
    return fullSearch(entityType, propertyName, value)
}

private fun StoreTransaction.searchByNullLink(entityType: String, linkName: String): EntityIterable {
    return getAll(entityType).minus(findWithLinks(entityType, linkName))
}

private fun StoreTransaction.searchByLinkValue(entityType: String, linkName: String, oppositeEntityId: EntityId): EntityIterable {
    val oppositeEntity = try {
        getEntity(oppositeEntityId)
    } catch (ex: EntityRemovedInDatabaseException) {
        null
    }
    return if (oppositeEntity == null) {
        EntityIterableBase.EMPTY
    } else {
        findLinks(entityType, oppositeEntity, linkName)
    }
}