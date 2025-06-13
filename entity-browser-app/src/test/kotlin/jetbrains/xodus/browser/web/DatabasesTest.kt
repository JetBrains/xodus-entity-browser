package jetbrains.xodus.browser.web

import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File


class DatabasesTest : TestSupport() {

    companion object {
        private const val LOCKED_DB_NAME = "locked-db"
    }

    private val lockedDBLocation = newLocation()
    private lateinit var lockedEnvironment: Environment

    @Before
    fun setUpLocked() {
        val parameters = YTDBEnvironmentParameters(key = LOCKED_DB_NAME, location = lockedDBLocation)
        lockedEnvironment = YTDBEnvironmentFactory.createEnvironment(parameters)
    }

    @After
    fun tearDownLocked() {
        YTDBEnvironmentFactory.closeEnvironment(lockedEnvironment)
        File(lockedDBLocation).delete()
    }

    @Test
    fun `should be able to add new db which is locked`() {
        val dbSummary = newDB(location = lockedDBLocation, dbName = LOCKED_DB_NAME, isOpened = false)
        assertFalse(dbSummary.isOpened)
        assertEquals(lockedDBLocation, dbSummary.location)
        assertEquals(LOCKED_DB_NAME, dbSummary.key)
    }

    @Test
    fun `should be able to add new db`() {
        val location = newLocation()
        val dbName = "new-db"
        val dbSummary = newDB(location, dbName)
        assertFalse(dbSummary.isOpened)
        assertEquals(location, dbSummary.location)
        assertEquals(dbName, dbSummary.key)
    }

    @Test
    fun `should be able to add new db and open it`() {
        val location = newLocation()
        val dbName = "new-open-db"
        val dbSummary = newDB(location, dbName, true)
        assertTrue(dbSummary.isOpened)
        assertEquals(location, dbSummary.location)
        assertEquals(dbName, dbSummary.key)
    }

    @Test
    fun `should be able to delete db`() {
        val location = newLocation()
        val dbName = "delete-db"
        val dbSummary = newDB(location, dbName, false)
        dbResource.delete(dbSummary.uuid).execute()
        assertTrue(webApp.databaseService.all().all { it.location != location })
        assertFalse(webApp.allServices.containsKey(dbSummary.uuid))
    }

    //TODO it seems that it will never work with YTDB
//    @Test
//    fun `should be able to trying to start and stop locked db`() {
//        val dbSummary = newDB(location = lockedDBLocation, dbName = LOCKED_DB_NAME, isOpened = false)
//        val uuid = dbSummary.uuid
//
//        assertFalse(dbSummary.isOpened)
//        val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
//        assertFalse(resultOfStart.body()!!.isOpened)
//        assertFalse(webApp.allServices.containsKey(uuid))
//
//        val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
//        assertFalse(resultOfStop.body()!!.isOpened)
//        assertFalse(webApp.allServices.containsKey(uuid))
//    }

    @Test
    fun `should be able to trying to start and stop db`() {
        val location = newLocation()
        val dbSummary = newDB(location = location, dbName = "new-db", isOpened = false)
        assertFalse(dbSummary.isOpened)

        val uuid = dbSummary.uuid
        val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
        assertTrue(resultOfStart.body()!!.isOpened)
        assertTrue(webApp.allServices.containsKey(uuid))

        val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
        assertFalse(resultOfStop.body()!!.isOpened)
        assertFalse(webApp.allServices.containsKey(uuid))
        val environment = YTDBEnvironmentFactory.createEnvironment(dbSummary.asParameters()) {
            getOrCreateEntityType("BlaBlaBla")
        }
        YTDBEnvironmentFactory.closeEnvironment(environment)
    }

    @Test
    fun `can not add new entity type in initialised state`() {
        val newDB = newDB(newLocation(), "initialised-db", true)
        val uuid = newDB.uuid
        val response = dbResource.addDbType(uuid, EntityType(id = null, name = "NewType")).execute()
        assertEquals(response.code(), 500)
    }

    @Test
    fun `should return all registered types`() {
        val location = newLocation()
        val dbName = "types-test-db"
        val dbSummary = newDB(location, dbName, false)
        val uuid = dbSummary.uuid

        val expectedTypes = listOf("Type1", "Type2", "Type3")
        val environment = YTDBEnvironmentFactory.createEnvironment(dbSummary.asParameters()) {
            expectedTypes.forEach { getOrCreateEntityType(it) }
        }
        YTDBEnvironmentFactory.closeEnvironment(environment)

        // Start the database to access its types
        dbResource.startOrStop(uuid, "start").execute()

        val response = dbResource.allDbTypes(uuid).execute()
        assertEquals(200, response.code())

        val types = response.body()!!
        assertEquals(expectedTypes.sorted(), types.map { it.name }.sorted())
    }

}
