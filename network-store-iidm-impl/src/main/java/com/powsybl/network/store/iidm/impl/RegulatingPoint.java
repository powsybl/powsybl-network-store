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
public final class RegulatingPoint <I extends Injection<I>, D extends InjectionAttributes> {
    private final NetworkObjectIndex index;
    private final AbstractRegulatingEquipment<I, D> identifiable;
    private final Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter;

    public RegulatingPoint(NetworkObjectIndex index, AbstractRegulatingEquipment<I, D> identifiable, Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        this.index = index;
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        this.identifiable = identifiable;
    }

    private Resource<D> getResource() {
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
        identifiable.updateResource(res -> getAttributes(res)
            .setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        identifiable.updateResource(res -> getAttributes(res)
            .setRegulatedResourceType(ResourceType.convert(regulatingTerminal.getConnectable().getType())));
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
        identifiable.updateResource(res -> getAttributes(res).setRegulatingTerminal(getAttributes().getLocalTerminal()));
        identifiable.updateResource(res -> getAttributes(res).setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    public void setRegulationMode(String regulationModeOrdinal) {
        identifiable.updateResource(res -> getAttributes(res).setRegulationMode(regulationModeOrdinal));
    }

    public void removeRegulation() {
        Terminal terminal = TerminalRefUtils.getTerminal(index,
            getAttributes().getLocalTerminal());
        if (terminal instanceof TerminalImpl<?> localTerminal) {
            Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
            // set local terminal as regulating terminal
            resetRegulationToLocalTerminal();
            // rest regulation mode for equipment having one
            resetRegulationMode(regulatingTerminal, localTerminal);
        } else {
            throw new PowsyblException("Cannot remove regulation because the local terminal is null");
        }
    }

    private void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() &&
            !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getRegulatingResourceType()) {
                // for svc we set the regulation mode to Off if the regulation was not on the same bus than the svc. If the svc is on the same bus were the equipment was remove we keep the regulation
                case STATIC_VAR_COMPENSATOR ->
                    setRegulationMode(String.valueOf(StaticVarCompensator.RegulationMode.OFF));
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
        return getAttributes().getRegulatingEquipmentId();
    }

    public ResourceType getRegulatingEquipmentType() {
        return getAttributes().getRegulatingResourceType();
    }
}
