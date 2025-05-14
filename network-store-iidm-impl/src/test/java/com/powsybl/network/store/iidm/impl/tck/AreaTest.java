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
public class AreaTest extends AbstractAreaTest {
    @Override
    public void mergeAndFlatten() {
        // merge is not implemented
    }

    @Override
    public void throwBoundaryOtherNetwork() {
        // creation of subnetwork needed
    }

    @Override
    public void throwAddVoltageLevelOtherNetwork() {
        // creation of subnetwork needed
    }

    @Override
    public void removeEquipmentRemovesAreaBoundaryMergeAndDetach() {
        // merge is not implemented
    }

    @Override
    public void testSetterGetterInMultiVariants() {
        // when variant is set and then deleted if a attribute is gotten it does not fail with network store
    }
}
