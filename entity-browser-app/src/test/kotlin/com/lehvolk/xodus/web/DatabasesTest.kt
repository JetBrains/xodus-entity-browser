package com.lehvolk.xodus.web

import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.resources.DB
import com.lehvolk.xodus.web.resources.DBs
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environments
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.util.*
@Ignore
class DatabasesTest {

    private val key = "teamsysdata"

    private val lockedDBLocation = newLocation()
    private lateinit var store: PersistentEntityStoreImpl


    private fun newLocation(): String {
        return "java.io.tmpdir".system() + File.separator + Random().nextLong()
    }

    @Before
    fun before() {
        System.setProperty("recent.dbs", newLocation() + File.separator + "recent.dbs.json")
        store = PersistentEntityStores.newInstance(Environments.newInstance(lockedDBLocation), key)
    }

    @After
    fun after() {
        store.close()
        Databases.deleteAll()
        Databases.file.delete()
    }

//    @Test
//    fun `should be able to add new db which is locked`() {
//        newDB(lockedDBLocation).let {
//            assertFalse(it.isOpened)
//            assertEquals(lockedDBLocation, it.location)
//            assertEquals(key, it.key)
//        }
//    }
//
//    @Test
//    fun `should be able to add new db`() {
//        val location = newLocation()
//        newDB(location).let {
//            assertTrue(it.isOpened)
//            assertEquals(location, it.location)
//            assertEquals(key, it.key)
//        }
//    }
//
//    @Test
//    fun `should be able to delete db`() {
//        val location = newLocation()
//        with(newDB(location)) {
//            dbResource(uuid).deleteDB()
//            assertTrue(Databases.all().all { it.location != location })
//            assertFalse(Application.allServices.contains(uuid))
//        }
//    }
//
//    private fun newDB(location: String): DBSummary {
//        return DBs().newDB(DBSummary(location, key))
//    }
//
//    private fun dbResource(uuid: String): DB {
//        return DBs().db(uuid)
//    }
}