package com.lehvolk.xodus.web

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.entitystore.PersistentStoreTransaction
import jetbrains.exodus.env.Environments
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class EntitiesApiTest : TestSupport() {

    private val location = newLocation()
    private lateinit var store: PersistentEntityStoreImpl

    private lateinit var entity: Entity
    private lateinit var linkedEntity1: Entity
    private lateinit var linkedEntity2: Entity

    private lateinit var db: DBSummary

    private val entitiesResource by lazy { retrofit.create(EntitiesApi::class.java) }

    @Before
    fun setup() {
        store = PersistentEntityStores.newInstance(Environments.newInstance(location), key)
        store.executeInTransaction {
            it as PersistentStoreTransaction
            store.getEntityTypeId(it, "Type1", true)
            store.getEntityTypeId(it, "Type2", true)

            linkedEntity1 = it.newEntity("Type1").also {
                it.setProperty("name", "John McClane")
                it.setProperty("age", 35L)
            }

            linkedEntity2 = it.newEntity("Type1").also {
                it.setProperty("name", "John Silver")
                it.setProperty("age", 45L)
                it.setLink("boss", linkedEntity1)
            }

            entity = it.newEntity("Type2").also {
                it.setProperty("type", "Band")
                it.addLink("folks", linkedEntity1)
                it.addLink("folks", linkedEntity2)
            }
        }
        store.close()
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
    fun `get entities`() {
        val view = entitiesResource.get(db.uuid, "0-0").execute().body()!!
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
        val pager = entitiesResource.search(db.uuid, 0, null).execute().body()!!
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
        val pager = entitiesResource.links(db.uuid, entity.toIdString(), "folks").execute().body()!!
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