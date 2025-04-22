/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaBoundaryAdder;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.AreaBoundaryAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AreaBoundaryAdderImpl implements AreaBoundaryAdder {
    AreaImpl area;

    Boundary boundary;

    Terminal terminal;

    Boolean ac;

    NetworkObjectIndex index;

    AreaBoundaryAdderImpl(AreaImpl area, NetworkObjectIndex index) {
        this.area = Objects.requireNonNull(area);
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public AreaBoundaryAdder setBoundary(Boundary boundary) {
        this.boundary = boundary;
        this.terminal = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        this.boundary = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setAc(boolean ac) {
        this.ac = ac;
        return this;
    }

    @Override
    public Area add() {
        if (ac == null) {
            throw new PowsyblException("AreaBoundary AC flag is not set.");
        }
        // we remove before adding, to forbid duplicates and allow updating ac to true/false
        AreaBoundaryAttributes areaBoundaryAttributes;
        if (boundary != null) {
            areaBoundaryAttributes = new AreaBoundaryAttributes(null, ac, area.getId(), boundary.getDanglingLine().getId());
            area.removeAreaBoundary(boundary);
        } else if (terminal != null) {
            TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(terminal);
            areaBoundaryAttributes = new AreaBoundaryAttributes(terminalRefAttributes, ac, area.getId(), null);
            area.removeAreaBoundary(terminal);
        } else {
            throw new PowsyblException("No AreaBoundary element (terminal or boundary) is set.");
        }
        area.addAreaBoundary(new AreaBoundaryImpl(areaBoundaryAttributes, index));
        return area;
    }
}
