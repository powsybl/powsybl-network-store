package com.powsybl.network.store.model;

import com.powsybl.iidm.network.ReactiveLimitsKind;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes.COMPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ReactiveCapabilityCurveAttributesTest {

    @Test
    void testConstructorNoArgs() {
        ReactiveCapabilityCurveAttributes attributes = new ReactiveCapabilityCurveAttributes();

        // Default values
        assertEquals(ReactiveLimitsKind.CURVE, attributes.getKind());
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = attributes.getPoints();
        assertNull(attributes.getOwnerDescription());

        // Check the TreeMap with some values added
        ReactiveCapabilityCurvePointAttributes pointAttributes = new ReactiveCapabilityCurvePointAttributes(0.0, 0.0, 0.0);
        ReactiveCapabilityCurvePointAttributes pointAttributes2 = new ReactiveCapabilityCurvePointAttributes(0.5, 0.0, 0.0);
        points.put(0.0, pointAttributes);
        points.put(0.5, pointAttributes2);
        assertEquals(2, points.size());
        assertEquals(pointAttributes, points.get(0.0));
        assertEquals(pointAttributes2, points.get(0.5));

        // Check the comparator
        assertEquals(pointAttributes, points.get(-0.0));
    }

    @Test
    void testConstructorAllArgs() {
        // Parameters
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> expectedPoints = new TreeMap<>(COMPARATOR);
        ReactiveCapabilityCurvePointAttributes pointAttributes = new ReactiveCapabilityCurvePointAttributes(0.0, 0.0, 0.0);
        ReactiveCapabilityCurvePointAttributes pointAttributes2 = new ReactiveCapabilityCurvePointAttributes(0.5, 0.0, 0.0);
        expectedPoints.put(0.0, pointAttributes);
        expectedPoints.put(0.5, pointAttributes2);

        // Create the object
        ReactiveCapabilityCurveAttributes attributes = new ReactiveCapabilityCurveAttributes(expectedPoints, "test");

        // Default values
        assertEquals(ReactiveLimitsKind.CURVE, attributes.getKind());
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = attributes.getPoints();
        assertEquals("test", attributes.getOwnerDescription());

        // Check the TreeMap with some values added
        assertEquals(2, points.size());
        assertEquals(pointAttributes, points.get(0.0));
        assertEquals(pointAttributes2, points.get(0.5));

        // Check the comparator
        assertEquals(pointAttributes, points.get(-0.0));
    }

    @Test
    void testSetPoints() {
        // Points
        ReactiveCapabilityCurvePointAttributes pointAttributes1 = new ReactiveCapabilityCurvePointAttributes(0.0, 0.0, 0.0);
        ReactiveCapabilityCurvePointAttributes pointAttributes2 = new ReactiveCapabilityCurvePointAttributes(0.5, 0.0, 0.0);

        // Create the object with default parameters
        ReactiveCapabilityCurveAttributes attributes = new ReactiveCapabilityCurveAttributes();

        // TreeMap with the comparator
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> treeMapWithComparator = new TreeMap<>(COMPARATOR);
        treeMapWithComparator.put(0.0, pointAttributes1);
        treeMapWithComparator.put(0.5, pointAttributes2);
        assertEquals(pointAttributes1, treeMapWithComparator.get(-0.0));

        // Set the TreeMap
        attributes.setPoints(treeMapWithComparator);

        // Check the TreeMap
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points = attributes.getPoints();
        assertPoints(points, pointAttributes1, pointAttributes2);

        // TreeMap without the comparator
        TreeMap<Double, ReactiveCapabilityCurvePointAttributes> treeMapWithoutComparator = new TreeMap<>();
        treeMapWithoutComparator.put(0.0, pointAttributes1);
        treeMapWithoutComparator.put(0.5, pointAttributes2);
        assertNull(treeMapWithoutComparator.get(-0.0));

        // Set the TreeMap
        attributes.setPoints(treeMapWithoutComparator);

        // Check the TreeMap
        points = attributes.getPoints();
        assertPoints(points, pointAttributes1, pointAttributes2);

        // Set to null
        attributes.setPoints(null);

        // Check the TreeMap
        assertNull(attributes.getPoints());
    }

    private void assertPoints(TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points,
                              ReactiveCapabilityCurvePointAttributes pointAttributes1,
                              ReactiveCapabilityCurvePointAttributes pointAttributes2) {
        assertEquals(2, points.size());
        assertEquals(pointAttributes1, points.get(0.0));
        assertEquals(pointAttributes2, points.get(0.5));

        // Check the comparator
        assertEquals(pointAttributes1, points.get(-0.0));
    }
}
