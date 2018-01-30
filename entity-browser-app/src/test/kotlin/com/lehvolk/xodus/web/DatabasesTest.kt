package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.DatabaseService
import com.lehvolk.xodus.web.db.Databases
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environments
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import spark.Spark
import java.io.File
import java.net.ServerSocket
import java.util.*


class DatabasesTest {

    private val key = "teamsysdata"

    private val lockedDBLocation = newLocation()
    private lateinit var store: PersistentEntityStoreImpl
    private val databaseService = DatabaseService()

    private val port by lazy {
        ServerSocket(0).let {
            it.close()
            it.localPort
        }
    }
    private var retrofit = Retrofit.Builder().baseUrl("http://localhost:$port/").addConverterFactory(JacksonConverterFactory.create(mapper)).build()

    private val dbsResource by lazy {
        retrofit.create(DBsApi::class.java)
    }

    private val dbResource by lazy {
        retrofit.create(DBApi::class.java)
    }


    private fun newLocation(): String {
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

    @After
    fun after() {
        store.close()
        databaseService.deleteAll()
        Databases.file.delete()
        Spark.stop()
    }

    @Test
    fun `should be able to add new db which is locked`() {
        newDB(lockedDBLocation).let {
            assertFalse(it.isOpened)
            assertEquals(lockedDBLocation, it.location)
            assertEquals(key, it.key)
        }
    }

    @Test
    fun `should be able to add new db`() {
        val location = newLocation()
        newDB(location).let {
            assertFalse(it.isOpened)
            assertEquals(location, it.location)
            assertEquals(key, it.key)
        }
    }

    @Test
    fun `should be able to add new db and open it`() {
        val location = newLocation()
        newDB(location, true).let {
            assertTrue(it.isOpened)
            assertEquals(location, it.location)
            assertEquals(key, it.key)
        }
    }

    @Test
    fun `should be able to delete db`() {
        val location = newLocation()
        with(newDB(location)) {
            dbResource.delete(uuid).execute()
            assertTrue(Databases.all().all { it.location != location })
            assertFalse(Application.allServices.contains(uuid))
        }
    }

    @Test
    fun `should be able to trying to start and stop locked db`() {
        with(newDB(lockedDBLocation)) {
            assertFalse(isOpened)

            val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
            assertFalse(resultOfStart.body()!!.isOpened)
            assertFalse(Application.allServices.contains(uuid))

            val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
            assertFalse(resultOfStop.body()!!.isOpened)
            assertFalse(Application.allServices.contains(uuid))
        }
    }

    @Test
    fun `should be able to trying to start and stop db`() {
        val location = newLocation()
        with(newDB(location)) {
            assertFalse(isOpened)

            val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
            assertTrue(resultOfStart.body()!!.isOpened)
            assertTrue(Application.allServices.contains(uuid))

            val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
            assertFalse(resultOfStop.body()!!.isOpened)
            assertFalse(Application.allServices.contains(uuid))
        }
    }

    private fun newDB(location: String, isOpened: Boolean = false): DBSummary {
        return dbsResource.new(DBSummary(location, key, isOpened)).execute().body()!!
    }

}