package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.DatabaseService
import com.lehvolk.xodus.web.db.Databases
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environments
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.net.ServerSocket
import java.util.*

open class TestSupport {
    protected val key = "teamsysdata"
    protected val lockedDBLocation = newLocation()
    private lateinit var store: PersistentEntityStoreImpl
    private val databaseService = DatabaseService()
    private val port by lazy {
        ServerSocket(0).let {
            it.close()
            it.localPort
        }
    }

    protected val retrofit: Retrofit = Retrofit.Builder().baseUrl("http://localhost:$port/").addConverterFactory(JacksonConverterFactory.create(mapper)).build()

    protected val dbsResource by lazy {
        retrofit.create(DBsApi::class.java)
    }
    protected val dbResource by lazy {
        retrofit.create(DBApi::class.java)
    }

    protected fun newLocation(): String {
        return "java.io.tmpdir".system() + Random().nextLong()
    }

    @Before
    fun before() {
        System.setProperty("recent.dbs", newLocation() + File.separator + "recent.dbs.json")
        store = PersistentEntityStores.newInstance(Environments.newInstance(lockedDBLocation), key)
        Application.start()
        HttpServer.setup(port)

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
        return dbsResource.new(DBSummary(location = location, key = key, isOpened = isOpened)).execute().body()!!
    }


    @After
    fun after() {
        store.close()
        databaseService.deleteAll()
        Databases.file.delete()
        HttpServer.stop()
    }
}