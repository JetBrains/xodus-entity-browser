package jetbrains.xodus.browser.web

import jetbrains.xodus.browser.web.db.Databases
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

    @Test
    fun `should be able to add new entity type`() {
        val location = newLocation()
        with(newDB(location, true)) {
            var types = dbResource.addDbType(uuid, EntityType(id = null, name = "NewType")).execute()
            assertEquals(1, types.body()!!.size)

            types = dbResource.addDbType(uuid, EntityType(id = null, name = "NewType1")).execute()
            assertEquals(2, types.body()!!.size)

            with(dbResource.allDbTypes(uuid)) {
                assertEquals(2, types.body()!!.size)
            }
        }
    }

}