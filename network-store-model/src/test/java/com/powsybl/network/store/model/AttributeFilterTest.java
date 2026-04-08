/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import org.junit.jupiter.api.Test;

import static com.powsybl.network.store.model.AttributeFilter.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
class AttributeFilterTest {

    // Identity: covering a filter with itself returns itself
    @Test
    void coveringSameFilter() {
        assertNull(covering(PRIMARY_AS_NULL, PRIMARY_AS_NULL));
        assertEquals(SV, covering(SV, SV));
        assertEquals(LIMITS, covering(LIMITS, LIMITS));
        assertEquals(FULL, covering(FULL, FULL));
    }

    // Different priorities: returns the broader (higher priority) filter
    @Test
    void coveringDifferentPriorities() {
        // PRIMARY (priority 0) vs SV (priority -1)
        assertNull(covering(PRIMARY_AS_NULL, SV));
        assertNull(covering(SV, PRIMARY_AS_NULL));

        // LIMITS (priority 1) vs SV (priority -1)
        assertEquals(LIMITS, covering(SV, LIMITS));
        assertEquals(LIMITS, covering(LIMITS, SV));

        // LIMITS (priority 1) vs PRIMARY (priority 0)
        assertEquals(LIMITS, covering(PRIMARY_AS_NULL, LIMITS));
        assertEquals(LIMITS, covering(LIMITS, PRIMARY_AS_NULL));

        // FULL (priority 2) vs SV (priority -1)
        assertEquals(FULL, covering(SV, FULL));
        assertEquals(FULL, covering(FULL, SV));

        // FULL (priority 2) vs PRIMARY (priority 0)
        assertEquals(FULL, covering(PRIMARY_AS_NULL, FULL));
        assertEquals(FULL, covering(FULL, PRIMARY_AS_NULL));

        // FULL (priority 2) vs LIMITS (priority 1)
        assertEquals(FULL, covering(LIMITS, FULL));
        assertEquals(FULL, covering(FULL, LIMITS));
    }

    // Same priority, different filters: promotes to the next broader category.
    // Uses the package-private overload with Object placeholders to simulate
    // peer filters that don't exist yet in the enum.
    @Test
    void coveringSamePriorityPromotion() {
        Object a = new Object();
        Object b = new Object();
        Object full = new Object();

        // Two different subsets (negative priority) promote to primary
        assertNull(covering(a, -1, b, -1, PRIMARY_AS_NULL, full));

        // Two different supersets (positive priority) promote to full
        assertEquals(full, covering(a, 1, b, 1, PRIMARY_AS_NULL, full));
    }

    // PRIMARY and FULL should have no peers — throw if two different filters share their priority
    @Test
    void coveringSamePriorityThrowsForPrimaryAndFull() {
        Object a = new Object();
        Object b = new Object();
        Object full = new Object();

        assertThrows(IllegalStateException.class, () -> covering(a, 0, b, 0, PRIMARY_AS_NULL, full));
        assertThrows(IllegalStateException.class, () -> covering(a, 2, b, 2, PRIMARY_AS_NULL, full));
    }

    @Test
    void testIsCovering() {
        assertTrue(AttributeFilter.isCovering(SV, SV));
        assertTrue(AttributeFilter.isCovering(SV, PRIMARY_AS_NULL));
        assertTrue(AttributeFilter.isCovering(SV, LIMITS));
        assertTrue(AttributeFilter.isCovering(SV, FULL));
        assertFalse(AttributeFilter.isCovering(LIMITS, SV));
        assertFalse(AttributeFilter.isCovering(LIMITS, PRIMARY_AS_NULL));
        assertTrue(AttributeFilter.isCovering(LIMITS, LIMITS));
        assertTrue(AttributeFilter.isCovering(LIMITS, FULL));
        assertFalse(AttributeFilter.isCovering(FULL, SV));
        assertFalse(AttributeFilter.isCovering(FULL, PRIMARY_AS_NULL));
        assertFalse(AttributeFilter.isCovering(FULL, LIMITS));
        assertTrue(AttributeFilter.isCovering(FULL, FULL));
    }
}
