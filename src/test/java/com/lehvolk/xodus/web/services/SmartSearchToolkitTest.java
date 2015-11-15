package com.lehvolk.xodus.web.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityId;
import jetbrains.exodus.entitystore.StoreTransaction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for SmartSearchToolkit
 * @author Alexey Volkov
 * @since 09.11.2015
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartSearchToolkitTest {

    private static final int TYPE_ID = 0;
    private static final String TYPE_NAME = "type";

    @Mock
    private StoreTransaction tx;

    @Mock
    private EntityIterable iterable;

    @Mock
    private EntityIterable union;

    @Mock
    private EntityIterable intersection;

    @Mock
    private Entity entity;

    @Before
    public void construct() {
        when(iterable.union(eq(iterable))).thenReturn(union);
        when(iterable.intersect(eq(iterable))).thenReturn(intersection);
    }

    @Test
    public void testSearchAll() {
        when(tx.getAll(TYPE_NAME)).thenReturn(iterable);
        EntityIterable search = search(null);
        assertEquals(iterable, search);
        verify(tx).getAll(eq(TYPE_NAME));
    }

    @Test
    public void testSimpleIdSearch() {
        PersistentEntityId id = new PersistentEntityId(TYPE_ID, 1);
        when(tx.getEntity(id)).thenReturn(entity);
        when(tx.getSingletonIterable(eq(entity))).thenReturn(iterable);

        EntityIterable search = search("1");
        assertEquals(iterable, search);
        verify(tx).getEntity(eq(id));
        verify(tx).getSingletonIterable(eq(entity));
    }

    @Test
    public void testWrongIdSearch() {
        EntityIterable search = search("1x");
        assertNotNull(search);
        assertEquals(0, search.size());
        verify(tx, never()).getEntity(any(PersistentEntityId.class));
    }

    @Test
    public void testSearchByIdParam() {
        PersistentEntityId id = new PersistentEntityId(TYPE_ID, 1);
        when(tx.getEntity(id)).thenReturn(entity);
        when(tx.find(TYPE_NAME, "id", "1")).thenReturn(iterable);
        when(tx.getSingletonIterable(eq(entity))).thenReturn(iterable);

        EntityIterable search = search("id=1");
        assertEquals(union, search);
        verify(tx).getEntity(eq(id));
        verify(tx).getSingletonIterable(eq(entity));
        verify(tx).find(eq(TYPE_NAME), eq("id"), eq("1"));
    }

    @Test
    public void testSearchByIdRange() {
        when(tx.findIds(TYPE_NAME, 1, 10)).thenReturn(iterable);
        when(tx.find(TYPE_NAME, "id", 1L, 10L)).thenReturn(iterable);
        when(tx.getSingletonIterable(eq(entity))).thenReturn(iterable);

        EntityIterable search = search("id=[1,10]");
        assertEquals(union, search);
        verify(tx).findIds(eq(TYPE_NAME), eq(1L), eq(10L));
        verify(tx).find(eq(TYPE_NAME), eq("id"), eq(1L), eq(10L));
    }

    @Test
    public void testSearchByLike() {
        when(tx.findStartingWith(TYPE_NAME, "firstName", "Jo")).thenReturn(iterable);

        EntityIterable search = search("firstName~Jo");
        assertEquals(iterable, search);
        verify(tx).findStartingWith(eq(TYPE_NAME), eq("firstName"), eq("Jo"));
    }

    @Test
    public void testSearchByParams() {
        when(tx.findStartingWith(TYPE_NAME, "firstName", "Jo")).thenReturn(iterable);
        when(tx.find(TYPE_NAME, "lastName", "McCain")).thenReturn(iterable);

        EntityIterable search = search("firstName~Jo and lastName=McCain");
        assertEquals(intersection, search);
        verify(tx).findStartingWith(eq(TYPE_NAME), eq("firstName"), eq("Jo"));
        verify(tx).find(eq(TYPE_NAME), eq("lastName"), eq("McCain"));
    }

    private EntityIterable search(String term) {
        return SmartSearchToolkit.doSmartSearch(term, TYPE_NAME, TYPE_ID, tx);
    }
}
