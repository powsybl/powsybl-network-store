/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility.AbstractVoltageRegulationBackwardCompatibilityOnVoltageSourceConverterTest;
import org.junit.jupiter.api.Disabled;

/**
 * Detailed DC network support is not implemented by network-store.
 */
@Disabled("Voltage source converters require the unsupported detailed DC network")
class VoltageRegulationBackwardCompatibilityOnVoltageSourceConverterTest
        extends AbstractVoltageRegulationBackwardCompatibilityOnVoltageSourceConverterTest {
}
