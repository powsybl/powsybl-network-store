package com.powsybl.network.store.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoordinateTest {
    @Test
    public void testEquals() {
        assertEquals(new Coordinate(48.1, 1.5), new Coordinate(48.1, 1.5));
        assertEquals(new Coordinate(48.06, 1.3), new Coordinate(48.06, 1.3));
    }

    @Test
    public void testToString() {
        assertEquals("Coordinate(lat=48.1, lon=1.5)", new Coordinate(48.1, 1.5).toString());
    }

    @Test
    public void testCopy() {
        var c1 = new Coordinate(48.1, 1.5);
        var c1Copy = new Coordinate(c1);
        assertEquals(c1.getLatitude(), c1Copy.getLatitude(), 0);
        assertEquals(c1.getLongitude(), c1Copy.getLongitude(), 0);
    }
}
