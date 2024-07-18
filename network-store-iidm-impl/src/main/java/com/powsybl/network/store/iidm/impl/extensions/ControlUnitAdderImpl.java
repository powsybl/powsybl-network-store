/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.ControlUnitAdder;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.ControlUnitAttributes;

import java.util.Objects;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
public class ControlUnitAdderImpl implements ControlUnitAdder {

    private final ControlZoneAdderImpl parent;

    private final NetworkImpl network;

    private String id;

    private boolean participate = true;

    ControlUnitAdderImpl(NetworkImpl network, ControlZoneAdderImpl parent) {
        this.network = Objects.requireNonNull(network);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public ControlUnitAdderImpl withId(String id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    @Override
    public ControlUnitAdderImpl withParticipate(boolean participate) {
        this.participate = participate;
        return this;
    }

    @Override
    public ControlZoneAdderImpl add() {
        if (id == null) {
            throw new PowsyblException("Control unit ID is not set");
        }
        ControlUnitAttributes controlUnitAttributes = ControlUnitAttributes.builder()
                .id(id)
                .participate(participate)
                .build();
        parent.addControlUnit(new ControlUnitImpl(network, controlUnitAttributes));
        return parent;
    }
}
