/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.CgmesControlAreaAttributes;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesControlAreaImpl implements CgmesControlArea {

    private final CgmesControlAreas parent;

    private final NetworkImpl network;

    private final int index;

    CgmesControlAreaImpl(CgmesControlAreas parent, NetworkImpl network, int index) {
        this.parent = parent;
        this.network = Objects.requireNonNull(network);
        this.index = index;
    }

    private CgmesControlAreaAttributes getAttributes(Resource<NetworkAttributes> resource) {
        return resource.getAttributes().getCgmesControlAreas().getControlAreas().get(index);
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
            TwoSides side = terminal == tieLine.getDanglingLine1().getTerminal() ? TwoSides.ONE : TwoSides.TWO;
            return tieLine.getDanglingLine(side).getBoundary();
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
    public void setNetInterchange(double netInterchange) {
        double oldValue = getNetInterchange();
        if (oldValue != netInterchange) {
            network.updateResourceExtension(parent, res -> getAttributes(res).setNetInterchange(netInterchange), "netInterchange", oldValue, netInterchange);
        }
    }

    @Override
    public void add(Terminal terminal) {
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(terminal);
        network.updateResourceExtension(parent, res -> getAttributes(res).getTerminals().add(terminalRefAttributes), "terminal", null, terminalRefAttributes);
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
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(terminal);
        network.updateResourceExtension(parent, res -> getAttributes(res).getBoundaries().add(terminalRefAttributes), "boundary", null, terminalRefAttributes);
    }

    @Override
    public double getPTolerance() {
        return getAttributes().getPTolerance();
    }

    @Override
    public void setPTolerance(double pTolerance) {
        double oldValue = getPTolerance();
        if (oldValue != pTolerance) {
            network.updateResourceExtension(parent, res -> getAttributes(res).setPTolerance(pTolerance), "pTolerance", oldValue, pTolerance);
        }
    }
}
