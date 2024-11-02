package jetbrains.xodus.browser.web

import jetbrains.exodus.entitystore.*
import jetbrains.exodus.env.Environments
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class BrokenLinksTest : TestSupport() {

    private val location = newLocation()
    private lateinit var store: PersistentEntityStoreImpl

    private lateinit var brokenEntityId: PersistentEntityId
    private lateinit var linkedEntity1: Entity

    private lateinit var db: DBSummary

    private val entitiesResource by lazy { retrofit.create(EntitiesApi::class.java) }

    @Before
    fun setup() {
        store = PersistentEntityStores.newInstance(Environments.newInstance(location), key)
        store.executeInTransaction {
            it as PersistentStoreTransaction
            store.getEntityTypeId(it, "Type1", true)
            store.getEntityTypeId(it, "Type2", true)

            val brokenEntity = it.newEntity("Type2").also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            brokenEntityId = brokenEntity.id

            linkedEntity1 = it.newEntity("Type1").also {
                it.setProperty("type", "Band")
                it.addLink("folks", brokenEntity)
            }
        }
        store.executeInTransaction {
            it as PersistentStoreTransaction

            it.getEntity(brokenEntityId).delete()
        }
        store.close()
        store.environment.close()
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