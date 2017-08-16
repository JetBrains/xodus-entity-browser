package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.*
import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService
import com.lehvolk.xodus.web.search.SearchQueryException
import org.slf4j.LoggerFactory
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo

const val DB_COOKIE = "entity-browser-database-uuid"

fun HttpServletResponse.switchTo(uuid: String = Databases.current().uuid) {
    Cookie(DB_COOKIE, uuid).apply {
        maxAge = Int.MAX_VALUE
        addCookie(this)
    }
}


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
abstract class ApplicationResource {

    internal val log = LoggerFactory.getLogger(javaClass)
    internal val configurator = JacksonConfigurator()

    @Context
    internal lateinit var args: UriInfo
    @Context
    internal lateinit var request: HttpServletRequest
    @Context
    internal lateinit var response: HttpServletResponse

    val storeService: StoreService get() = InjectionContexts.current.storeService
    val jobsService: JobsService get() = InjectionContexts.current.jobsService

    internal inline fun <R> safely(block: () -> R): R {
        try {
            return block()
        } catch (e: EntityNotFoundException) {
            log.error("getting entity failed", e)
            throw e
        } catch (e: InvalidFieldException) {
            log.error("error updating entity", e)
            throw e
        } catch (e: SearchQueryException) {
            log.warn("error executing '${request.method}' request for '${args.absolutePath}'", e)
            throw XodusRestClientException(e)
        } catch(e: Exception) {
            log.error("error executing '${request.method}' request for '${args.absolutePath}'", e)
            throw XodusRestServerException(e)
        }
    }

}