package jetbrains.xodus.browser.web

import jetbrains.exodus.bindings.ComparableSet
import jetbrains.exodus.bindings.ComparableValueType
import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.youtrackdb.YTDBComparableSet
import jetbrains.xodus.browser.web.search.UIPropertyTypes

fun Entity.asView(): EntityView {
    return asLightView().apply {
        blobs = blobNames.map { blobView(it) }
        links = linkNames.asSequence().map { linkView(it) }.toList()
    }
}

fun Entity.asLightView(): EntityView {
    return EntityView(
            id = id.toString(),
            typeId = id.typeId,
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
            totalCount = links.size(),
            entities = links.asSequence().drop(skip).take(top).map { link ->
                val linkId = link.id
                val exists = link.exists()
                EntityLink(
                        id = link.id.toString(),
                        name = name,
                        typeId = linkId.typeId,
                        label = link.label,
                        type = link.type,
                        notExists = !exists
                )
            }.toList()
    )
}

fun Entity.exists(): Boolean {
    return try {
        val currentTransaction = store.currentTransaction!!
        currentTransaction.getEntity(id)
        true
    } catch (e: RuntimeException) {
        false
    }
}


private fun Entity.blobView(name: String): EntityBlob {
    return EntityBlob(
            name = name,
            blobSize = getBlobSize(name)
    )
}

private fun Entity.propertyView(name: String): EntityProperty {
    val value = this.getProperty(name)
    val clazz: Class<*> = if (value != null) {
        ComparableValueType.getPredefinedType(getValueJavaClass(value)).clazz
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

fun EntityProperty.string2value(): Comparable<*>? {
    if (value == null) {
        return null
    }
    try {
        val clazz = type.clazz
        val type = UIPropertyTypes.uiTypeOf<Comparable<*>>(clazz)
        val typedValue = type.toValue(this.value)
        return when (typedValue) {
            is ComparableSet<*> -> YTDBComparableSet(typedValue.toMutableSet())
            else -> typedValue
        }
    } catch (e: RuntimeException) {
        throw InvalidFieldException(e, name, value!!)
    }
}

fun <T : Comparable<*>> value2string(value: T?): String? {
    if (value == null) {
        return null
    }
    try {
        val clazz = getValueJavaClass(value)
        val type = UIPropertyTypes.uiTypeOf<T>(clazz)
        return type.toString(value)
    } catch (e: RuntimeException) {
        throw IllegalStateException(e)
    }

}

@Suppress("UNCHECKED_CAST")
private fun <T : Comparable<*>> getValueJavaClass(value: T): Class<T> {
    val clazz = when (value) {
        is YTDBComparableSet<*> -> ComparableSet::class.java
        else -> value.javaClass
    }
    return (clazz) as Class<T>
}

val Entity.label: String
    get() {
        return "$type[${toIdString()}]"
    }
