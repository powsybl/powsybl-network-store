/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InjectionAttributes;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalBusViewImpl<U extends InjectionAttributes> implements Terminal.BusView {

    private final NetworkObjectIndex index;

    private final U attributes;

    TerminalBusViewImpl(NetworkObjectIndex index, U attributes) {
        this.index = Objects.requireNonNull(index);
        this.attributes = attributes;
    }

    private boolean test(CalculateBus b) {
        return b.getVertices().stream().anyMatch(vertex -> vertex.getNode() == attributes.getNode());
    }

    @Override
    public Bus getBus() {
        VoltageLevel voltageLevel = index.getVoltageLevel(attributes.getVoltageLevelId()).orElseThrow(IllegalStateException::new);
        return voltageLevel.getBusView().getBusStream()
                .map(CalculateBus.class::cast)
                .filter(this::test)
                .findFirst()
                .orElseGet(() -> new CalculateBus(index, attributes.getVoltageLevelId(), "", "", Collections.emptyList())); // FIXME should not happen
    }

    @Override
    public Bus getConnectableBus() {
        throw new UnsupportedOperationException("TODO");
    }
}
