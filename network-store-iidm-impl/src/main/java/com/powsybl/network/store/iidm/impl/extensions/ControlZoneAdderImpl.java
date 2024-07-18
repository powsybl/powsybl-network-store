/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.ControlZoneAdder;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.ControlZoneAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
public class ControlZoneAdderImpl implements ControlZoneAdder {

    private final SecondaryVoltageControlAdderImpl parent;

    private final NetworkImpl network;

    private String name;

    private PilotPointImpl pilotPoint;

    private final List<ControlUnitImpl> controlUnits = new ArrayList<>();

    ControlZoneAdderImpl(NetworkImpl network, SecondaryVoltageControlAdderImpl parent) {
        this.network = Objects.requireNonNull(network);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public ControlZoneAdderImpl withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    void setPilotPoint(PilotPointImpl pilotPoint) {
        this.pilotPoint = Objects.requireNonNull(pilotPoint);
    }

    @Override
    public PilotPointAdderImpl newPilotPoint() {
        return new PilotPointAdderImpl(network, this);
    }

    void addControlUnit(ControlUnitImpl controlUnit) {
        controlUnits.add(Objects.requireNonNull(controlUnit));
    }

    @Override
    public ControlUnitAdderImpl newControlUnit() {
        return new ControlUnitAdderImpl(network, this);
    }

    @Override
    public SecondaryVoltageControlAdderImpl add() {
        if (name == null) {
            throw new PowsyblException("Zone name is not set");
        }
        if (pilotPoint == null) {
            throw new PowsyblException("Pilot point is not set for zone '" + name + "'");
        }
        if (controlUnits.isEmpty()) {
            throw new PowsyblException("Empty control unit list for zone '" + name + "'");
        }
        ControlZoneAttributes controlZoneAttributes = ControlZoneAttributes.builder()
                .name(name)
                .pilotPoint(pilotPoint.getPilotPointAttributes())
                .controlUnits(controlUnits.stream().map(ControlUnitImpl::getControlUnitAttributes).toList())
                .build();
        ControlZoneImpl controlZone = new ControlZoneImpl(network, controlZoneAttributes);
        parent.addControlZone(controlZone);
        return parent;
    }
}
