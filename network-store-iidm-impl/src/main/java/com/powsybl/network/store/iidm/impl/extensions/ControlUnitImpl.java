/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlUnit;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.ControlUnitAttributes;

import java.util.Objects;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
class ControlUnitImpl implements ControlUnit {
    private final SecondaryVoltageControl secondaryVoltageControl;

    private final ControlZone controlZone;

    private final ControlUnitAttributes controlUnitAttributes;

    private final NetworkImpl network;

    public ControlUnitImpl(SecondaryVoltageControl secondaryVoltageControl, ControlZone controlZone, NetworkImpl network, ControlUnitAttributes controlUnitAttributes) {
        this.secondaryVoltageControl = secondaryVoltageControl;
        this.controlZone = controlZone;
        this.controlUnitAttributes = Objects.requireNonNull(controlUnitAttributes);
        this.network = Objects.requireNonNull(network);
    }

    public ControlUnitAttributes getControlUnitAttributes() {
        return controlUnitAttributes;
    }

    @Override
    public String getId() {
        return controlUnitAttributes.getId();
    }

    @Override
    public boolean isParticipate() {
        return controlUnitAttributes.isParticipate();
    }

    @Override
    public void setParticipate(boolean participate) {
        boolean oldValue = isParticipate();
        if (oldValue != participate) {
            network.updateResourceExtension(secondaryVoltageControl, res -> controlUnitAttributes.setParticipate(participate), "participate for control unit " + getId() + " in control zone " + controlZone.getName(), oldValue, participate);
        }
    }
}
