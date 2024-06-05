package jetbrains.xodus.browser.web.servlets

import com.google.gson.Gson
import jetbrains.xodus.browser.web.ApplicationSummary
import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.PersistentWebApplication
import jetbrains.xodus.browser.web.WebApplication
import jetbrains.xodus.browser.web.db.PersistentDatabaseService
import java.io.*
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse



/**
 * The ApiServlet class handles HTTP requests and generates the appropriate response based on the requested path.
 *
 * /api/dbs
 */
class ApiServlet : HttpServlet() {
    val gson = Gson()


    /**
     * This method handles the HTTP request and generates the appropriate response based on the requested path.
     *
     * @param request The HttpServletRequest object representing the incoming request.
     * @param response The HttpServletResponse object representing the response to be sent back to the client.
     * @throws ServletException If any servlet-specific error occurs.
     * @throws IOException If any I/O error occurs.
     */
    @Throws(ServletException::class, IOException::class)
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        val path = request.contextPath
        handleApiDbs(request, response)
    }

    fun handleApiDbs(request: HttpServletRequest, response: HttpServletResponse) {
        val path = request.servletPath
        val pathSequence: List<String> = path.split("/")
        when {
            path == "/api/dbs" -> {handleOnlyDBs(request, response)}
            pathSequence.size == 4 -> {
                // /api/dbs/{uuid}
                val uuid = pathSequence[3]
                handleOnlyDBsWithUuid(request, response, uuid)
            }

            pathSequence.size == 5 && pathSequence[4] == "types" -> {
                // /api/dbs/{uuid}/types
                val uuid = pathSequence[3]
                handleOnlyDBsTypes(request, response, uuid)
            }

            pathSequence.size == 5 && pathSequence[4] == "entities" -> {
                // /api/dbs/{uuid}/entities
                val uuid = pathSequence[3]
                handleOnlyDBsEntities(request, response, uuid)
            }
            pathSequence.size == 6 && pathSequence[4] == "entities" -> {
                // /api/dbs/{uuid}/entities/{entityId}
                val uuid = pathSequence[3]
                val entityId = pathSequence[5]
                handleOnlyDBsEntitiesWithId(request, response, uuid, entityId)
            }
            pathSequence.size == 8 && pathSequence[4] == "entities" && pathSequence[6]=="blob"-> {
                // /api/dbs/{uuid}/entities/{entityId}/blob/{blobName}
                val uuid = pathSequence[3]
                val entityId = pathSequence[5]
                val blobName = pathSequence[7]
                handleOnlyDBsEntitiesBlob(request, response, uuid, entityId, blobName)
            }
            pathSequence.size == 8 && pathSequence[4] == "entities" && pathSequence[6]=="blobString"-> {
                // /api/dbs/{uuid}/entities/{entityId}/blobString/{blobName}
                val uuid = pathSequence[3]
                val entityId = pathSequence[5]
                val blobName = pathSequence[7]
                handleOnlyDBsEntitiesBlobString(request, response, uuid, entityId, blobName)
            }
            pathSequence.size == 8 && pathSequence[4] == "entities" && pathSequence[6]=="links"-> {
                // /api/dbs/{uuid}/entities/{entityId}/links/{blobName}
                val uuid = pathSequence[3]
                val entityId = pathSequence[5]
                val linkName = pathSequence[7]
                handleOnlyDBsEntitiesLink(request, response, uuid, entityId, linkName)
            }
            else -> {
                notAllowed(request, response)
            }

        }
    }






    fun handleOnlyDBs(request: HttpServletRequest, response: HttpServletResponse) {
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                val applicationSummary = ApplicationSummary(
                    isReadonly = webApp.isReadonly,
                    dbs = webApp.databaseService.all().map { it.secureCopy() })
                val jsonString = gson.toJson(applicationSummary)

                response.contentType = "application/json"
                response.writer.println(jsonString)
            }

            "POST" -> {
                val newSummary = gson.fromJson(getBody(request), DBSummary::class.java)
                if (webApp.isReadonly) {
                    throw BadRequestException("application is R/O")
                }
                val summary = webApp.databaseService.add(newSummary)
                if (newSummary.isOpened) {
                    webApp.tryStartServices(summary, false)
                }
                val applicationSummary = webApp.databaseService.find(summary.uuid)?.secureCopy() ?: throw NotFoundException()
                val jsonString = gson.toJson(applicationSummary)
                response.writer.println(jsonString)
            }
            "DELETE" -> {
                //TODO
            }
            "POST" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    fun handleOnlyDBsWithUuid(request: HttpServletRequest, response: HttpServletResponse, uuid: String) {
        val webApp = resources.webApp
        when (request.method) {
            "DELETE" -> {
                //TODO
            }
            "POST" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }
    private fun handleOnlyDBsTypes(request: HttpServletRequest, response: HttpServletResponse, uuid: String) {
        // api/dbs/{uuid}/types
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            "POST" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    private fun handleOnlyDBsEntities(request: HttpServletRequest, response: HttpServletResponse, uuid: String) {
        // api/dbs/{uuid}/entities
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            "DELETE" -> {
                //TODO
            }
            "POST" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    private fun handleOnlyDBsEntitiesWithId(
        request: HttpServletRequest,
        response: HttpServletResponse,
        uuid: String,
        entityId: String
    ) {
        // api/dbs/{uuid}/entities/{entityId}
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            "PUT" -> {
                //TODO
            }
            "DELETE" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    private fun handleOnlyDBsEntitiesBlob(
        request: HttpServletRequest,
        response: HttpServletResponse,
        uuid: String,
        entityId: String,
        blobName: String
    ) {
        // /api/dbs/{uuid}/entities/{entityId}/blob/{blobName}
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    private fun handleOnlyDBsEntitiesBlobString(
        request: HttpServletRequest,
        response: HttpServletResponse,
        uuid: String,
        entityId: String,
        blobName: String
    ) {
        // /api/dbs/{uuid}/entities/{entityId}/blobString/{blobName}
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }

    private fun handleOnlyDBsEntitiesLink(
        request: HttpServletRequest,
        response: HttpServletResponse,
        uuid: String,
        entityId: String,
        linkName: String
    ) {
        // /api/dbs/{uuid}/entities/{entityId}/links/{blobName}
        val webApp = resources.webApp
        when (request.method) {
            "GET" -> {
                //TODO
            }
            else -> notAllowed(request, response)
        }
    }


    private fun notFound(request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_NOT_FOUND
        response.writer.println("{ \"status\": \"failed\", \"message\":\"Page not found\"}")
    }

    private fun notAllowed(request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_METHOD_NOT_ALLOWED
        response.writer.println("{ \"status\": \"failed\", \"message\":\"Method is not allowed for this path\"}")
    }

    private fun DBSummary.secureCopy(): DBSummary {
        return copy(encryptionKey = null, encryptionIV = null, encryptionProvider = null)
    }
}

object resources {
    val webApp: WebApplication = PersistentWebApplication(PersistentDatabaseService()).also { it.start() }
}

fun getBody(request: HttpServletRequest): String {
    var body: String? = null
    val stringBuilder = StringBuilder()
    var bufferedReader: BufferedReader? = null

    try {
        val inputStream: InputStream? = request.inputStream
        if (inputStream != null) {
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val charBuffer = CharArray(128)
            var bytesRead = -1
            while ((bufferedReader.read(charBuffer).also { bytesRead = it }) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead)
            }
        } else {
            stringBuilder.append("")
        }
    } catch (ex: IOException) {
        // throw ex;
        return ""
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close()
            } catch (ex: IOException) {
            }
        }
    }

    body = stringBuilder.toString()
    return body
}