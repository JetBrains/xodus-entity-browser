package com.lehvolk.xodus.web

import java.util.*

open class BaseVO {
    var id: String? = null
}

open class Named {
    var name: String? = null
}

open class PropertyType(
        var readonly: Boolean = false,
        var clazz: String? = null,
        var displayName: String? = null
)

class EntityProperty(
        var type: PropertyType = PropertyType(),
        var value: String? = null
) : Named()

class EntityLink(
        var typeId: Int = 0,
        var type: String? = null,
        var label: String? = null,
        var entityId: Long = 0
) : Named()

class LinkPager(
        var skip: Int = 0,
        var top: Int = 100,
        var totalSize: Long = 0L,
        var entities: List<EntityLink> = emptyList()
) : Named()

class EntityBlob(var blobSize: Long = 0) : Named()

class EntityView(
        var type: String? = null,
        var label: String? = null,
        var typeId: String? = null,
        var properties: List<EntityProperty> = emptyList(),
        var links: List<LinkPager> = emptyList(),
        var blobs: List<EntityBlob> = emptyList()
) : BaseVO()

open class EntityType : Named() {
    var id: String? = null
}

open class SearchPager(val items: Array<EntityView>, val totalCount: Long)

open class ChangeSummaryAction<T>(
        var newValue: T? = null
) : Named()

open class PropertiesChangeSummaryAction : ChangeSummaryAction<EntityProperty>()
open class LinkChangeSummaryAction(
        var oldValue: EntityLink? = null,
        var totallyRemoved: Boolean = false
) : ChangeSummaryAction<EntityLink>()

open class BlobChangeSummaryAction : ChangeSummaryAction<EntityBlob>()

class ChangeSummary(
        var properties: List<PropertiesChangeSummaryAction> = listOf(),
        var links: List<LinkChangeSummaryAction> = listOf(),
        var blobs: List<BlobChangeSummaryAction> = listOf()
)

data class DBSummary(
        var location: String,
        var key: String,
        var isOpened: Boolean = true,
        var uuid: String = UUID.randomUUID().toString()
)

fun <T : Named> T.withName(name: String): T = apply {
    this.name = name
}
