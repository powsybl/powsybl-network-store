/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
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
    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String REGULATED_RESOURCE_TYPE = "regulatedResourceType";

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
        var oldRegulatingTerminal = getAttributes().getRegulatingTerminal();
        getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(getAttributes().getLocalTerminal()),
            REGULATING_TERMINAL, oldRegulatingTerminal, () -> getAttributes().getRegulatingTerminal());

        var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
        getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(getAttributes().getRegulatingResourceType()),
            REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, () -> getAttributes().getRegulatedResourceType());
    }

    protected abstract <I extends Identifiable<I>, D extends IdentifiableAttributes> AbstractIdentifiableImpl<I, D> getIdentifiable();

    public void setRegulationMode(String attribute, String regulationMode) {
        String oldValue = getAttributes().getRegulationMode();
        getIdentifiable().updateResource(res -> getAttributes().setRegulationMode(regulationMode),
            attribute, oldValue, regulationMode);
    }

    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        if (regulatingTerminal != null) {
            TerminalImpl<?> regulatingTerminal1 = (TerminalImpl<?>) regulatingTerminal;
            regulatingTerminal1.setAsRegulatingPoint(this);

            var oldRegulatingTerminalAttributes = getAttributes().getRegulatingTerminal();
            TerminalRefAttributes attributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal1);
            getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(attributes),
                REGULATING_TERMINAL, oldRegulatingTerminalAttributes, attributes);

            var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
            ResourceType newRegulatedResourceType = ResourceType.convert(regulatingTerminal1.getConnectable().getType());
            getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(newRegulatedResourceType),
                REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, newRegulatedResourceType);
        } else {
            // Setting the regulating terminal to null returns the local terminal upon retrieval.
            // For consistency with the local terminal, we set the regulatedResourceType to correspond with the resource's own type.
            var oldRegulatingTerminalAttributes = getAttributes().getRegulatingTerminal();
            getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(null),
                REGULATING_TERMINAL, oldRegulatingTerminalAttributes, () -> null);

            var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
            getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(getRegulatingEquipmentType()),
                REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, () -> getAttributes().getRegulatedResourceType());
        }
    }

    protected abstract void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal, ReportNode reportNode);

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        // if regulating Terminal is null dont need to reset the regulation on the terminal because the terminal will be deleted
        if (regulatingTerminal != null) {
            regulatingTerminal.removeRegulatingPoint(this);
        }
    }

    public void removeRegulation(ReportNode reportNode) {
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        // set local terminal as regulating terminal
        resetRegulationToLocalTerminal();
        // rest regulation mode for equipment having one
        resetRegulationMode(regulatingTerminal, localTerminal, reportNode);
    }

    public Boolean isRegulating() {
        return getAttributes().getRegulating();
    }

    public void setRegulating(String attribute, boolean regulating) {
        Boolean oldValue = getAttributes().getRegulating();
        getIdentifiable().updateResource(res -> getAttributes().setRegulating(regulating),
            attribute, oldValue, regulating);
    }
}
