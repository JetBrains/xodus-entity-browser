package com.lehvolk.xodus.repo;

import org.junit.Before;
import org.junit.Test;

import com.lehvolk.xodus.vo.EntityVO.EntityPropertyTypeVO;
import com.lehvolk.xodus.vo.EntityVO.EntityPropertyVO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * tests for transformations service
 * @author Alexey Volkov
 * @since 09.11.2015
 */
public class TransformationsTest {

    private static final String VALUE = "123";

    private Transformations transformations;

    @Before
    public void before() {
        transformations = new Transformations();
        transformations.construct();
    }

    @Test
    public void testString2valueForShort() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, Short.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), Short.class);
        assertEquals(x, (short) 123);
    }

    @Test
    public void testString2valueForInteger() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, Integer.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), Integer.class);
        assertEquals(x, 123);
    }

    @Test
    public void testString2valueForLong() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, Long.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), Long.class);
        assertEquals(x, 123L);
    }

    @Test
    public void testString2valueForFloat() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, Float.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), Float.class);
        assertEquals((Float) x, Float.valueOf(123), 0.0E-9f);
    }

    @Test
    public void testString2valueForDouble() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, Double.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), Double.class);
        assertEquals((Double) x, 123, 0.0E-9d);
    }

    @Test
    public void testString2valueForString() throws Exception {
        EntityPropertyVO propertyVO = ofType(VALUE, String.class);
        Comparable<?> x = transformations.string2value(propertyVO);
        assertNotNull(x);
        assertSame(x.getClass(), String.class);
        assertEquals(x, VALUE);
    }

    @Test(expected = InvalidFieldException.class)
    public void testString2valueForIncorrectValue() throws Exception {
        EntityPropertyVO propertyVO = ofType("123xasadasd", Double.class);
        transformations.string2value(propertyVO);
    }

    @Test
    public void testValue2stringForNull() throws Exception {
        EntityPropertyVO propertyVO = ofType(null, Double.class);
        assertNull(transformations.string2value(propertyVO));
    }

    private EntityPropertyVO ofType(String value, Class<?> clazz) {
        EntityPropertyVO propertyVO = new EntityPropertyVO();
        propertyVO.setValue(value);
        propertyVO.setType(new EntityPropertyTypeVO());
        propertyVO.getType().setClazz(clazz.getName());
        return propertyVO;
    }
}