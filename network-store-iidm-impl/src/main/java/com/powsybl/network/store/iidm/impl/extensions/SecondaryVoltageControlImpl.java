/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.SecondaryVoltageControlAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
public class SecondaryVoltageControlImpl extends AbstractExtension<Network> implements SecondaryVoltageControl {

    SecondaryVoltageControlImpl(Network network) {
        super(network);
    }

    SecondaryVoltageControlAttributes getSecondaryVoltageControlAttributes() {
        NetworkImpl network = (NetworkImpl) getExtendable();
        return (SecondaryVoltageControlAttributes) network.getResource().getAttributes().getExtensionAttributes().get(SecondaryVoltageControl.NAME);
    }

    @Override
    public List<ControlZone> getControlZones() {
        return getSecondaryVoltageControlAttributes().getControlZones().stream()
                .map(controlZoneAttributes -> (ControlZone) new ControlZoneImpl((NetworkImpl) getExtendable(), controlZoneAttributes))
                .toList();
    }

    @Override
    public Optional<ControlZone> getControlZone(String name) {
        Objects.requireNonNull(name);
        return getControlZones().stream().filter(z -> z.getName().equals(name)).findFirst();
    }
}
