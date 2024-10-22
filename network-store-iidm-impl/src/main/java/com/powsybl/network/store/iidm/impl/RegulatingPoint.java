/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.*;
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

    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(getResource().getAttributes()).getRegulatingPoint();
    }

    private RegulatingPointAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRegulatingPoint();
    }

    public Terminal getRegulatingTerminal() {
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
    }

    public void setRegulatingTerminal(TerminalImpl<?> regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        regulatingTerminal.setAsRegulatingPoint(this);
        identifiable.updateResource(res -> getAttributes((Resource<?>) res)
            .setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        identifiable.updateResource(res -> getAttributes((Resource<?>) res)
            .setRegulatingResourceType(ResourceType.convert(regulatingTerminal.getConnectable().getType())));
    }

    public void setRegulatingTerminalAsLocalTerminalAndRemoveRegulation() {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        resetRegulationToLocalTerminal();
    }

    public void resetRegulationToLocalTerminal() {
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulatingTerminal(getAttributes().getLocalTerminal()));
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulatingResourceType(getAttributes().getResourceType()));
    }

    public void setRegulationMode(String regulationModeOrdinal) {
        identifiable.updateResource(res -> getAttributes((Resource<?>) res).setRegulationMode(regulationModeOrdinal));
    }

    public void removeRegulation() {
        TerminalImpl localTerminal = (TerminalImpl) TerminalRefUtils.getTerminal(index,
            getAttributes().getLocalTerminal());
        if (localTerminal == null) {
            throw new PowsyblException("Cannot remove regulation because the local terminal is null");
        }
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        // set local terminal as regulating terminal
        resetRegulationToLocalTerminal();
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() && !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
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

    public String getRegulatingEquipmentId() {
        return getAttributes().getRegulatedEquipmentId();
    }

    public ResourceType getRegulatingEquipmentType() {
        return getAttributes().getResourceType();
    }
}
