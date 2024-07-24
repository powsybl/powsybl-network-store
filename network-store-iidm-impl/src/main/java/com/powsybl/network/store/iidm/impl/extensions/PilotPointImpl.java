/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.PilotPointAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
class PilotPointImpl implements PilotPoint {

    private final NetworkImpl network;

    private final PilotPointAttributes pilotPointAttributes;

    PilotPointImpl(NetworkImpl network, PilotPointAttributes pilotPointAttributes) {
        this.pilotPointAttributes = Objects.requireNonNull(pilotPointAttributes);
        this.network = Objects.requireNonNull(network);
    }

    public PilotPointAttributes getPilotPointAttributes() {
        return pilotPointAttributes;
    }

    /**
     * Get pilot point busbar section ID or bus ID of the bus/breaker view.
     */
    @Override
    public List<String> getBusbarSectionsOrBusesIds() {
        return Collections.unmodifiableList(pilotPointAttributes.getBusbarSectionsOrBusesIds());
    }

    @Override
    public double getTargetV() {
        return pilotPointAttributes.getTargetV();
    }

    @Override
    public void setTargetV(double targetV) {
        if (Double.isNaN(targetV)) {
            throw new PowsyblException("Invalid pilot point target voltage'");
        }
        network.updateResource(res -> pilotPointAttributes.setTargetV(targetV));
    }
}
