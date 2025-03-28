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
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalNodeBreakerViewImpl<U extends IdentifiableAttributes> implements Terminal.NodeBreakerView {

    private final NetworkObjectIndex index;

    private final Connectable<?> connectable;

    private final Function<Resource<U>, InjectionAttributes> attributesGetter;

    TerminalNodeBreakerViewImpl(NetworkObjectIndex index, Connectable<?> connectable, Function<Resource<U>, InjectionAttributes> attributesGetter) {
        this.index = Objects.requireNonNull(index);
        this.connectable = connectable;
        this.attributesGetter = attributesGetter;
    }

    private AbstractIdentifiableImpl<?, U> getAbstractIdentifiable() {
        return (AbstractIdentifiableImpl<?, U>) connectable;
    }

    private InjectionAttributes getAttributes() {
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Cannot modify removed equipment " + connectable.getId());
        }
        return getAttributes(getAbstractIdentifiable().getResource());
    }

    private InjectionAttributes getAttributes(Resource<U> resource) {
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Cannot modify removed equipment " + connectable.getId());
        }
        return attributesGetter.apply(resource);
    }

    private VoltageLevelImpl getVoltageLevel() {
        return index.getVoltageLevel(getAttributes().getVoltageLevelId()).orElseThrow(IllegalStateException::new);
    }

    @Override
    public int getNode() {
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Cannot access node of removed equipment " + connectable.getId());
        }
        Integer node = getAttributes().getNode();
        if (node == null) {
            throw new PowsyblException("Not supported in a bus breaker topology");
        }
        return node;
    }

    @Override
    public void moveConnectable(int node, String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (getAbstractIdentifiable().getOptionalResource().isEmpty()) {
            throw new PowsyblException("Cannot modify removed equipment");
        }
        var attributes = getAttributes();
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
        VoltageLevelImpl oldVoltageLevel = getVoltageLevel();
        getAbstractIdentifiable().updateResource(res -> {
            InjectionAttributes attr = getAttributes(res);
            attr.setConnectableBus(null);
            attr.setBus(null);
            attr.setNode(node);
            attr.setVoltageLevelId(voltageLevelId);
        }, "injectionAttributes", attributes, () -> getAttributes(getAbstractIdentifiable().getResource()));
        oldVoltageLevel.invalidateCalculatedBuses();
        voltageLevel.invalidateCalculatedBuses();
    }
}
