package jetbrains.xodus.browser.web

import org.junit.Assert
import org.junit.Test

class IndexHTMLTest : TestSupport() {

    private val frontendResource by lazy { retrofit.create(FrontendApi::class.java) }

    @Test
    fun `fallback for test html should work`() {
        val response = frontendResource.get().execute()
        Assert.assertTrue(response.body()!!.string().startsWith("<!DOCTYPE html>"))
    }
}