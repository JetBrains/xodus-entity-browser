package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.EntityId
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class BrokenLinksTest : TestSupport() {

    private val location = newLocation()

    private lateinit var deletedEntityId: EntityId
    private lateinit var entityWithLinkToDeleted: Entity

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
            getOrCreateEntityType(Groups.CLASS) // type with id=0, first entity with id=0-0
            getOrCreateEntityType(Users.CLASS) // type with id=1, first entity with id=1-0
            addAssociation(Users.CLASS, Groups.CLASS, Users.Links.GROUPS, Groups.Links.FOLKS)
            addAssociation(Users.CLASS, Users.CLASS, Users.Links.BOSS, Users.Links.TEAM)
        }
        environment.transactional { txn: StoreTransaction ->

            val entityToDelete = txn.newEntity(Users.CLASS).also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            deletedEntityId = entityToDelete.id

            entityWithLinkToDeleted = txn.newEntity(Groups.CLASS).also {
                it.setProperty("type", "Band")
                it.addLink("folks", entityToDelete)
            }
        }
        environment.transactional {
            assertEquals("0-0", entityWithLinkToDeleted.toIdString())
            assertEquals("1-0", deletedEntityId.toString())
            it.getEntity(deletedEntityId).delete()
        }
        EnvironmentFactory.closeEnvironment(environment)
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
    fun `get entities with cleared links to deleted entities`() {
        val db = newDB(location, true)
        val view = entitiesResource.get(db.uuid, "0-0").execute().body()!!
        //link description is presented
        assertEquals(1, view.links.size)
        //all linked entities are cleared
        assertEquals(0, view.links.first().entities.size)
    }
}