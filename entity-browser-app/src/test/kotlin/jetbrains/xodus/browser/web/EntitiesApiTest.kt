package jetbrains.xodus.browser.web

import com.jetbrains.youtrack.db.api.DatabaseType
import jetbrains.exodus.entitystore.Entity
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class EntitiesApiTest : TestSupport() {

    private val location = newLocation()

    private lateinit var entity: Entity
    private lateinit var linkedEntity1: Entity
    private lateinit var linkedEntity2: Entity

    private lateinit var dbSummary: DBSummary

    private val entitiesResource by lazy { retrofit.create(EntitiesApi::class.java) }

    private object Users {
        const val CLASS = "User"

        object Links {
            const val GROUPS = "groups"
            const val BOSS = "boss"
            const val TEAM = "team"
        }
    }

    private object Groups {
        const val CLASS = "Group"

        object Links {
            const val FOLKS = "folks"
        }
    }

    @Before
    fun setup() {
        val params = EnvironmentParameters(location = location, key = key)
        val environment = EnvironmentFactory.createEnvironment(params) {
            createEntityType(Users.CLASS)
            createEntityType(Groups.CLASS)
            addAssociation(Users.CLASS, Groups.CLASS, Users.Links.GROUPS, Groups.Links.FOLKS)
            addAssociation(Users.CLASS, Users.CLASS, Users.Links.BOSS, Users.Links.TEAM)
        }
        environment.transactional { txn ->

            linkedEntity1 = txn.newEntity(Users.CLASS).also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            linkedEntity2 = txn.newEntity(Users.CLASS).also {
                it.setProperty("name", "John Silver")
                it.setProperty("age", 45L)
                it.setLink(Users.Links.BOSS, linkedEntity1)
            }

            entity = txn.newEntity(Groups.CLASS).also {
                it.setProperty("type", "Band")
                it.addLink(Groups.Links.FOLKS, linkedEntity1)
                it.addLink(Groups.Links.FOLKS, linkedEntity2)
            }
        }
        EnvironmentFactory.closeEnvironment(environment)
        dbSummary = newDB(location = location, isOpened = true)
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
            assertEquals("User", type)
            assertEquals(0, typeId)
            assertEquals("User[0-0]", label)
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
                assertEquals(Users.CLASS, type)
                assertEquals(0, typeId)
                assertEquals("User[0-0]", label)
                assertEquals(2, properties.size)
            }
            with(items[1]) {
                assertEquals(Users.CLASS, type)
                assertEquals(0, typeId)
                assertEquals("User[0-1]", label)
                assertEquals(2, properties.size)
                assertEquals(1, links.size)
            }
        }
    }

    @Test
    fun `linked entities`() {
        val pager = entitiesResource.links(dbSummary.uuid, entity.toIdString(), Groups.Links.FOLKS).execute().body()!!
        with(pager) {
            assertEquals(2, totalCount)
            assertEquals(Groups.Links.FOLKS, name)

            with(entities.first()) {
                assertEquals(Users.CLASS, type)
                assertEquals(Groups.Links.FOLKS, name)
                assertEquals(0, typeId)
                assertEquals("User[0-0]", label)
            }
            with(entities[1]) {
                assertEquals(Users.CLASS, type)
                assertEquals(Groups.Links.FOLKS, name)
                assertEquals(0, typeId)
                assertEquals("User[0-1]", label)
            }
        }
    }

}