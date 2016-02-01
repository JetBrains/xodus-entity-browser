package com.lehvolk.xodus.web

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityId
import jetbrains.exodus.entitystore.StoreTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SmartSearchToolkitTest {
    private val TYPE_ID = 0
    private val TYPE_NAME = "type"

    @Mock
    private val tx: StoreTransaction? = null

    @Mock
    private val iterable: EntityIterable? = null

    @Mock
    private val union: EntityIterable? = null

    @Mock
    private val intersection: EntityIterable? = null

    @Mock
    private val entity: Entity? = null

    @Before
    fun construct() {
        `when`<EntityIterable>(iterable!!.union(eq<EntityIterable>(iterable))).thenReturn(union)
        `when`<EntityIterable>(iterable.intersect(eq<EntityIterable>(iterable))).thenReturn(intersection)
    }

    @Test
    fun testSearchAll() {
        `when`<EntityIterable>(tx!!.getAll(TYPE_NAME)).thenReturn(iterable)
        val search = search(null)
        assertEquals(iterable, search)
        verify<StoreTransaction>(tx).getAll(eq<String>(TYPE_NAME))
    }

    @Test
    fun testSimpleIdSearch() {
        val id = PersistentEntityId(TYPE_ID, 1)
        `when`<Entity>(tx!!.getEntity(id)).thenReturn(entity)
        `when`<EntityIterable>(tx.getSingletonIterable(eq<Entity>(entity))).thenReturn(iterable)

        val search = search("1")
        assertEquals(iterable, search)
        verify<StoreTransaction>(tx).getEntity(eq<PersistentEntityId>(id))
        verify<StoreTransaction>(tx).getSingletonIterable(eq<Entity>(entity))
    }

    @Test
    fun testWrongIdSearch() {
        val search = search("1x")
        assertNotNull(search)
        assertEquals(0, search.size())
        verify<StoreTransaction>(tx, never()).getEntity(any<PersistentEntityId>(PersistentEntityId::class.java))
    }

    @Test
    fun testSearchByIdParam() {
        val id = PersistentEntityId(TYPE_ID, 1)
        `when`<Entity>(tx!!.getEntity(id)).thenReturn(entity)
        `when`<EntityIterable>(tx.find(TYPE_NAME, "id", "1")).thenReturn(iterable)
        `when`<EntityIterable>(tx.getSingletonIterable(eq<Entity>(entity))).thenReturn(iterable)

        val search = search("id=1")
        assertEquals(union, search)
        verify<StoreTransaction>(tx).getEntity(eq<PersistentEntityId>(id))
        verify<StoreTransaction>(tx).getSingletonIterable(eq<Entity>(entity))
        verify<StoreTransaction>(tx).find(eq<String>(TYPE_NAME), eq<String>("id"), eq<String>("1"))
    }

    @Test
    fun testSearchByIdRange() {
        `when`<EntityIterable>(tx!!.findIds(TYPE_NAME, 1, 10)).thenReturn(iterable)
        `when`<EntityIterable>(tx.find(TYPE_NAME, "id", 1L, 10L)).thenReturn(iterable)
        `when`<EntityIterable>(tx.getSingletonIterable(eq<Entity>(entity))).thenReturn(iterable)

        val search = search("id=[1,10]")
        assertEquals(union, search)
        verify<StoreTransaction>(tx).findIds(eq<String>(TYPE_NAME), eq(1L), eq(10L))
        verify<StoreTransaction>(tx).find(eq<String>(TYPE_NAME), eq<String>("id"), eq(1L), eq(10L))
    }

    @Test
    fun testSearchByLike() {
        `when`<EntityIterable>(tx!!.findStartingWith(TYPE_NAME, "firstName", "Jo")).thenReturn(iterable)

        val search = search("firstName~Jo")
        assertEquals(iterable, search)
        verify<StoreTransaction>(tx).findStartingWith(eq<String>(TYPE_NAME), eq<String>("firstName"), eq<String>("Jo"))
    }

    @Test
    fun testSearchByParams() {
        `when`<EntityIterable>(tx!!.findStartingWith(TYPE_NAME, "firstName", "Jo")).thenReturn(iterable)
        `when`<EntityIterable>(tx.find(TYPE_NAME, "lastName", "McClane")).thenReturn(iterable)

        val search = search("firstName~Jo and lastName=McClane")
        assertEquals(intersection, search)
        verify<StoreTransaction>(tx).findStartingWith(eq<String>(TYPE_NAME), eq<String>("firstName"), eq<String>("Jo"))
        verify<StoreTransaction>(tx).find(eq<String>(TYPE_NAME), eq<String>("lastName"), eq<String>("McClane"))
    }

    private fun search(term: String?): EntityIterable {
        return SmartSearchToolkit.doSmartSearch(term, TYPE_NAME, TYPE_ID, tx!!)
    }


}