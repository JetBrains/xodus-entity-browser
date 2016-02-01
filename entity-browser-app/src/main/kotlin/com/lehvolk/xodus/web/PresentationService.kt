package com.lehvolk.xodus.web


object PresentationService {

    fun labelOf(typeId: Int, type: String): (EntityView) -> EntityView {
        return { entity ->
            var labelFormat = labelFormat(typeId)
            labelFormat = labelFormat.replace(wrap("type").toRegex(), type)
            entity.label = doFormat(labelFormat, entity)
            entity
        }
    }

    fun labelFormat(typeId: Int): String {
        return "{{type}}[{{id}}]"
    }

    private fun doFormat(format: String, entityVO: EntityView): String {
        var formatted = format.replace(wrap("id").toRegex(), entityVO.id.toString())
        for (property in entityVO.properties) {
            formatted = formatted.replace(wrap("entity." + property).toRegex(), property.value.toString())
            if (!formatted.contains("\\{\\{\\.*\\}\\}")) {
                return formatted
            }
        }
        return formatted
    }

    private fun wrap(key: String): String {
        return "\\{\\{$key\\}\\}"
    }

}
