package com.lehvolk.xodus.web.resources

import com.fasterxml.jackson.core.JsonProcessingException
import com.lehvolk.xodus.web.*
import mu.KLogging
import spark.kotlin.Http
import spark.kotlin.RouteHandler


class Entities : Resource, ResourceSupport {

    companion object : KLogging()

    private val RouteHandler.entityId: ApiEntityId
        get() {
            val id = request.params("entityId")
            val parts = id.split("-").also {
                if (it.size != 2) {
                    throw IllegalArgumentException()
                }
            }
            return ApiEntityId(parts[0].toInt(), parts[1].toLong())
        }

    override val prefix = "/api/dbs/:uuid/entities"

    override fun registerRouting(http: Http) {

        http.safeGet(prefixed()) {
            val id = request.queryParams("id").toInt()
            val term = request.queryParams("term")
            val offset = (request.queryParams("offset") ?: "0").toInt()
            val pageSize = (request.queryParams("pageSize") ?: "0").toInt()
            logger.debug {
                "searching entities by typeId: $id, term [$term] with offset = $offset and pageSize = $pageSize"
            }
            if (offset < 0 || pageSize < 0) {
                response.status(400)
            }
            storeService.searchType(id, term, offset, if (pageSize == 0) 50 else Math.min(pageSize, 1000))
        }

        http.safeGet(prefixed(":entityId")) {
            val entityId = entityId
            logger.debug { "getting entity by entity id '$entityId'" }
            storeService.getEntity(entityId.typeId, entityId.localId)
        }

        http.safePut<ChangeSummary>(prefixed(":entityId")) {
            val entityId = entityId
            logger.debug { "updating entity for '$entityId'. ChangeSummary: ${toString(it)}" }
            storeService.updateEntity(entityId.typeId, entityId.localId, it)
        }

        http.safePost<ChangeSummary>(prefixed(":typeId")) {
            val typeId = request.queryParams("typeId").toInt()
            logger.debug { "creating entity for '$entityId'. ChangeSummary: ${toString(it)}" }
            storeService.newEntity(typeId, it)
        }

        http.safeDelete(prefixed(":entityId")) {
            val entityId = entityId
            logger.debug { "deleting '$entityId'" }
            storeService.deleteEntity(entityId.typeId, entityId.localId)
        }

        http.get(prefixed(":entityId/blob/:blobName")) {
            val entityId = entityId
            logger.debug { "getting entity by entity id '$entityId'" }
            response.header("Content Type", "application/octet-stream;charset=utf-8")
            storeService.getBlob(entityId.typeId, entityId.localId, request.params("blobName"),
                    response.raw().outputStream)
        }

        http.safeGet(prefixed(":entityId/links/:linkName")) {
            val entityId = entityId
            val linkName = request.params("linkName")
            val offset = (request.queryParams("offset") ?: "0").toInt()
            val pageSize = (request.queryParams("pageSize") ?: "0").toInt()
            logger.debug {
                "searching entities by typeId: $entityId, linkName [$linkName] with offset = $offset and pageSize = $pageSize"
            }
            if (offset < 0 || pageSize < 0) {
                response.status(400)
            }
            storeService.searchEntity(entityId.typeId, entityId.localId, linkName, offset, if (pageSize == 0) 100 else Math.min(pageSize, 1000))
        }
    }

    private fun toString(vo: ChangeSummary): String {
        return try {
            mapper.writeValueAsString(vo)
        } catch (e: JsonProcessingException) {
            "Error converting vo to string. Check the server state this error should never happened"
        }
    }

}

data class ApiEntityId(val typeId: Int, val localId: Long)