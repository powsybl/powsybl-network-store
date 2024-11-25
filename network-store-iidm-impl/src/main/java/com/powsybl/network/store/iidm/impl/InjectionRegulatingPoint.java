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

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class InjectionRegulatingPoint<I extends Injection<I>, D extends InjectionAttributes> extends AbstractRegulatingPoint {
    private final AbstractRegulatingEquipment<I, D> identifiable;

    public InjectionRegulatingPoint(NetworkObjectIndex index, AbstractRegulatingEquipment<I, D> identifiable, Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        super(index, attributesGetter);
        this.identifiable = identifiable;
    }

    private Resource<D> getResource() {
        return identifiable.getResource();
    }

    @Override
    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(getResource().getAttributes()).getRegulatingPoint();
    }

    private RegulatingPointAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRegulatingPoint();
    }

    @Override
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

    @Override
    public void resetRegulationToLocalTerminal() {
        identifiable.updateResource(res -> getAttributes(res).setRegulatingTerminal(getAttributes().getLocalTerminal()));
        identifiable.updateResource(res -> getAttributes(res).setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    @Override
    public void setRegulationMode(String regulationMode) {
        identifiable.updateResource(res -> getAttributes(res).setRegulationMode(regulationMode));
    }

    @Override
    protected void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal) {
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

    @Override
    public String getRegulatingEquipmentId() {
        return getAttributes().getRegulatingEquipmentId();
    }

    @Override
    public ResourceType getRegulatingEquipmentType() {
        return getAttributes().getRegulatingResourceType();
    }
}
