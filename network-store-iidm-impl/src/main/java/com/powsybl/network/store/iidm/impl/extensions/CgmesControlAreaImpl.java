/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.NetworkObjectIndex;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.CgmesControlAreaAttributes;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesControlAreaImpl implements CgmesControlArea {

    private final NetworkObjectIndex index;

    private final CgmesControlAreaAttributes attributes;

    CgmesControlAreaImpl(NetworkObjectIndex index, CgmesControlAreaAttributes attributes) {
        this.index = Objects.requireNonNull(index);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public String getId() {
        return attributes.getId();
    }

    @Override
    public String getName() {
        return attributes.getName();
    }

    @Override
    public String getEnergyIdentificationCodeEIC() {
        return attributes.getEnergyIdentificationCodeEic();
    }

    @Override
    public Set<Terminal> getTerminals() {
        return attributes.getTerminals().stream().map(a -> TerminalRefUtils.getTerminal(index, a)).collect(Collectors.toSet());
    }

    private Boundary getTerminalBoundary(Terminal terminal) {
        if (terminal.getConnectable() instanceof DanglingLine) {
            return ((DanglingLine) terminal.getConnectable()).getBoundary();
        } else if (terminal.getConnectable() instanceof TieLine) {
            TieLine tieLine = (TieLine) terminal.getConnectable();
            Branch.Side side = terminal == tieLine.getTerminal1() ? Branch.Side.ONE : Branch.Side.TWO;
            return ((TieLine) terminal.getConnectable()).getHalf(side).getBoundary();
        } else {
            throw new IllegalStateException("Unexpected boundary component: " + terminal.getConnectable().getType());
        }
    }

    @Override
    public Set<Boundary> getBoundaries() {
        return attributes.getBoundaries().stream()
                .map(a -> TerminalRefUtils.getTerminal(index, a))
                .map(this::getTerminalBoundary)
                .collect(Collectors.toSet());
    }

    @Override
    public double getNetInterchange() {
        return attributes.getNetInterchange();
    }

    @Override
    public void add(Terminal terminal) {
        attributes.getTerminals().add(TerminalRefUtils.getTerminalRefAttributes(terminal));
    }

    private Terminal getBoundaryTerminal(Boundary boundary) {
        if (boundary.getConnectable() instanceof DanglingLine) {
            return ((DanglingLine) boundary.getConnectable()).getTerminal();
        } else if (boundary.getConnectable() instanceof TieLine) {
            return ((TieLine) boundary.getConnectable()).getTerminal(boundary.getSide());
        } else {
            throw new IllegalStateException("Unexpected boundary component: " + boundary.getConnectable().getType());
        }
    }

    @Override
    public void add(Boundary boundary) {
        Terminal terminal = getBoundaryTerminal(boundary);
        attributes.getBoundaries().add(TerminalRefUtils.getTerminalRefAttributes(terminal));
    }
}
