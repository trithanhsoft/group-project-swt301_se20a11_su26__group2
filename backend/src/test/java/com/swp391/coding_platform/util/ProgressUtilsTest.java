package com.swp391.coding_platform.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressUtilsTest {

    @Test
    void testCalculatePercentage_WithNullTotalLessons() {
        assertEquals(0, ProgressUtils.calculatePercentage(5, null));
    }

    @Test
    void testCalculatePercentage_WithZeroOrNegativeTotalLessons() {
        assertEquals(0, ProgressUtils.calculatePercentage(5, 0));
        assertEquals(0, ProgressUtils.calculatePercentage(5, -1));
    }

    @Test
    void testCalculatePercentage_WithValidInputs() {
        assertEquals(0, ProgressUtils.calculatePercentage(0, 10));
        assertEquals(50, ProgressUtils.calculatePercentage(5, 10));
        assertEquals(30, ProgressUtils.calculatePercentage(3, 10));
        assertEquals(33, ProgressUtils.calculatePercentage(1, 3));
        assertEquals(67, ProgressUtils.calculatePercentage(2, 3));
        assertEquals(100, ProgressUtils.calculatePercentage(10, 10));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<ProgressUtils> constructor = ProgressUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }
}
