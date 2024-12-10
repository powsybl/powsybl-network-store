/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
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

    public RegulatingTapChangerType getRegulatingResourceSubType() {
        return getAttributes().getRegulatingResourceSubType();
    }

    public abstract RegulatingPointAttributes getAttributes();

    public Terminal getRegulatingTerminal() {
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
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
        getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(getAttributes().getLocalTerminal()));
        getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    protected abstract AbstractIdentifiableImpl getIdentifiable();

    public void setRegulationMode(String regulationMode) {
        getIdentifiable().updateResource(res -> getAttributes().setRegulationMode(regulationMode));
    }

    public void setRegulatingTerminal(TerminalImpl<?> regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        regulatingTerminal.setAsRegulatingPoint(this);
        getIdentifiable().updateResource(res -> getAttributes()
            .setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        getIdentifiable().updateResource(res -> getAttributes()
            .setRegulatedResourceType(ResourceType.convert(regulatingTerminal.getConnectable().getType())));
    }

    protected abstract void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal);

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        regulatingTerminal.removeRegulatingPoint(this);
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

    public Boolean isRegulating() {
        return getAttributes().getRegulating();
    }

    public void setRegulating(boolean regulating) {
        getIdentifiable().updateResource(res -> getAttributes().setRegulating(regulating));
    }
}
