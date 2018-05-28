package jetbrains.xodus.browser.web.resources

import spark.resource.AbstractFileResolvingResource
import spark.resource.ClassPathResourceHandler
import spark.staticfiles.StaticFilesConfiguration
import spark.utils.GzipUtils
import spark.utils.IOUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ContextAwareStaticFilesConfiguration(private val context: String) : StaticFilesConfiguration() {


    private val handler = object : ClassPathResourceHandler("/static") {

        override fun getResource(path: String?): AbstractFileResolvingResource {
            if (context.isBlank()) {
                return super.getResource(path)
            }
            return super.getResource(path?.removePrefix("/$context"))
        }
    }

    override fun consume(httpRequest: HttpServletRequest,
                         httpResponse: HttpServletResponse): Boolean {
        try {
            val resource = handler.getResource(httpRequest)
            resource.inputStream.use { inputStream ->
                GzipUtils.checkAndWrap(httpRequest, httpResponse, false).use { wrappedOutputStream ->
                    IOUtils.copy(inputStream, wrappedOutputStream)
                }
                return true
            }
        } catch (e: Exception) {
            return false
        }
    }
}
