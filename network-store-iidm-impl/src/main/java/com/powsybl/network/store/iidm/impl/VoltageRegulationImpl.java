/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.HashSet;
import java.util.Set;

/**
 * Network-store implementation of {@link VoltageRegulation}.
 *
 * <p>The regulation state is persisted in the {@link com.powsybl.network.store.model.VoltageRegulationAttributes} of the current
 * variant resource of the regulating equipment. Terminal reference maintenance (registration on the
 * regulated terminal) is delegated to an {@link AbstractRegulatingPoint}.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class VoltageRegulationImpl implements VoltageRegulation, Referrer<Terminal> {

    private static final String VOLTAGE_REGULATION_PREFIX = "VoltageRegulation.";

    private final NetworkObjectIndex index;
    private final VoltageRegulationHolder<?> holder;
    private final Class<? extends VoltageRegulationHolder<?>> holderClass;
    private final Validable validable;
    private final AbstractRegulatingPoint regulatingPoint;
    private final String notificationPrefix;

    public VoltageRegulationImpl(NetworkObjectIndex index,
                                 VoltageRegulationHolder<?> holder,
                                 Class<? extends VoltageRegulationHolder<?>> holderClass,
                                 Validable validable,
                                 AbstractRegulatingPoint regulatingPoint,
                                 String notificationPrefix) {
        this.index = index;
        this.holder = holder;
        this.holderClass = holderClass;
        this.validable = validable;
        this.regulatingPoint = regulatingPoint;
        this.notificationPrefix = notificationPrefix;
    }

    private com.powsybl.network.store.model.VoltageRegulationAttributes getVoltageRegulationAttributes() {
        return (com.powsybl.network.store.model.VoltageRegulationAttributes) regulatingPoint.getAttributes();
    }

    private AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return regulatingPoint.getIdentifiable();
    }

    private Validable getValidable() {
        return validable;
    }

    private NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    /**
     * Build the powsybl-core attributes record of this voltage regulation, used for validation.
     */
    VoltageRegulation.VoltageRegulationAttributes toCoreAttributes() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        if (attributes == null) {
            return null;
        }
        return new VoltageRegulation.VoltageRegulationAttributes(
            attributes.getTargetValue(),
            attributes.getTargetDeadband(),
            attributes.getSlope(),
            getMode(),
            isRegulating(),
            regulatingPoint.getExplicitRegulatingTerminal());
    }

    private void checkAttributes(VoltageRegulation.VoltageRegulationAttributes newAttributes) {
        if (newAttributes != null) {
            NetworkImpl network = getNetwork();
            ValidationUtil.checkVoltageRegulation(getValidable(), newAttributes, network, holderClass,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
            if (network.getVariantManager().getVariantIds().size() > 1
                && regulatingPoint.getExplicitRegulatingTerminal() != newAttributes.terminal()) {
                throw new PowsyblException(getValidable().getMessageHeader() + "Cannot change terminal when there are multiple variants");
            }
        }
    }

    @Override
    public double getTargetValue() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes == null ? Double.NaN : attributes.getTargetValue();
    }

    @Override
    public VoltageRegulation setTargetValue(double targetValue) {
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withTargetValue(targetValue);
        checkAttributes(newAttributes);
        double oldValue = getTargetValue();
        if (Double.compare(targetValue, oldValue) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setTargetValue(targetValue),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "TargetValue", oldValue, targetValue);
        }
        return this;
    }

    @Override
    public double getTargetDeadband() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes == null ? Double.NaN : attributes.getTargetDeadband();
    }

    @Override
    public VoltageRegulation setTargetDeadband(double targetDeadband) {
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withTargetDeadband(targetDeadband);
        checkAttributes(newAttributes);
        double oldValue = getTargetDeadband();
        if (Double.compare(targetDeadband, oldValue) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setTargetDeadband(targetDeadband),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "TargetDeadband", oldValue, targetDeadband);
        }
        return this;
    }

    @Override
    public double getSlope() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes == null ? Double.NaN : attributes.getSlope();
    }

    @Override
    public VoltageRegulation setSlope(double slope) {
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withSlope(slope);
        checkAttributes(newAttributes);
        double oldValue = getSlope();
        if (Double.compare(slope, oldValue) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setSlope(slope),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "Slope", oldValue, slope);
        }
        return this;
    }

    @Override
    public Terminal getTerminal() {
        return regulatingPoint.getExplicitRegulatingTerminal();
    }

    /**
     * Register this regulation as a referrer of its regulating terminal, so that it is notified
     * when the terminal is removed. To be called when a regulation with an explicit terminal is
     * created or loaded.
     */
    public void registerOnTerminal() {
        Terminal terminal = getTerminal();
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        // The removal of a regulated equipment is already handled through the persisted
        // regulatingEquipments references (see TerminalImpl.removeAsRegulatingPoint). This callback
        // only covers the cases where the regulating point has not been cleaned yet.
        if (getTerminal() == removedTerminal) {
            regulatingPoint.removeRegulation(getNetwork().getReportNodeContext().getReportNode());
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldTerminal, Terminal newTerminal) {
        if (getTerminal() == oldTerminal) {
            regulatingPoint.setRegulatingTerminal(notificationPrefix + VOLTAGE_REGULATION_PREFIX + "Terminal", newTerminal);
        }
    }

    @Override
    public VoltageRegulation setTerminal(Terminal terminal, double targetValue) {
        if (getNetwork().getVariantManager().getVariantIds().size() > 1) {
            throw new PowsyblException(getValidable().getMessageHeader() + "Cannot set terminal when there are multiple variants");
        }
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withTerminalAndTargetValue(terminal, targetValue);
        checkAttributes(newAttributes);
        if (terminal == null) {
            ValidationUtil.checkLocalTargetQandV(getValidable(),
                holderClass,
                holder.getLocalTargetV(),
                holder.getLocalTargetQ(),
                false,
                isRegulating(),
                false,
                getMode(),
                getNetwork().getMinValidationLevel(),
                getNetwork().getReportNodeContext().getReportNode());
        }
        ValidationUtil.checkRegulatingTerminal(getValidable(), terminal, getNetwork());
        updateTerminal(terminal);
        getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setTargetValue(targetValue),
            notificationPrefix + VOLTAGE_REGULATION_PREFIX + "TargetValue", getTargetValue(), targetValue);
        return this;
    }

    @Override
    public boolean isWithTerminal() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes != null && attributes.getRegulatingTerminal() != null;
    }

    @Override
    public RegulationMode getMode() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes == null || attributes.getRegulationMode() == null ? null : RegulationMode.valueOf(attributes.getRegulationMode());
    }

    @Override
    public VoltageRegulation setMode(RegulationMode mode) {
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withMode(mode);
        checkAttributes(newAttributes);
        ValidationUtil.checkLocalTargetQandV(getValidable(),
            holderClass,
            holder.getLocalTargetV(),
            holder.getLocalTargetQ(),
            false,
            isRegulating(),
            isWithTerminal(),
            getMode(),
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());
        RegulationMode oldMode = getMode();
        if (mode != oldMode) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setRegulationMode(mode != null ? mode.name() : null),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "RegulationMode", oldMode, mode);
        }
        return this;
    }

    @Override
    public boolean isRegulating() {
        com.powsybl.network.store.model.VoltageRegulationAttributes attributes = getVoltageRegulationAttributes();
        return attributes != null && Boolean.TRUE.equals(attributes.getRegulating());
    }

    @Override
    public VoltageRegulation setRegulating(boolean regulating) {
        VoltageRegulation.VoltageRegulationAttributes newAttributes = toCoreAttributes().withRegulating(regulating);
        if (holder instanceof RatioTapChanger ratioTapChanger && validable instanceof TapChangerParent parent) {
            NetworkImpl network = getNetwork();
            ValidationUtil.checkRatioTapChangerRegulation(parent, newAttributes,
                ratioTapChanger.hasLoadTapChangingCapabilities(),
                network, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
            Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
            tapChangers.remove(holder);
            ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        }
        ValidationUtil.checkLocalTargetQandV(getValidable(),
            holderClass,
            holder.getLocalTargetV(),
            holder.getLocalTargetQ(),
            false,
            regulating,
            isWithTerminal(),
            getMode(),
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());
        checkAttributes(newAttributes);
        boolean oldValue = isRegulating();
        if (regulating != oldValue) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setRegulating(regulating),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "isRegulating", oldValue, regulating);
        }
        return this;
    }

    /**
     * Update all the attributes of this regulation on the current variant, following the powsybl-core
     * in-memory implementation (see {@code VoltageRegulationImpl.setAttributesOnCurrentVariant}).
     * Contrary to {@link #setTerminal(Terminal, double)}, setting the same terminal is allowed in a
     * multi-variant context.
     */
    public void setAttributesOnCurrentVariant(VoltageRegulation.VoltageRegulationAttributes attributes) {
        checkAttributes(attributes);
        // attributes are mutated directly, without the individual setter validations,
        // like in the powsybl-core in-memory implementation
        RegulationMode oldMode = getMode();
        if (attributes.mode() != oldMode) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setRegulationMode(attributes.mode() != null ? attributes.mode().name() : null),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "RegulationMode", oldMode, attributes.mode());
        }
        double oldSlope = getSlope();
        if (Double.compare(attributes.slope(), oldSlope) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setSlope(attributes.slope()),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "Slope", oldSlope, attributes.slope());
        }
        double oldTargetDeadband = getTargetDeadband();
        if (Double.compare(attributes.targetDeadband(), oldTargetDeadband) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setTargetDeadband(attributes.targetDeadband()),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "TargetDeadband", oldTargetDeadband, attributes.targetDeadband());
        }
        if (regulatingPoint.getExplicitRegulatingTerminal() != attributes.terminal()) {
            updateTerminal(attributes.terminal());
        }
        double oldTargetValue = getTargetValue();
        if (Double.compare(attributes.targetValue(), oldTargetValue) != 0) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setTargetValue(attributes.targetValue()),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "TargetValue", oldTargetValue, attributes.targetValue());
        }
        boolean oldRegulating = isRegulating();
        if (attributes.isRegulating() != oldRegulating) {
            getIdentifiable().updateResource(res -> getVoltageRegulationAttributes().setRegulating(attributes.isRegulating()),
                notificationPrefix + VOLTAGE_REGULATION_PREFIX + "isRegulating", oldRegulating, attributes.isRegulating());
        }
    }

    private void updateTerminal(Terminal newTerminal) {
        Terminal oldTerminal = getTerminal();
        if (oldTerminal != null) {
            ((TerminalImpl<?>) oldTerminal).getReferrerManager().unregister(this);
        }
        regulatingPoint.setRegulatingTerminal(notificationPrefix + VOLTAGE_REGULATION_PREFIX + "Terminal", newTerminal);
        if (newTerminal != null) {
            ((TerminalImpl<?>) newTerminal).getReferrerManager().register(this);
        }
    }

    /**
     * Unregister this regulation from the referenced terminal. To be called when the regulation
     * or the regulating equipment is removed.
     */
    public void onRemove() {
        Terminal terminal = getTerminal();
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
        }
        regulatingPoint.remove();
    }
}
