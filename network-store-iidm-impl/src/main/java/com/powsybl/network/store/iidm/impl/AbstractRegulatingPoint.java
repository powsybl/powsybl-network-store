/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public abstract class AbstractRegulatingPoint {

    protected final NetworkObjectIndex index;
    protected final Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter;

    protected AbstractRegulatingPoint(NetworkObjectIndex index, Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        this.index = index;
        this.attributesGetter = attributesGetter;
    }

    public String getRegulatingEquipmentId() {
        return getAttributes().getRegulatingEquipmentId();
    }

    public ResourceType getRegulatingEquipmentType() {
        return getAttributes().getRegulatingResourceType();
    }

    public RegulatingTapChangerType getRegulatingTapChangerType() {
        return getAttributes().getRegulatingTapChangerType();
    }

    public abstract RegulatingPointAttributes getAttributes();

    public Terminal getRegulatingTerminal() {
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
    }

    public void resetRegulationToLocalTerminal() {
        getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(getAttributes().getLocalTerminal()));
        getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    protected abstract <I extends Identifiable<I>, D extends IdentifiableAttributes> AbstractIdentifiableImpl<I, D> getIdentifiable();

    public void setRegulationMode(String regulationMode) {
        getIdentifiable().updateResource(res -> getAttributes().setRegulationMode(regulationMode));
    }

    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        if (regulatingTerminal != null) {
            TerminalImpl<?> regulatingTerminal1 = (TerminalImpl<?>) regulatingTerminal;
            regulatingTerminal1.setAsRegulatingPoint(this);
            getIdentifiable().updateResource(res -> getAttributes()
                .setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal1)));
            getIdentifiable().updateResource(res -> getAttributes()
                .setRegulatedResourceType(ResourceType.convert(regulatingTerminal1.getConnectable().getType())));
        } else {
            // Setting the regulating terminal to null returns the local terminal upon retrieval.
            // For consistency with the local terminal, we set the regulatedResourceType to correspond with the resource's own type.
            getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(null));
            getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(getRegulatingEquipmentType()));
        }
    }

    protected abstract void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal);

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        // if regulating Terminal is null dont need to reset the regulation on the terminal because the terminal will be deleted
        if (regulatingTerminal != null) {
            regulatingTerminal.removeRegulatingPoint(this);
        }
    }

    public void removeRegulation() {
        Terminal localTerminal = TerminalRefUtils.getTerminal(index,
            getAttributes().getLocalTerminal());
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        // set local terminal as regulating terminal
        resetRegulationToLocalTerminal();
        // rest regulation mode for equipment having one
        resetRegulationMode(regulatingTerminal, localTerminal);
    }

    public Boolean isRegulating() {
        return getAttributes().getRegulating();
    }

    public void setRegulating(boolean regulating) {
        getIdentifiable().updateResource(res -> getAttributes().setRegulating(regulating));
    }
}
