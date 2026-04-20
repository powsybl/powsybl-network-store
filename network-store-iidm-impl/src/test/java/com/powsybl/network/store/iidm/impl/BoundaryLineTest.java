/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class BoundaryLineTest {
    @Test
    void removeExtension() {
        Network network = BoundaryLineNetworkFactory.create();
        BoundaryLine boundaryLine = network.getBoundaryLine("BL");
        boundaryLine.newExtension(ConnectablePositionAdder.class).newFeeder().withOrder(10).add().add();
        assertTrue(boundaryLine.removeExtension(ConnectablePosition.class));
        assertNull(boundaryLine.getExtension(ConnectablePosition.class));
        assertFalse(boundaryLine.removeExtension(ConnectablePosition.class));
    }

    @Test
    void testReactiveCapabilityCurveProperties() {
        Network network = BoundaryLineNetworkFactory.createWithGeneration();
        BoundaryLine boundaryLine = network.getBoundaryLine("BL");

        ReactiveCapabilityCurve curve = (ReactiveCapabilityCurve) boundaryLine.getGeneration().getReactiveLimits();

        assertFalse(curve.hasProperty());
        assertTrue(curve.getPropertyNames().isEmpty());

        curve.setProperty("curveProp1", "curveValue1");
        curve.setProperty("curveProp2", "curveValue2");

        assertTrue(curve.hasProperty());
        assertTrue(curve.hasProperty("curveProp1"));
        assertTrue(curve.hasProperty("curveProp2"));
        assertFalse(curve.hasProperty("nonExistentCurveProp"));

        assertEquals("curveValue1", curve.getProperty("curveProp1"));
        assertEquals("curveValue2", curve.getProperty("curveProp2"));
        assertNull(curve.getProperty("nonExistentCurveProp"));

        assertEquals("curveValue1", curve.getProperty("curveProp1", "defaultCurveValue"));
        assertEquals("defaultCurveValue", curve.getProperty("nonExistentCurveProp", "defaultCurveValue"));

        String oldCurveValue = curve.setProperty("curveProp3", "curveValue3");
        assertNull(oldCurveValue);
        assertEquals("curveValue3", curve.getProperty("curveProp3"));

        String replacedCurveValue = curve.setProperty("curveProp1", "newCurveValue1");
        assertEquals("curveValue1", replacedCurveValue);
        assertEquals("newCurveValue1", curve.getProperty("curveProp1"));

        Set<String> curvePropertyNames = curve.getPropertyNames();
        assertEquals(3, curvePropertyNames.size());
        assertTrue(curvePropertyNames.contains("curveProp1"));
        assertTrue(curvePropertyNames.contains("curveProp2"));
        assertTrue(curvePropertyNames.contains("curveProp3"));

        assertTrue(curve.removeProperty("curveProp3"));
        assertFalse(curve.hasProperty("curveProp3"));
        assertFalse(curve.removeProperty("nonExistentCurveProp"));

        Collection<ReactiveCapabilityCurve.Point> points = curve.getPoints();
        ReactiveCapabilityCurve.Point point = points.iterator().next();

        assertFalse(point.hasProperty());
        assertTrue(point.getPropertyNames().isEmpty());

        point.setProperty("pointProp1", "pointValue1");
        point.setProperty("pointProp2", "pointValue2");

        assertTrue(point.hasProperty());
        assertTrue(point.hasProperty("pointProp1"));
        assertTrue(point.hasProperty("pointProp2"));
        assertFalse(point.hasProperty("nonExistentPointProp"));

        assertEquals("pointValue1", point.getProperty("pointProp1"));
        assertEquals("pointValue2", point.getProperty("pointProp2"));
        assertNull(point.getProperty("nonExistentPointProp"));

        assertEquals("pointValue1", point.getProperty("pointProp1", "defaultPointValue"));
        assertEquals("defaultPointValue", point.getProperty("nonExistentPointProp", "defaultPointValue"));

        String oldPointValue = point.setProperty("pointProp3", "pointValue3");
        assertNull(oldPointValue);
        assertEquals("pointValue3", point.getProperty("pointProp3"));

        String replacedPointValue = point.setProperty("pointProp1", "newPointValue1");
        assertEquals("pointValue1", replacedPointValue);
        assertEquals("newPointValue1", point.getProperty("pointProp1"));

        Set<String> pointPropertyNames = point.getPropertyNames();
        assertEquals(3, pointPropertyNames.size());
        assertTrue(pointPropertyNames.contains("pointProp1"));
        assertTrue(pointPropertyNames.contains("pointProp2"));
        assertTrue(pointPropertyNames.contains("pointProp3"));

        assertTrue(point.removeProperty("pointProp3"));
        assertFalse(point.hasProperty("pointProp3"));
        assertFalse(point.removeProperty("nonExistentPointProp"));

        ReactiveCapabilityCurve retrievedCurve = (ReactiveCapabilityCurve) network.getBoundaryLine("BL").getGeneration().getReactiveLimits();
        assertEquals("newCurveValue1", retrievedCurve.getProperty("curveProp1"));
        assertEquals("curveValue2", retrievedCurve.getProperty("curveProp2"));
        assertFalse(retrievedCurve.hasProperty("curveProp3"));

        ReactiveCapabilityCurve.Point retrievedPoint = retrievedCurve.getPoints().iterator().next();
        assertEquals("newPointValue1", retrievedPoint.getProperty("pointProp1"));
        assertEquals("pointValue2", retrievedPoint.getProperty("pointProp2"));
        assertFalse(retrievedPoint.hasProperty("pointProp3"));
    }
}
