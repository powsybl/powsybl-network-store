/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.network.store.model.NetworkVoltageRegulationAttributes;

final class NetworkVoltageRegulationAttributesMapper {

    private NetworkVoltageRegulationAttributesMapper() {
    }

    static NetworkVoltageRegulationAttributes map(VoltageRegulation.VoltageRegulationAttributes attributes) {
        if (attributes == null) {
            return null;
        }
        return NetworkVoltageRegulationAttributes.builder()
            .targetValue(attributes.targetValue())
            .targetDeadband(attributes.targetDeadband())
            .slope(attributes.slope())
            .mode(attributes.mode())
            .regulating(attributes.isRegulating())
            .terminal(TerminalRefUtils.getTerminalRefAttributes(attributes.terminal()))
            .build();
    }
}
