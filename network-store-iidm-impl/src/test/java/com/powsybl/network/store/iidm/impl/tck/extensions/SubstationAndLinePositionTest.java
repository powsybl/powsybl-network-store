/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.tck.extensions.AbstractSubstationAndLinePositionTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubstationAndLinePositionTest extends AbstractSubstationAndLinePositionTest {
    @Test
    public void testLinePositionException() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        assertEquals("Line position extension only supported for lines and dangling lines", assertThrows(PowsyblException.class, () -> vl.newExtension(LinePositionAdder.class)).getMessage());

    }

    @Test
    public void testExtensionsName() {
        Network network = EurostagTutorialExample1Factory.create();

        Substation p1 = network.getSubstation("P1");
        SubstationPosition substationPosition = p1.newExtension(SubstationPositionAdder.class).withCoordinate(new Coordinate(48.0D, 2.0D)).add();
        Assertions.assertEquals("substationPosition", substationPosition.getName());

        Line l1 = network.getLine("NHV1_NHV2_1");
        l1.newExtension(LinePositionAdder.class).withCoordinates(List.of(new Coordinate(48.0D, 2.0D), new Coordinate(48.1D, 2.1D))).add();
        LinePosition linePosition = l1.getExtension(LinePosition.class);
        Assertions.assertEquals("linePosition", linePosition.getName());
    }
}
