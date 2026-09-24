/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * Common validation for native voltage regulation attributes and local targets.
 */
final class VoltageRegulationValidation {

    private VoltageRegulationValidation() {
    }

    static void checkAttributes(Validable validable,
                                VoltageRegulation.VoltageRegulationAttributes attributes,
                                Class<? extends VoltageRegulationHolder<?>> holderClass,
                                NetworkImpl network) {
        if (attributes != null) {
            ValidationUtil.checkVoltageRegulation(validable, attributes, network, holderClass,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        }
    }

    static void check(Validable validable,
                      VoltageRegulation.VoltageRegulationAttributes attributes,
                      Class<? extends VoltageRegulationHolder<?>> holderClass,
                      double localTargetV,
                      double localTargetQ,
                      NetworkImpl network) {
        checkAttributes(validable, attributes, holderClass, network);
        ValidationUtil.checkLocalTargetQandV(validable, holderClass,
            localTargetV, localTargetQ, attributes, network.getMinValidationLevel(),
            network.getReportNodeContext().getReportNode());
    }
}
