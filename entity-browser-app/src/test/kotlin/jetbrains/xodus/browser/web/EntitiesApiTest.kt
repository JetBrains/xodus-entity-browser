package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.xodus.browser.web.db.getOrCreateEntityTypeId
import jetbrains.xodus.browser.web.db.transactional
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class EntitiesApiTest : TestSupport() {

    private val location = newLocation()
    private lateinit var store: PersistentEntityStore

    private lateinit var entity: Entity
    private lateinit var linkedEntity1: Entity
    private lateinit var linkedEntity2: Entity

    private lateinit var dbSummary: DBSummary

    private val entitiesResource by lazy { retrofit.create(EntitiesApi::class.java) }

    @Before
    fun setup() {
        store = EnvironmentFactory.persistentEntityStore(dbSummary)
        store.getOrCreateEntityTypeId( "Type1", true)
        store.getOrCreateEntityTypeId( "Type2", true)
        store.transactional { txn ->

            linkedEntity1 = txn.newEntity("Type1").also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            linkedEntity2 = txn.newEntity("Type1").also {
                it.setProperty("name", "John Silver")
                it.setProperty("age", 45L)
                it.setLink("boss", linkedEntity1)
            }

            entity = txn.newEntity("Type2").also {
                it.setProperty("type", "Band")
                it.addLink("folks", linkedEntity1)
                it.addLink("folks", linkedEntity2)
            }
        }
        store.close()
        dbSummary = newDB(location, true)
    }

    @After
    fun cleanup() {
        try {
            File(location).delete()
        } catch (e: Exception) {
            println("can't delete file")
            e.printStackTrace()
        }
    }

    @Test
    fun `get entities`() {
        val view = entitiesResource.get(dbSummary.uuid, "0-0").execute().body()!!
        with(view) {
            assertEquals("0-0", id)
            assertEquals("Type1", type)
            assertEquals(0, typeId)
            assertEquals("Type1[0-0]", label)
            assertEquals(2, properties.size)
            with(properties.first { it.value == "35" }) {
                assertEquals("age", name)
                assertEquals("Long", type.displayName)
            }
            with(properties.first { it.value == "John McClane" }) {
                assertEquals("name", name)
                assertEquals("String", type.displayName)
            }
        }
    }

    @Test
    fun `search entities`() {
        val pager = entitiesResource.search(dbSummary.uuid, 0, null).execute().body()!!
        with(pager) {
            assertEquals(2, items.size)

            with(items.first()) {
                assertEquals("Type1", type)
                assertEquals(0, typeId)
                assertEquals("Type1[0-0]", label)
                assertEquals(2, properties.size)
            }
            with(items[1]) {
                assertEquals("Type1", type)
                assertEquals(0, typeId)
                assertEquals("Type1[0-1]", label)
                assertEquals(2, properties.size)
                assertEquals(1, links.size)
            }
        }
    }

    @Test
    fun `linked entities`() {
        val pager = entitiesResource.links(dbSummary.uuid, entity.toIdString(), "folks").execute().body()!!
        with(pager) {
            assertEquals(2, totalCount)
            assertEquals("folks", name)

            with(entities.first()) {
                assertEquals("Type1", type)
                assertEquals("folks", name)
                assertEquals(0, typeId)
                assertEquals("Type1[0-0]", label)
            }
            with(entities[1]) {
                assertEquals("Type1", type)
                assertEquals("folks", name)
                assertEquals(0, typeId)
                assertEquals("Type1[0-1]", label)
            }
        }
    }

}