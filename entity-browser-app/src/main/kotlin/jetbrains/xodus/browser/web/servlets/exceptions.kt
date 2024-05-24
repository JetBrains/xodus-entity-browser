package jetbrains.xodus.browser.web.servlets

/**
 * Base exception to indicate that the request is not correct due to
 * wrong/missing request parameters, body content or header values.
 * Throwing this exception in a handler will lead to 400 Bad Request response
 * unless a custom [io.ktor.features.StatusPages] handler registered.
 */
public open class BadRequestException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * This exception means that the requested resource is not found.
 * HTTP status 404 Not found will be replied when this exception is thrown and not caught.
 * 404 status page could be configured by registering a custom [io.ktor.features.StatusPages] handler.
 */
public class NotFoundException(message: String? = "Resource not found") : Exception(message)