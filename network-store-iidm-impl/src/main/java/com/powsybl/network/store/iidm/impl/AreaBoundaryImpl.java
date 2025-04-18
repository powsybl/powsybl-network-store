/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;
import com.powsybl.network.store.model.AreaBoundaryAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Optional;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AreaBoundaryImpl implements AreaBoundary {

    protected final NetworkObjectIndex index;

    private final AreaBoundaryAttributes areaBoundaryAttributes;

    protected AreaBoundaryImpl(AreaBoundaryAttributes areaBoundaryAttributes, NetworkObjectIndex index) {
        this.index = index;
        this.areaBoundaryAttributes = areaBoundaryAttributes;
    }

    @Override
    public Area getArea() {
        return this.index.getArea(areaBoundaryAttributes.getAreaId()).orElse(null);
    }

    @Override
    public Optional<Terminal> getTerminal() {
        Terminal terminal = TerminalRefUtils.getTerminal(index, areaBoundaryAttributes.getTerminal());
        return Optional.ofNullable(terminal);
    }

    private Terminal getRefTerminal() {
        TerminalRefAttributes terminalRefAttributes = areaBoundaryAttributes.getTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    @Override
    public Optional<Boundary> getBoundary() {
        if (areaBoundaryAttributes.getBoundaryDanglingLineId() == null) {
            return Optional.empty();
        }
        Optional<DanglingLineImpl> danglingLine = index.getDanglingLine(areaBoundaryAttributes.getBoundaryDanglingLineId());
        return danglingLine.map(DanglingLineBoundaryImpl::new);
    }

    @Override
    public boolean isAc() {
        return areaBoundaryAttributes.getAc();
    }

    @Override
    public double getP() {
        return getBoundary().map(Boundary::getP).orElseGet(() -> getRefTerminal().getP());
    }

    @Override
    public double getQ() {
        return getBoundary().map(Boundary::getQ).orElseGet(() -> getRefTerminal().getQ());
    }
}
