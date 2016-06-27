package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.dbFilter
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

class EntityProperty() : Named() {
    var type: PropertyType = PropertyType()
    var value: String? = null
}

class EntityLink() : Named() {
    var typeId: Int = 0
    var type: String? = null
    var label: String? = null
    var entityId: Long = 0
}

class EntityBlob() : Named() {
    var blobSize: Long = 0
}

class EntityView : BaseVO() {
    var type: String? = null
    var label: String? = null
    var typeId: String? = null
    var properties: List<EntityProperty> = emptyList()
    var links: List<EntityLink> = emptyList()
    var blobs: List<EntityBlob> = emptyList()
}

open class EntityType() : Named() {
    var id: String? = null
}

open class SearchPager(val items: Array<EntityView>, val totalCount: Long)


open class ChangeSummarySection<T> {

    var added: List<T> = ArrayList()
    var deleted: List<T> = ArrayList()
    var modified: List<T> = ArrayList()

}

class PropertiesSection : ChangeSummarySection<EntityProperty>()
class LinksSection : ChangeSummarySection<EntityLink>()
class BlobsSection : ChangeSummarySection<EntityBlob>()

class ChangeSummary {

    var properties: PropertiesSection = PropertiesSection()
    var links: LinksSection = LinksSection()

}

class DB {
    var location: String? = null
    var key: String? = null
    var title: String? = null
    var isOpened: Boolean = true
    var uuid: String = UUID.randomUUID().toString()
}

class DBSummary {
    var location: String? = null
    var key: String? = null
    var title: String? = null
    var recent: List<DB> = Databases.allRecent() //TODO: remove this property
    var types: List<EntityType> = emptyList()
}

class AppConfig {
    var opened = Databases.allOpened()
    var recent = Databases.allRecent()
    var current: DB? = Databases.current()
}

fun <T : Named> T.withName(name: String): T {
    this.name = name;
    return this;
}
