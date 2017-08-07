package com.lehvolk.xodus.web


import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl

fun Entity.asView(): EntityView {
    val entity = this
    return entity.asLightView().apply {
        blobs = entity.blobNames.map { entity.blobView(it) }
        links = entity.linkNames.asSequence().map { entity.linkView(it) }.toList()
    }
}

fun Entity.asLightView(): EntityView {
    val entity = this
    val store = entity.store as PersistentEntityStoreImpl
    return EntityView().apply {
        this.id = entity.id.localId.toString()
        this.properties = entity.propertyNames.map { entity.propertyView(it) }
        val typeId = entity.id.typeId
        val entityType = store.getEntityType(store.currentTransaction!!, typeId)
        labeled(entityType)
        this.typeId = typeId.toString()
        this.type = entityType
    }
}

fun Entity.linkView(name: String, skip: Int = 0, top: Int = 100): LinkPager {
    val entity = this
    val links = entity.getLinks(name)
    return LinkPager().withName(name).apply {
        this.skip = skip
        this.top = top
        this.totalSize = links.size()
        this.entities = links.asSequence().drop(skip).take(top).map { link ->
            EntityLink().withName(name).apply {
                val lightVO = link.asLightView()
                val linkId = link.id
                typeId = linkId.typeId
                entityId = linkId.localId
                label = lightVO.label
                type = lightVO.type
            }
        }.toList()
    }
}


private fun Entity.blobView(name: String): EntityBlob {
    val entity = this
    return EntityBlob().withName(name).apply {
        this.blobSize = entity.getBlobSize(name)
    }
}

private fun Entity.propertyView(name: String): EntityProperty {
    val value = this.getProperty(name)
    val entity = this
    val store = entity.store as PersistentEntityStoreImpl
    val typeVO = PropertyType().apply {
        val clazz: Class<*> = if (value != null) {
            val propertyType = store.propertyTypes.getPropertyType(value.javaClass)
            propertyType.clazz
        } else {
            String::class.java
        }
        this.readonly = !UIPropertyTypes.isSupported(clazz)
        this.clazz = clazz.name
        this.displayName = clazz.simpleName
    }
    return EntityProperty().withName(name).apply {
        this.type = typeVO
        this.value = value2string(value)
    }
}

fun String.asEntityType(store: PersistentEntityStoreImpl): EntityType {
    val name = this
    return EntityType().withName(name).apply {
        val typeId = store.getEntityTypeId(store.currentTransaction!!, name, false)
        id = typeId.toString()
    }
}

fun EntityProperty.string2value(): Comparable<*>? {
    if (this.value == null) {
        return null
    }
    try {
        val clazz = this.type.clazz
        val type = UIPropertyTypes.uiTypeOf<Comparable<*>>(clazz!!)
        return type.toValue(this.value)
    } catch (e: RuntimeException) {
        throw InvalidFieldException(e, this.name!!, this.value!!)
    }
}

fun <T : Comparable<*>> value2string(value: T?): String? {
    if (value == null) {
        return null
    }
    try {
        val clazz = value.javaClass
        val type = UIPropertyTypes.uiTypeOf<T>(clazz)
        return type.toString(value)
    } catch (e: RuntimeException) {
        throw IllegalStateException(e)
    }

}


private val labelFormat = "{{type}}[{{id}}]"

fun EntityView.labeled(type: String) {

    fun wrap(key: String): String {
        return "\\{\\{$key\\}\\}"
    }

    fun doFormat(format: String, entityVO: EntityView): String {
        var formatted = format.replace(wrap("id").toRegex(), entityVO.id.toString())
        for (property in entityVO.properties) {
            formatted = formatted.replace(wrap("entity." + property).toRegex(), property.value.toString())
            if (!formatted.contains("\\{\\{\\.*\\}\\}")) {
                return formatted
            }
        }
        return formatted
    }

    var labelFormat = labelFormat
    labelFormat = labelFormat.replace(wrap("type").toRegex(), type)
    label = doFormat(labelFormat, this)
}
