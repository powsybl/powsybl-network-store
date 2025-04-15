/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractAreaTest;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
// FIXME remove all the overridden test in this class when Areas are implemented
public class AreaTest extends AbstractAreaTest {
    @Override
    public void mergeAndFlatten() {

    }

    @Override
    public void throwAddVoltageLevelOtherNetwork() {

    }

    @Override
    public void removeArea() {
        // removed equipments is managed differently in powsybl core implementation
    }

    @Override
    public void throwRemovedVoltageLevel() {
        // removed equipments is managed differently in powsybl core implementation
    }

    @Override
    public void testGetAreaBoundary() {
        // problem with removal
    }

    @Override
    public void testSetterGetterInMultiVariants() {
        // problem with removal
    }

    @Override
    public void throwBoundaryOtherNetwork() {
        // creation of subnetwork needed
    }

    @Override
    public void removeEquipmentRemovesAreaBoundaryMergeAndDetach() {
        // merge is not implemented
    }
}
