/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public abstract class AbstractRegulatingPoint {

    protected final NetworkObjectIndex index;
    protected final Function<Attributes, ? extends RegulatingPointAttributes> attributesGetter;
    private final String terminalAttribute;
    private static final String REGULATED_RESOURCE_TYPE = "regulatedResourceType";

    protected AbstractRegulatingPoint(NetworkObjectIndex index, Function<Attributes, ? extends RegulatingPointAttributes> attributesGetter, String terminalAttribute) {
        this.index = index;
        this.attributesGetter = attributesGetter;
        this.terminalAttribute = terminalAttribute;
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

    /**
     * Get the regulating point attributes. Can be null when the regulating equipment
     * has no regulation defined (e.g. no voltage regulation for a voltage regulation holder).
     */
    public abstract RegulatingPointAttributes getAttributes();

    /**
     * Get the terminal explicitly set as regulating terminal, or null if none was set.
     * Unlike {@link #getRegulatingTerminal()}, no fallback on the local terminal is done.
     */
    public Terminal getExplicitRegulatingTerminal() {
        RegulatingPointAttributes attributes = getAttributes();
        return attributes == null ? null : TerminalRefUtils.getTerminal(index, attributes.getRegulatingTerminal());
    }

    public Terminal getRegulatingTerminal() {
        RegulatingPointAttributes attributes = getAttributes();
        if (attributes == null) {
            return null;
        }
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, attributes.getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, attributes.getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
    }

    public Terminal getLocalTerminal() {
        RegulatingPointAttributes attributes = getAttributes();
        return attributes == null ? null : TerminalRefUtils.getTerminal(index, attributes.getLocalTerminal());
    }

    /**
     * Set the regulating terminal without updating the reverse references on the terminals.
     * Used when the regulated equipment is being removed: its regulating equipments set is being
     * iterated and cleared by the caller, so it must not be modified here.
     */
    private void setRegulatingTerminalWithoutReverseReferenceUpdate(Terminal regulatingTerminal) {
        var oldRegulatingTerminalAttributes = getAttributes().getRegulatingTerminal();
        TerminalRefAttributes newAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(newAttributes),
            terminalAttribute, oldRegulatingTerminalAttributes, newAttributes);

        var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
        var newRegulatedResourceType = regulatingTerminal != null
            ? ((AbstractIdentifiableImpl<?, ?>) regulatingTerminal.getConnectable()).getResource().getType()
            : getAttributes().getRegulatingResourceType();
        getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(newRegulatedResourceType),
            REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, newRegulatedResourceType);
    }

    protected abstract <I extends Identifiable<I>, D extends IdentifiableAttributes> AbstractIdentifiableImpl<I, D> getIdentifiable();

    public void setRegulationMode(String attribute, String regulationMode) {
        String oldValue = getAttributes().getRegulationMode();
        getIdentifiable().updateResource(res -> getAttributes().setRegulationMode(regulationMode),
            attribute, oldValue, regulationMode);
    }

    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        setRegulatingTerminal(terminalAttribute, regulatingTerminal);
    }

    public void setRegulatingTerminal(String attribute, Terminal regulatingTerminal) {
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
                attribute, oldRegulatingTerminalAttributes, attributes);

            var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
            ResourceType newRegulatedResourceType = ((AbstractIdentifiableImpl<?, ?>) regulatingTerminal1.getConnectable()).getResource().getType();
            getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(newRegulatedResourceType),
                REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, newRegulatedResourceType);
        } else {
            // Setting the regulating terminal to null returns the local terminal upon retrieval.
            // For consistency with the local terminal, we set the regulatedResourceType to correspond with the resource's own type.
            var oldRegulatingTerminalAttributes = getAttributes().getRegulatingTerminal();
            getIdentifiable().updateResource(res -> getAttributes().setRegulatingTerminal(null),
                attribute, oldRegulatingTerminalAttributes, null);

            var oldRegulatedResourceType = getAttributes().getRegulatedResourceType();
            var regulatingEquipmentType = getRegulatingEquipmentType();
            getIdentifiable().updateResource(res -> getAttributes().setRegulatedResourceType(regulatingEquipmentType),
                REGULATED_RESOURCE_TYPE, oldRegulatedResourceType, regulatingEquipmentType);
        }
    }

    void remove() {
        RegulatingPointAttributes attributes = getAttributes();
        if (attributes == null) {
            return;
        }
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, attributes.getRegulatingTerminal());
        // if regulating Terminal is null dont need to reset the regulation on the terminal because the terminal will be deleted
        if (regulatingTerminal != null) {
            regulatingTerminal.removeRegulatingPoint(this);
        }
    }

    /**
     * Called when the regulated equipment is removed.
     * Following the iidm in-memory implementation (see VoltageRegulationImpl), if the regulating terminal
     * is on the same bus as the local terminal, the regulation is relocated on the local terminal and stays
     * active. Otherwise the regulating terminal is unset (which means local regulation) and the regulation
     * is deactivated.
     */
    public void removeRegulation(ReportNode reportNode) {
        RegulatingPointAttributes attributes = getAttributes();
        if (attributes == null) {
            return;
        }
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, attributes.getLocalTerminal());
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, attributes.getRegulatingTerminal());
        if (regulatingTerminal == null) {
            return;
        }
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        // (buses are compared by id because the network store may materialize several instances of the same bus)
        Bus bus = regulatingTerminal.getBusView().getBus();
        Bus localBus = localTerminal != null ? localTerminal.getBusView().getBus() : null;
        if (localTerminal != null && bus != null && localBus != null && bus.getId().equals(localBus.getId()) && canRelocateOnLocalTerminal()) {
            // regulation point is re-located at the regulating equipment, regulation stays on
            // the reverse reference on the removed terminal is not cleaned here: the whole regulating
            // equipments set of the removed terminal is being iterated and cleared by the caller
            setRegulatingTerminalWithoutReverseReferenceUpdate(localTerminal);
            return;
        }
        // unset the regulating terminal (regulation falls back to the local terminal)
        setRegulatingTerminalWithoutReverseReferenceUpdate(null);
        // reset regulating and regulation mode for equipment having one
        resetRegulatingAndRegulationMode(regulatingTerminal, localTerminal, reportNode);
    }

    /**
     * Whether the regulation can stay active by being relocated on the local terminal when the regulated
     * equipment is removed. Always true except for specific equipments (e.g. static var compensators
     * regulating in reactive power mode).
     */
    protected boolean canRelocateOnLocalTerminal() {
        return true;
    }

    protected abstract void resetRegulatingAndRegulationMode(Terminal regulatingTerminal, Terminal localTerminal, ReportNode reportNode);

    public Boolean isRegulating() {
        RegulatingPointAttributes attributes = getAttributes();
        return attributes != null && Boolean.TRUE.equals(attributes.getRegulating());
    }

    public void setRegulating(String attribute, boolean regulating) {
        Boolean oldValue = getAttributes().getRegulating();
        getIdentifiable().updateResource(res -> getAttributes().setRegulating(regulating),
            attribute, oldValue, regulating);
    }
}
