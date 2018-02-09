package com.lehvolk.xodus.web.resources

import com.fasterxml.jackson.core.JsonProcessingException
import com.lehvolk.xodus.web.*
import mu.KLogging
import spark.kotlin.Http
import spark.kotlin.RouteHandler


class Entities : Resource, ResourceSupport {

    companion object : KLogging()

    private val RouteHandler.entityId: String
        get() {
            return request.params("entityId")
        }

    override fun registerRouting(http: Http) {
        http.service.path("/api/dbs/:uuid/entities") {
            http.safeGet {
                val id = request.queryParams("id").toInt()
                val q = request.queryParams("q")
                val offset = (request.queryParams("offset") ?: "0").toInt()
                val pageSize = (request.queryParams("pageSize") ?: "0").toInt()
                logger.debug {
                    "searching entities by typeId: $id, q [$q] with offset = $offset and pageSize = $pageSize"
                }
                if (offset < 0 || pageSize < 0) {
                    response.status(400)
                }
                storeService.searchType(id, q, offset, if (pageSize == 0) 50 else Math.min(pageSize, 1000))
            }

            http.safeGet("/:entityId") {
                logger.debug { "getting entity by entity id '$entityId'" }
                storeService.getEntity(entityId)
            }

            http.safePut<ChangeSummary>("/:entityId") {
                logger.debug { "updating entity for '$entityId'. ChangeSummary: ${toString(it)}" }
                storeService.updateEntity(entityId, it)
            }

            http.safePost<ChangeSummary> {
                val typeId = request.queryParams("typeId").toInt()
                logger.debug { "creating entity for '$entityId'. ChangeSummary: ${toString(it)}" }
                storeService.newEntity(typeId, it)
            }

            http.safeDelete("/:entityId") {
                logger.debug { "deleting '$entityId'" }
                storeService.deleteEntity(entityId)
            }

            http.get("/:entityId/blob/:blobName") {
                logger.debug { "getting entity by entity id '$entityId'" }
                response.header("content-type", "application/octet-stream;charset=utf-8")
                storeService.getBlob(entityId, request.params("blobName"),
                        response.raw().outputStream)
            }

            http.safeGet("/:entityId/links/:linkName") {
                val linkName = request.params("linkName")
                val offset = (request.queryParams("offset") ?: "0").toInt()
                val pageSize = (request.queryParams("pageSize") ?: "0").toInt()
                logger.debug {
                    "searching entities by typeId: $entityId, linkName [$linkName] with offset = $offset and pageSize = $pageSize"
                }
                if (offset < 0 || pageSize < 0) {
                    response.status(400)
                }
                storeService.searchEntity(entityId, linkName, offset, if (pageSize == 0) 100 else Math.min(pageSize, 1000))
            }
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