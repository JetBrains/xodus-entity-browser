package com.lehvolk.xodus.web

import java.util.*

class ServerError(val msg: String)

open class BaseVO {
    var id: String? = null
}

open class Named {
    var name: String? = null
}

open class PropertyType {
    var readonly: Boolean = false
    var clazz: String? = null
    var displayName: String? = null
}

class EntityProperty : Named() {
    var type: PropertyType = PropertyType()
    var value: String? = null
}

class EntityLink : Named() {
    var typeId: Int = 0
    var type: String? = null
    var label: String? = null
    var entityId: Long = 0
}

class LinkPager : Named() {
    var skip = 0
    var top = 100
    var totalSize = 0L
    var entities: List<EntityLink> = emptyList()
}

class EntityBlob : Named() {
    var blobSize: Long = 0
}

class EntityView : BaseVO() {
    var type: String? = null
    var label: String? = null
    var typeId: String? = null
    var properties: List<EntityProperty> = emptyList()
    var links: List<LinkPager> = emptyList()
    var blobs: List<EntityBlob> = emptyList()
}

open class EntityType : Named() {
    var id: String? = null
}

open class SearchPager(val items: Array<EntityView>, val totalCount: Long)

open class ChangeSummaryAction<T> : Named() {
    var newValue: T? = null
}

open class PropertiesChangeSummaryAction : ChangeSummaryAction<EntityProperty>()
open class LinkChangeSummaryAction : ChangeSummaryAction<EntityLink>(){
    var oldValue: EntityLink? = null
    var totallyRemoved: Boolean = false
}
open class BlobChangeSummaryAction : ChangeSummaryAction<EntityBlob>()

class ChangeSummary {
    var properties = listOf<PropertiesChangeSummaryAction>()
    var links = listOf<LinkChangeSummaryAction>()
    var blobs = listOf<BlobChangeSummaryAction>()
}

open class DBSummary {
    var location: String? = null
    var key: String? = null
    var title: String? = null
    var isOpened: Boolean = true
    var uuid: String = UUID.randomUUID().toString()
}

fun <T : Named> T.withName(name: String): T {
    this.name = name;
    return this;
}
