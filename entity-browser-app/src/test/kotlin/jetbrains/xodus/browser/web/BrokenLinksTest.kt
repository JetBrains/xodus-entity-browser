package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.EntityId
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.xodus.browser.web.db.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class BrokenLinksTest : TestSupport() {

    private val location = newLocation()
    private lateinit var environment: Environment

    private lateinit var brokenEntityId: EntityId
    private lateinit var linkedEntity1: Entity

    private lateinit var db: DBSummary

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
        environment = EnvironmentFactory.createEnvironment(params) {
            createEntityType(Users.CLASS)
            createEntityType(Groups.CLASS)
            addAssociation(Users.CLASS, Groups.CLASS, Users.Links.GROUPS, Groups.Links.FOLKS)
            addAssociation(Users.CLASS, Users.CLASS, Users.Links.BOSS, Users.Links.TEAM)
        }
        environment.transactional { txn: StoreTransaction ->

            val brokenEntity = txn.newEntity(Groups.CLASS).also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            brokenEntityId = brokenEntity.id

            linkedEntity1 = txn.newEntity(Users.CLASS).also {
                it.setProperty("type", "Band")
                it.addLink("folks", brokenEntity)
            }
        }
        environment.transactional {
            it.getEntity(brokenEntityId).delete()
        }
        EnvironmentFactory.closeEnvironment(environment)
        db = newDB(location, true)
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
    fun `get entities with broken links`() {
        val view = entitiesResource.get(db.uuid, "0-0").execute().body()!!
        with(view) {
            Assert.assertEquals(1, links.size)
        }
    }

    @Test
    fun `delete broken link`() {
        val changeSummary = ChangeSummary(
            links = listOf(
                LinkChangeSummaryAction(
                    name = "folks",
                    oldValue = EntityLink(id = "1-0", label = "", typeId = 0, type = "", notExists = false, name = ""),
                    newValue = null
                )
            )
        )
        entitiesResource.update(db.uuid, "0-0", changeSummary).execute()

        val view = entitiesResource.get(db.uuid, "0-0").execute().body()!!
        with(view) {
            Assert.assertTrue(links.isEmpty())
        }
    }
}