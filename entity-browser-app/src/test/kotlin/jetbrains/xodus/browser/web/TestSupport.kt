package jetbrains.xodus.browser.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environments
import jetbrains.xodus.browser.web.db.PersistentDatabaseService
import mu.KLogging
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.*


open class TestSupport {

    companion object : KLogging() {

        val mapper = ObjectMapper().also {
            it.registerModule(kotlinModule())
            it.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    protected val key = "teamsysdata"
    protected val lockedDBLocation = newLocation()
    private lateinit var store: PersistentEntityStoreImpl
    lateinit var webApp: PersistentWebApplication

    private lateinit var server: JettyApplicationEngine
    private val context = "/custom"
    private val port = 18443

    protected val retrofit: Retrofit by lazy {
        val client = with(OkHttpClient().newBuilder()) {
            interceptors().add(HttpLoggingInterceptor { logger.info { it } }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            build()
        }
        Retrofit.Builder().client(client).baseUrl("http://localhost:$port/custom/").addConverterFactory(JacksonConverterFactory.create(mapper)).build()
    }

    protected val dbsResource by lazy {
        retrofit.create(DBsApi::class.java)
    }
    protected val dbResource by lazy {
        retrofit.create(DBApi::class.java)
    }

    protected fun newLocation(): String {
        return "java.io.tmpdir".systemProperty() + File.separator + Random().nextLong()
    }

    @Before
    fun before() {
        System.setProperty("xodus.entity.browser.db.store", newLocation())
        store = PersistentEntityStores.newInstance(Environments.newInstance(lockedDBLocation), key)
        webApp = PersistentWebApplication(PersistentDatabaseService())

        server = embeddedServer(Jetty, port = port) {
            webApp.start()
            HttpServer(webApp, context).setup(this)
        }
        server.start(wait = false)

        var setuped = false
        var times = 0
        while (!setuped && times < 10) {
            Thread.sleep(500)
            times++
            val response = dbsResource.all().execute()
            setuped = response.isSuccessful
        }
        if (!setuped) {
            throw IllegalStateException("http server is down")
        }
    }

    fun newDB(location: String, isOpened: Boolean = false): DBSummary {
        return dbsResource.new(DBSummary(location = location,
                key = key,
                isOpened = isOpened,
                isReadonly = false,
                isWatchReadonly = false)).execute().body()!!
    }


    @After
    fun after() {
        store.close()
        webApp.stop()
        File("db").delete()
        server.stop(gracePeriodMillis = 20, timeoutMillis = 20)
    }
}