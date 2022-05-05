/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.InjectionAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalNodeBreakerViewImpl<U extends InjectionAttributes> implements Terminal.NodeBreakerView {

    private final NetworkObjectIndex index;

    private final U attributes;
    private final Connectable connectable;

    TerminalNodeBreakerViewImpl(NetworkObjectIndex index, U attributes, Connectable connectable) {
        this.index = Objects.requireNonNull(index);
        this.attributes = attributes;
        this.connectable = connectable;
    }

    @Override
    public int getNode() {
        Integer node = attributes.getNode();
        if (node == null) {
            throw new PowsyblException("Not supported in a bus breaker topology");
        }
        return node;
    }

    @Override
    public void moveConnectable(int node, String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (((AbstractIdentifiableImpl) connectable).optResource().isEmpty()) {
            throw new PowsyblException("Cannot modify removed equipment");
        }
        VoltageLevelImpl voltageLevel = index.getVoltageLevel(voltageLevelId)
                .orElseThrow(() -> new PowsyblException("Voltage level '" + voltageLevelId + "' not found"));
        if (voltageLevel.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            throw new PowsyblException("Trying to move connectable " + attributes.getResource().getId()
                    + " to node " + node + " of voltage level " + voltageLevelId + ", which is a bus breaker voltage level");
        }
        Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
        if (terminal != null) {
            throw new ValidationException(attributes.getResource(), "an equipment (" + terminal.getConnectable().getId()
                    + ") is already connected to node " + node + " of voltage level " + voltageLevelId);
        }
        attributes.setNode(node);
        attributes.setVoltageLevelId(voltageLevelId);
        voltageLevel.invalidateCalculatedBuses();
    }
}
