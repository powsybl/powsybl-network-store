/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.tck.AbstractOperationalLimitsGroupsTest;

import java.util.Collection;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class OperationalLimitsGroupsTest extends AbstractOperationalLimitsGroupsTest {
    //
    // TODO : all following overrided tests will be removed later, when we will handle multiple selected operational limits groups
    // on one side
    //
    @Override
    public void operationalLimitsGroupOrdering() {
    }

    @Override
    public void getAllSelectedCurrentLimits() {
    }

    @Override
    public void getAllSelectedActivePowerLimits() {
    }

    @Override
    public void getAllSelectedApparentPowerLimits() {
    }

    @Override
    public void checkAddWithPredicate() {
    }

    @Override
    public void doNotSelectGroupsIfAnyIsNullOrDoesNotExist() {
    }

    @Override
    public void checkGetOrCreateSetsSelected() {
    }

    @Override
    public void overloadDurationWithMultipleSelectedOperationalLimitsGroup(Network n, String lineId, double p, double q, int expectedOverloadDuration) {
    }

    @Override
    public void testCopy() {
    }

    @Override
    public void violationUtilCheckTemporaryLimits(Identifiable<?> identifiable, ThreeSides side, Collection<Double> limitReductions, LimitType type, double value, Collection<ExpectedOverload> expected) {
    }

    @Override
    public void operationalLimitsGroupLimitReductionValueSelection(Identifiable<?> identifiable, ThreeSides side, double limitReductionValue, Collection<String> groupsToApplyLimitReduction, LimitType type, double value, Collection<ExpectedOverload> expected) {
    }
}
