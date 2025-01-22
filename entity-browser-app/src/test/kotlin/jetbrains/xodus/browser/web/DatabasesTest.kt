package jetbrains.xodus.browser.web

import jetbrains.xodus.browser.web.db.EnvironmentFactory
import jetbrains.xodus.browser.web.db.asParameters
import jetbrains.xodus.browser.web.db.getOrCreateEntityType
import org.junit.Assert.*
import org.junit.Test


class DatabasesTest : TestSupport() {

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
            assertTrue(webApp.databaseService.all().all { it.location != location })
            assertFalse(webApp.allServices.containsKey(uuid))
        }
    }

    @Test
    fun `should be able to trying to start and stop locked db`() {
        with(newDB(lockedDBLocation)) {
            assertFalse(isOpened)

            val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
            assertFalse(resultOfStart.body()!!.isOpened)
            assertFalse(webApp.allServices.containsKey(uuid))

            val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
            assertFalse(resultOfStop.body()!!.isOpened)
            assertFalse(webApp.allServices.containsKey(uuid))
        }
    }

    @Test
    fun `should be able to trying to start and stop db`() {
        val location = newLocation()
        val dbSummary = newDB(location)
        assertFalse(dbSummary.isOpened)

        val uuid = dbSummary.uuid
        val resultOfStart = dbResource.startOrStop(uuid, "start").execute()
        assertTrue(resultOfStart.body()!!.isOpened)
        assertTrue(webApp.allServices.containsKey(uuid))

        val resultOfStop = dbResource.startOrStop(uuid, "stop").execute()
        assertFalse(resultOfStop.body()!!.isOpened)
        assertFalse(webApp.allServices.containsKey(uuid))
        val environment = EnvironmentFactory.createEnvironment(dbSummary.asParameters()) {
            getOrCreateEntityType("BlaBlaBla")
        }
        EnvironmentFactory.closeEnvironment(environment)
    }

    @Test
    fun `can not add new entity type in initialised state`() {
        val location = newLocation()
        val newDB = newDB(location, true)
        val uuid = newDB.uuid
        val response = dbResource.addDbType(uuid, EntityType(id = null, name = "NewType")).execute()
        assertEquals(response.code(), 500)
    }

}