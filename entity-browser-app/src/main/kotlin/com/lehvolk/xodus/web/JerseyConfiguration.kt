package com.lehvolk.xodus.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.lehvolk.xodus.web.search.SearchQueryException
import javax.ws.rs.ClientErrorException
import javax.ws.rs.InternalServerErrorException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ContextResolver
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


interface WithMessage {
    val msg: String
}

class EntityNotFoundException(cause: Throwable, val typeId: Int, val entityId: Long) : RuntimeException(cause), WithMessage {

    override val msg: String = "Error getting entity by type '$typeId' and id='$entityId'"
}

class InvalidFieldException(cause: Throwable, val fieldName: String, val fieldValue: String) : RuntimeException(cause), WithMessage {

    override val msg: String = "invalid value of property '$fieldName': '$fieldValue'"
}

class XodusRestServerException(cause: Throwable) : InternalServerErrorException(cause), WithMessage {
    override val msg = "Internal server error. Getting " +
            cause.javaClass.name + ": " +
            cause.message + ". Check server log for more details."
}

class XodusRestClientException(cause: Throwable) : RuntimeException(cause), WithMessage {
    override val msg = cause.message ?: "UFO error"
}

abstract class AbstractMapper<T> : ExceptionMapper<T>
    where T : WithMessage, T : Throwable {

    abstract val status: Response.Status

    override fun toResponse(exception: T): Response? {
        val vo = ServerError(exception.msg)
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(vo).build()
    }

}

@Provider
class ServerExceptionMapper : AbstractMapper<XodusRestServerException>() {

    override val status: Response.Status
        get() = Response.Status.INTERNAL_SERVER_ERROR
}

@Provider
class EntityExceptionMapper : AbstractMapper<EntityNotFoundException>() {

    override val status: Response.Status
        get() = Response.Status.NOT_FOUND

}

@Provider
class ValidationErrorMapper : AbstractMapper<InvalidFieldException>() {

    override val status: Response.Status
        get() = Response.Status.BAD_REQUEST

}

@Provider
class JacksonConfigurator : ContextResolver<ObjectMapper> {

    val mapper: ObjectMapper = ObjectMapper().apply {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun getContext(type: Class<*>): ObjectMapper {
        return mapper
    }

}

@Provider
class ClientExceptionMapper : AbstractMapper<XodusRestClientException>() {
    override val status = Response.Status.BAD_REQUEST
}