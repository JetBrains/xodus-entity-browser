package com.lehvolk.xodus.web


import com.lehvolk.xodus.web.search.UIPropertyTypes
import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl

fun Entity.asView(): EntityView {
    return asLightView().apply {
        blobs = blobNames.map { blobView(it) }
        links = linkNames.asSequence().map { linkView(it) }.toList()
    }
}

fun Entity.asLightView(): EntityView {
    return EntityView(
            id = id.toString(),
            typeId = id.typeId.toString(),
            label = label,
            type = type,

            properties = propertyNames.map { propertyView(it) }
    )
}

fun Entity.linkView(name: String, skip: Int = 0, top: Int = 100): LinkPager {
    val links = getLinks(name)
    return LinkPager(
            name = name,
            skip = skip,
            top = top,
            totalSize = links.size(),
            entities = links.asSequence().drop(skip).take(top).map { link ->
                val lightVO = link.asLightView()
                val linkId = link.id
                EntityLink(
                        name = name,
                        typeId = linkId.typeId,
                        entityId = linkId.localId,
                        label = lightVO.label,
                        type = lightVO.type
                )
            }.toList()
    )
}


private fun Entity.blobView(name: String): EntityBlob {
    return EntityBlob(
            name = name,
            blobSize = getBlobSize(name)
    )
}

private fun Entity.propertyView(name: String): EntityProperty {
    val value = this.getProperty(name)
    val entity = this
    val store = entity.store as PersistentEntityStoreImpl
    val clazz: Class<*> = if (value != null) {
        val propertyType = store.propertyTypes.getPropertyType(value.javaClass)
        propertyType.clazz
    } else {
        String::class.java
    }
    val typeVO = PropertyType(
            readonly = !UIPropertyTypes.isSupported(clazz),
            clazz = clazz.name,
            displayName = clazz.simpleName
    )
    return EntityProperty(
            name = name,
            type = typeVO,
            value = value2string(value)
    )
}

fun String.asEntityType(store: PersistentEntityStoreImpl): EntityType {
    val typeId = store.getEntityTypeId(store.currentTransaction!!, this, false)
    return EntityType(typeId.toString(), this)
}

fun EntityProperty.string2value(): Comparable<*>? {
    if (value == null) {
        return null
    }
    try {
        val clazz = type.clazz
        val type = UIPropertyTypes.uiTypeOf<Comparable<*>>(clazz)
        return type.toValue(this.value)
    } catch (e: RuntimeException) {
        throw InvalidFieldException(e, name, value!!)
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

val Entity.label: String
    get() {
        return "$type[${toIdString()}]"
    }
