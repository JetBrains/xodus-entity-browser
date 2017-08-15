package com.lehvolk.xodus.web.search

import jetbrains.exodus.entitystore.*
import jetbrains.exodus.entitystore.iterate.EntityIterableBase
import java.util.regex.Pattern


sealed class SearchTerm(val name: String) {
    companion object {
        private val RANGE_PATTERN = Pattern.compile("\\[\\s*(\\d*)\\s*,\\s*(\\d*)\\s*]")
        private val ENTITY_ID = Pattern.compile("^(?<type>[a-zA-z]\\w*)\\[(?<id>\\d+)]$")
        private val NULL_TOKEN = "null"

        @JvmStatic
        fun from(rawProperty: String, rawOperand: String, rawValue: String): SearchTerm {
            val property = prepare(rawProperty)
            val operand = prepare(rawOperand)
            val value = prepare(rawValue)

            return if (property.startsWith("@")) {
                parseAsLink(property.removePrefix("@"), operand, value)
            } else {
                parseAsProperty(property, operand, rawValue)
            }
        }

        private fun parseAsLink(linkName: String, operand: String, value: String): SearchTerm {
            if (operand != "=") {
                throw SearchQueryException("The [$operand] operand is not supported for links. Only the equality [=] operand is supported")
            }

            if (value == NULL_TOKEN) {
                return LinkSearchTerm.nullValue(linkName)
            }

            val entityIdMatcher = ENTITY_ID.matcher(value)
            if (!entityIdMatcher.matches()) {
                throw SearchQueryException("Link value should have the \"MyType[15]\" pattern but was \"$value\"")
            }

            return LinkSearchTerm.value(linkName, entityIdMatcher.group("type"), entityIdMatcher.group("id").toLong())
        }

        private fun parseAsProperty(propertyName: String, operand: String, rawValue: String): SearchTerm {
            val value = prepare(rawValue)
            val isNullValue = rawValue == NULL_TOKEN
            return if (isNullValue) {
                if (operand != "=") throw SearchQueryException("Only the equality [=] operand is supported for comparing a property value with null")
                PropertyValueSearchTerm(propertyName, null)
            } else {
                val rangeMatcher = RANGE_PATTERN.matcher(value)
                if (rangeMatcher.matches()) {
                    if (operand != "=") throw SearchQueryException("Only the equality [=] operand is supported for comparing a property value with a range")
                    PropertyRangeSearchTerm(propertyName, rangeMatcher.group(1).toLong(), rangeMatcher.group(2).toLong())
                } else {
                    when (operand) {
                        "~" -> PropertyLikeSearchTerm(propertyName, value)
                        else -> PropertyValueSearchTerm(propertyName, value)
                    }
                }
            }
        }

        private fun prepare(value: String): String {
            if (value.length <= 1) {
                return value
            }

            val first = value[0]
            val last = value[value.length - 1]
            return if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                removeDoubleQuotes(value.substring(1, value.length - 1))
            } else {
                removeDoubleQuotes(value)
            }
        }

        private fun removeDoubleQuotes(value: String): String {
            if (value.length <= 1) {
                return value
            }

            return value.replace("''", "'")
        }
    }

    abstract fun search(txn: StoreTransaction, entityType: String, entityTypeId: Int): EntityIterable

    protected val String.isIdProperty: Boolean get() = "id".equals(this, ignoreCase = true)
}

/**
 * propertyName=[1,5]
 */
class PropertyRangeSearchTerm(name: String, val start: Long, val end: Long) : SearchTerm(name) {
    override fun search(txn: StoreTransaction, entityType: String, entityTypeId: Int): EntityIterable {
        return txn.fullSearch(entityType, name, start, end).let {
            if (name.isIdProperty) {
                it.union(txn.findIds(entityType, start, end))
            } else {
                it
            }
        }
    }

    private fun StoreTransaction.fullSearch(type: String, property: String, start: Long, end: Long): EntityIterable {
        return UIPropertyTypes.rangeTree.find(this, type, property, start.toString(), end.toString())
    }
}

/**
 * propertyName~Jo
 */
class PropertyLikeSearchTerm(name: String, val value: String) : SearchTerm(name) {
    override fun search(txn: StoreTransaction, entityType: String, entityTypeId: Int): EntityIterable {
        return txn.findStartingWith(entityType, name, value)
    }
}

/**
 * - id=18
 * - propertyName=John
 * - propertyName=null
 */
class PropertyValueSearchTerm(name: String, val value: String?) : SearchTerm(name) {
    override fun search(txn: StoreTransaction, entityType: String, entityTypeId: Int): EntityIterable {
        return if (value != null) {
            val localId = value.toLongOrNull()
            txn.fullSearch(entityType, name, value).let {
                if (name.isIdProperty && localId != null) {
                    val entityId = PersistentEntityId(entityTypeId, localId)
                    it.union(txn.getSingletonIterable(txn.getEntity(entityId)))
                } else {
                    it
                }
            }
        } else {
            txn.getAll(entityType).minus(txn.findWithProp(entityType, name))
        }
    }

    private fun StoreTransaction.fullSearch(type: String, property: String, value: String): EntityIterable {
        return UIPropertyTypes.tree.map { it.find(this, type, property, value) }.reduce { it1, it2 -> it1.union(it2) }
    }
}

/**
 * - @linkName=MyType[34]
 * - @linkName=null
 */
class LinkSearchTerm private constructor(name: String, val oppositeEntityTypeName: String?, val oppositeEntityLocalId: Long?) : SearchTerm(name) {
    companion object {
        fun nullValue(linkName: String) = LinkSearchTerm(linkName, null, null)
        fun value(linkName: String, oppositeEntityTypeName: String, oppositeEntityLocalId: Long) = LinkSearchTerm(linkName, oppositeEntityTypeName, oppositeEntityLocalId)
    }

    override fun search(txn: StoreTransaction, entityType: String, entityTypeId: Int): EntityIterable {
        return if (oppositeEntityTypeName == null || oppositeEntityLocalId == null) {
            txn.getAll(entityType).minus(txn.findWithLinks(entityType, name))
        } else if (txn is PersistentStoreTransaction) {
            val oppositeTypeId = txn.store.getEntityTypeId(txn, oppositeEntityTypeName, false)
            if (oppositeTypeId < 0) {
                EntityIterableBase.EMPTY
            } else {
                val oppositeEntityId = txn.toEntityId("$oppositeTypeId-$oppositeEntityLocalId")
                val oppositeEntity = try {
                    txn.getEntity(oppositeEntityId)
                } catch (ex: EntityRemovedInDatabaseException) {
                    null
                }
                return if (oppositeEntity == null) {
                    EntityIterableBase.EMPTY
                } else {
                    txn.findLinks(entityType, oppositeEntity, name)
                }
            }
        } else {
            throw RuntimeException("Can't search by a link. The transaction should be persistent")
        }
    }
}