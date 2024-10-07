/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public record RegulatingPoint(NetworkObjectIndex index, AbstractIdentifiableImpl identifiable,
                              Function<Attributes, AbstractIdentifiableAttributes> attributesGetter) {

    public RegulatingPoint(NetworkObjectIndex index, AbstractIdentifiableImpl identifiable, Function<Attributes, AbstractIdentifiableAttributes> attributesGetter) {
        this.index = index;
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        this.identifiable = identifiable;
    }

    private Resource<AbstractIdentifiableAttributes> getResource() {
        return identifiable.getResource();
    }

    private RegulationPointAttributes getAttributes() {
        return attributesGetter.apply(getResource().getAttributes()).getRegulationPoint();
    }

    private RegulationPointAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRegulationPoint();
    }

    public Terminal getRegulatingTerminal() {
        // FIXME when deleting a element the regulating terminal are not set to their original terminal it creates bugs
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        Terminal regulatingTerminal;
        if (index.getIdentifiable(getAttributes().getRegulatingTerminal().getConnectableId()) == null) {
            setRegulatingTerminalAsLocalTerminal();
            regulatingTerminal = localTerminal;
        } else {
            regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        }
        return regulatingTerminal;
    }

    public void setRegulatingTerminal(TerminalImpl<?> regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        oldRegulatingTerminal.removeRegulatingPoint(this);
        regulatingTerminal.addNewRegulatingPoint(this);
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
    }

    public void setRegulatingTerminalAsLocalTerminal() {
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulatingTerminal(getAttributes().getLocalTerminal()));
    }

    public void setRegulationMode(String regulationModeOrdinal) {
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulationMode(regulationModeOrdinal));
    }

    public void removeRegulation() {
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(localTerminal)));
        if (!localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getResourceType()) {
                case STATIC_VAR_COMPENSATOR ->
                    identifiable.updateResource(res -> getAttributes().setRegulationMode(String.valueOf(StaticVarCompensator.RegulationMode.OFF)), null);
                case GENERATOR, SHUNT_COMPENSATOR, VSC_CONVERTER_STATION -> {
                }
                default -> throw new PowsyblException("No regulation for this kind of equipment");
            }
        }
    }

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        regulatingTerminal.removeRegulatingPoint(this);
    }
}
