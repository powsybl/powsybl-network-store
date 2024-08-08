/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;
import lombok.Getter;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Getter
public class RegulatingPoint {

    private final StaticVarCompensatorImpl staticVarCompensator;
    private final NetworkObjectIndex index;
    private final Function<Attributes, StaticVarCompensatorAttributes> attributesGetter;

    public RegulatingPoint(NetworkObjectIndex index, StaticVarCompensatorImpl staticVarCompensator, Function<Attributes, StaticVarCompensatorAttributes> attributesGetter) {
        this.index = index;
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        this.staticVarCompensator = staticVarCompensator;
    }

    protected Resource<StaticVarCompensatorAttributes> getSvcResource() {
        return staticVarCompensator.getResource();
    }

    protected RegulationPointAttributes getAttributes() {
        return attributesGetter.apply(getSvcResource().getAttributes()).getRegulationPoint();
    }

    protected RegulationPointAttributes getAttributes(Resource resource) {
        return attributesGetter.apply(resource.getAttributes()).getRegulationPoint();
    }

    public Terminal getRegulatingTerminal() {
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
    }

    public void setRegulatingTerminal(TerminalImpl regulatingTerminal) {
        TerminalImpl oldRegulatingTerminal = (TerminalImpl) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        oldRegulatingTerminal.removeRegulatingPoint(this);
        regulatingTerminal.addNewRegulatingPoint(this);
        staticVarCompensator.updateResource(res -> getAttributes(res).setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
    }

    public void setRegulatingTerminalAsLocalTerminal() {
        staticVarCompensator.updateResource(res -> getAttributes(res).setRegulatingTerminal(getAttributes().getLocalTerminal()));
    }

    public void setRegulationMode(int regulationModeOrdinal) {
        staticVarCompensator.updateResource(res -> getAttributes(res).setRegulationMode(regulationModeOrdinal));
    }

    public void removeRegulation() {
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        staticVarCompensator.updateResource(res -> getAttributes(res).setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(localTerminal)));
        if (!localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getIdentifiableType()) {
                case STATIC_VAR_COMPENSATOR -> staticVarCompensator.updateResource(res -> getAttributes().setRegulationMode(StaticVarCompensator.RegulationMode.OFF.ordinal()), null);
            }
        }
    }

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        regulatingTerminal.removeRegulatingPoint(this);
    }
}
