/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.CgmesControlAreaAttributes;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesControlAreaImpl implements CgmesControlArea {

    private final NetworkImpl network;

    private final int index;

    CgmesControlAreaImpl(NetworkImpl network, int index) {
        this.network = Objects.requireNonNull(network);
        this.index = index;
    }

    private CgmesControlAreaAttributes getAttributes(Resource<NetworkAttributes> resource) {
        return network.getResource().getAttributes().getCgmesControlAreas().getControlAreas().get(index);
    }

    private CgmesControlAreaAttributes getAttributes() {
        return getAttributes(network.getResource());
    }

    @Override
    public String getId() {
        return getAttributes().getId();
    }

    @Override
    public String getName() {
        return getAttributes().getName();
    }

    @Override
    public String getEnergyIdentificationCodeEIC() {
        return getAttributes().getEnergyIdentificationCodeEic();
    }

    @Override
    public Set<Terminal> getTerminals() {
        return getAttributes().getTerminals().stream().map(a -> TerminalRefUtils.getTerminal(network.getIndex(), a)).collect(Collectors.toSet());
    }

    public static Boundary getTerminalBoundary(Terminal terminal) {
        if (terminal.getConnectable() instanceof DanglingLine) {
            return ((DanglingLine) terminal.getConnectable()).getBoundary();
        } else if (terminal.getConnectable() instanceof TieLine) {
            TieLine tieLine = (TieLine) terminal.getConnectable();
            Branch.Side side = terminal == tieLine.getHalf1().getTerminal() ? Branch.Side.ONE : Branch.Side.TWO;
            return tieLine.getHalf(side).getBoundary();
        } else {
            throw new IllegalStateException("Unexpected boundary component: " + terminal.getConnectable().getType());
        }
    }

    @Override
    public Set<Boundary> getBoundaries() {
        return getAttributes().getBoundaries().stream()
                .map(a -> TerminalRefUtils.getTerminal(network.getIndex(), a))
                .map(CgmesControlAreaImpl::getTerminalBoundary)
                .collect(Collectors.toSet());
    }

    @Override
    public double getNetInterchange() {
        return getAttributes().getNetInterchange();
    }

    @Override
    public void add(Terminal terminal) {
        network.updateResource(res -> getAttributes(res).getTerminals().add(TerminalRefUtils.getTerminalRefAttributes(terminal)));
    }

    public static Terminal getBoundaryTerminal(Boundary boundary) {
        Terminal t = boundary.getDanglingLine().getTerminal();
        if (t != null) {
            return t;
        } else {
            throw new IllegalStateException("Terminal of the dangling line linked to the boundary is null");
        }
    }

    @Override
    public void add(Boundary boundary) {
        Terminal terminal = getBoundaryTerminal(boundary);
        network.updateResource(res -> getAttributes(res).getBoundaries().add(TerminalRefUtils.getTerminalRefAttributes(terminal)));
    }
}
