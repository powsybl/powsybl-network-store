/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.voltage.regulation;

import com.powsybl.iidm.network.tck.voltage.regulation.AbstractVoltageRegulationOnVoltageSourceConverterTest;
import org.junit.jupiter.api.Disabled;

/**
 * Voltage source converters belong to the detailed DC network model, which is not implemented
 * in the network store yet.
 */
@Disabled("The detailed DC network model is not implemented in the network store")
class VoltageRegulationOnVoltageSourceConverterTest extends AbstractVoltageRegulationOnVoltageSourceConverterTest {
}
