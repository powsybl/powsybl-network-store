/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractSubnetworksCreationTest;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class SubnetworksCreationTest extends AbstractSubnetworksCreationTest {
    @Override
    public void setup() {
    }

    @Override
    public void testAreaCreation() {
        // FIXME
    }

    @Override
    public void testSubstationCreation() {
        // FIXME
    }

    @Override
    public void testVoltageLevelCreation() {
        // FIXME
    }

    @Override
    public void testLineCreation() {
        // FIXME
    }

    @Override
    public void failCreateLineFromSubnetworkBetweenRootAndSubnetwork() {
        // FIXME
    }

    @Override
    public void failCreateLineFromASubnetworkInAnother() {
        // FIXME
    }

    @Override
    public void testTwoWindingsTransformersCreation() {
        // FIXME
    }

    @Override
    public void testThreeWindingsTransformersCreation() {
        // FIXME
    }

    @Override
    public void testValidationWithSubnetworkChanges(String networkId) {
        // FIXME
    }

    @Override
    public void testListeners() {
        // FIXME
    }

    @Override
    public void testAngleVoltageLimitCreation() {
        // FIXME
    }

    @Override
    public void failCreateVoltageAngleLimitFromSubnetworkBetweenRootAndSubnetwork() {
        // FIXME
    }

    @Override
    public void failCreateVoltageAngleLimitFromASubnetworkInAnother() {
        // FIXME
    }

    @Override
    public void subnetworksWithSubstationFromSameCountry() {
        // FIXME
    }
}
