/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractSubstationAndLinePositionTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class SubstationPositionTest extends AbstractSubstationAndLinePositionTest {

    @Override
    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Substation p1 = network.getSubstation("P1");
        SubstationPosition substationPosition = p1.newExtension(SubstationPositionAdder.class).withCoordinate(new Coordinate(48.0D, 2.0D)).add();
        Assertions.assertNotNull(substationPosition);
        Assertions.assertEquals(new Coordinate(48.0D, 2.0D), substationPosition.getCoordinate());
    }
}
