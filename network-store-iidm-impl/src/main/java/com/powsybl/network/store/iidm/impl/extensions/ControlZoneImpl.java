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
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.ControlZoneAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
class ControlZoneImpl implements ControlZone {
    private final ControlZoneAttributes controlZoneAttributes;

    private final NetworkImpl network;

    ControlZoneImpl(NetworkImpl network, ControlZoneAttributes controlZoneAttributes) {
        this.network = Objects.requireNonNull(network);
        this.controlZoneAttributes = Objects.requireNonNull(controlZoneAttributes);
    }

    public ControlZoneAttributes getControlZoneAttributes() {
        return controlZoneAttributes;
    }

    @Override
    public String getName() {
        return controlZoneAttributes.getName();
    }

    @Override
    public PilotPoint getPilotPoint() {
        return new PilotPointImpl(network, controlZoneAttributes.getPilotPoint());
    }

    @Override
    public List<ControlUnit> getControlUnits() {
        return controlZoneAttributes.getControlUnits().stream()
                .map(controlUnitAttributes -> (ControlUnit) new ControlUnitImpl(network, controlUnitAttributes))
                .toList();
    }

    @Override
    public Optional<ControlUnit> getControlUnit(String id) {
        Objects.requireNonNull(id);
        return getControlUnits().stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
