/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
abstract class AbstractAreaCharacteristics implements ObservabilityArea.AreaCharacteristics {

    protected final VoltageLevel voltageLevel;
    private final Characteristics characteristics;

    AbstractAreaCharacteristics(Characteristics characteristics, VoltageLevel voltageLevel) {
        this.characteristics = characteristics;
        this.voltageLevel = voltageLevel;
    }

    @Override
    public int getAreaNumber() {
        return characteristics.areaNumber;
    }

    @Override
    public ObservabilityArea.ObservabilityStatus getStatus() {
        return characteristics.status;
    }

    record Characteristics(int areaNumber, ObservabilityArea.ObservabilityStatus status) {
    }
}
