/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License as published by the Free Software Foundation, either version 2.0
 * of the License, or (at your option) any later version.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.NetworkVoltageRegulationAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Resource-backed implementation of the common IIDM voltage regulation API.
 */
public final class VoltageRegulationImpl implements VoltageRegulation, Referrer<Terminal> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageRegulationImpl.class);

    @FunctionalInterface
    public interface AttributesUpdater {
        void update(Consumer<NetworkVoltageRegulationAttributes> modifier,
                    String attribute, Object oldValue, Object newValue);
    }

    private final Validable validable;
    private final VoltageRegulationHolder<?> holder;
    private final Class<? extends VoltageRegulationHolder<?>> holderClass;
    private final NetworkObjectIndex index;
    private final Supplier<NetworkVoltageRegulationAttributes> attributesSupplier;
    private final AttributesUpdater attributesUpdater;
    private final AbstractRegulatingPoint regulatingPoint;
    private final String regulatingEquipmentId;
    private final Object regulatingEquipmentType;
    private final Object regulatingTapChangerType;
    private final boolean hasRegulatingPointAttributes;

    private Terminal terminal;

    public VoltageRegulationImpl(Validable validable,
                                 VoltageRegulationHolder<?> holder,
                                 Class<? extends VoltageRegulationHolder<?>> holderClass,
                                 NetworkObjectIndex index,
                                 Supplier<NetworkVoltageRegulationAttributes> attributesSupplier,
                                 AttributesUpdater attributesUpdater,
                                 AbstractRegulatingPoint regulatingPoint) {
        this.validable = Objects.requireNonNull(validable);
        this.holder = Objects.requireNonNull(holder);
        this.holderClass = Objects.requireNonNull(holderClass);
        this.index = Objects.requireNonNull(index);
        this.attributesSupplier = Objects.requireNonNull(attributesSupplier);
        this.attributesUpdater = Objects.requireNonNull(attributesUpdater);
        this.regulatingPoint = regulatingPoint;
        var regulatingPointAttributes = regulatingPoint == null ? null : regulatingPoint.getAttributes();
        this.hasRegulatingPointAttributes = regulatingPointAttributes != null;
        this.regulatingEquipmentId = regulatingPointAttributes == null ? null : regulatingPointAttributes.getRegulatingEquipmentId();
        this.regulatingEquipmentType = regulatingPointAttributes == null ? null : regulatingPointAttributes.getRegulatingResourceType();
        this.regulatingTapChangerType = regulatingPointAttributes == null ? null : regulatingPointAttributes.getRegulatingTapChangerType();
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        if (attributes != null && attributes.getTerminal() != null) {
            updateTerminal(TerminalRefUtils.getTerminal(index, attributes.getTerminal()));
        }
    }

    @Override
    public double getTargetValue() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes == null ? Double.NaN : attributes.getTargetValue();
    }

    @Override
    public VoltageRegulation setTargetValue(double targetValue) {
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setTargetValue(targetValue);
        checkAttributes(newAttributes);
        updateAttribute("targetValue", attributes.getTargetValue(), targetValue, a -> a.setTargetValue(targetValue));
        return this;
    }

    @Override
    public double getTargetDeadband() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes == null ? Double.NaN : attributes.getTargetDeadband();
    }

    @Override
    public VoltageRegulation setTargetDeadband(double targetDeadband) {
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setTargetDeadband(targetDeadband);
        checkAttributes(newAttributes);
        updateAttribute("targetDeadband", attributes.getTargetDeadband(), targetDeadband, a -> a.setTargetDeadband(targetDeadband));
        return this;
    }

    @Override
    public double getSlope() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes == null ? Double.NaN : attributes.getSlope();
    }

    @Override
    public VoltageRegulation setSlope(double slope) {
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setSlope(slope);
        checkAttributes(newAttributes);
        updateAttribute("slope", attributes.getSlope(), slope, a -> a.setSlope(slope));
        return this;
    }

    @Override
    public Terminal getTerminal() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes == null ? null : resolveTerminal(attributes);
    }

    @Override
    public VoltageRegulation setTerminal(Terminal newTerminal, double targetValue) {
        if (index.getNetwork().getVariantManager().getVariantIds().size() > 1) {
            throw new PowsyblException(validable.getMessageHeader() + "Cannot set terminal when there are multiple variants");
        }
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setTerminal(TerminalRefUtils.getTerminalRefAttributes(newTerminal));
        newAttributes.setTargetValue(targetValue);
        checkAttributes(newAttributes, newTerminal);
        TerminalRefAttributes oldReference = attributes.getTerminal();
        TerminalRefAttributes newReference = newAttributes.getTerminal();
        attributesUpdater.update(a -> {
            a.setTerminal(newReference);
            a.setTargetValue(targetValue);
        }, "VoltageRegulation.Terminal", oldReference, newReference);
        updateTerminal(newTerminal);
        return this;
    }

    @Override
    public boolean isWithTerminal() {
        return getTerminal() != null;
    }

    @Override
    public RegulationMode getMode() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes == null ? null : attributes.getMode();
    }

    @Override
    public VoltageRegulation setMode(RegulationMode mode) {
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setMode(mode);
        checkAttributes(newAttributes);
        ValidationUtil.checkLocalTargetQandV(validable, holderClass,
                holder.getLocalTargetV(), holder.getLocalTargetQ(), newAttributes.toAttributes(resolveTerminal(newAttributes)),
                index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        updateAttribute("mode", attributes.getMode(), mode, a -> a.setMode(mode));
        return this;
    }

    @Override
    public boolean isRegulating() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        return attributes != null && attributes.isRegulating();
    }

    @Override
    public VoltageRegulation setRegulating(boolean regulating) {
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        NetworkVoltageRegulationAttributes newAttributes = copy(attributes);
        newAttributes.setRegulating(regulating);
        if (holder instanceof RatioTapChanger ratioTapChanger && validable instanceof TapChangerParent tapChangerParent) {
            ValidationUtil.checkRatioTapChangerRegulation(tapChangerParent,
                    newAttributes.toAttributes(resolveTerminal(newAttributes)),
                    ratioTapChanger.hasLoadTapChangingCapabilities(),
                    index.getNetwork(),
                    index.getNetwork().getMinValidationLevel(),
                    index.getNetwork().getReportNodeContext().getReportNode());
            Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(tapChangerParent.getAllTapChangers());
            tapChangers.remove(tapChangerParent.getRatioTapChanger());
            ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(tapChangerParent, tapChangers, regulating,
                    index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        }
        ValidationUtil.checkLocalTargetQandV(validable, holderClass,
                holder.getLocalTargetV(), holder.getLocalTargetQ(), newAttributes.toAttributes(resolveTerminal(newAttributes)),
                index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        checkAttributes(newAttributes);
        updateAttribute("regulating", attributes.isRegulating(), regulating, a -> a.setRegulating(regulating));
        return this;
    }

    public void onRemove() {
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
            terminal = null;
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedReferenced) {
        if (terminal != removedReferenced) {
            return;
        }
        Terminal localTerminal = holder.getTerminal();
        if (localTerminal != null && localTerminal != removedReferenced
                && shouldKeepLocalRegulation(removedReferenced, localTerminal)) {
            updateTerminalWithoutValidation(localTerminal);
            return;
        }
        NetworkVoltageRegulationAttributes attributes = currentAttributes();
        attributesUpdater.update(a -> {
            a.setTerminal(null);
            a.setRegulating(false);
            a.setTargetValue(Double.NaN);
            a.setMode(RegulationMode.VOLTAGE);
        }, "VoltageRegulation.Terminal", terminal, null);
        updateTerminal(null);
        LOGGER.warn("Connectable {} was a regulation point for {}. Regulation is deactivated",
                removedReferenced.getConnectable().getId(), validable);
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (terminal == oldReferenced) {
            updateTerminalWithoutValidation(newReferenced);
        }
    }

    private boolean shouldKeepLocalRegulation(Terminal removed, Terminal local) {
        if (holderClass == StaticVarCompensator.class && getMode() != RegulationMode.VOLTAGE) {
            return false;
        }
        String removedBusId = getBusId(removed);
        String localBusId = getBusId(local);
        return removedBusId != null && Objects.equals(removedBusId, localBusId);
    }

    private static String getBusId(Terminal terminal) {
        Bus bus = terminal.getBusView().getBus();
        if (bus != null) {
            return bus.getId();
        }
        Bus connectableBus = terminal.getBusView().getConnectableBus();
        return connectableBus != null ? connectableBus.getId() : null;
    }

    private void updateTerminalWithoutValidation(Terminal newTerminal) {
        Terminal oldTerminal = terminal;
        updateTerminal(newTerminal);
        TerminalRefAttributes newReference = TerminalRefUtils.getTerminalRefAttributes(newTerminal);
        attributesUpdater.update(a -> a.setTerminal(newReference),
                "VoltageRegulation.Terminal",
                TerminalRefUtils.getTerminalRefAttributes(oldTerminal),
                newReference);
    }

    private void updateTerminal(Terminal newTerminal) {
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
        }
        terminal = newTerminal;
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
        }
        if (regulatingPoint != null) {
            regulatingPoint.synchronizeRegulatingTerminal(newTerminal);
        }
    }

    private void checkAttributes(NetworkVoltageRegulationAttributes attributes) {
        checkAttributes(attributes, resolveTerminal(attributes));
    }

    private void checkAttributes(NetworkVoltageRegulationAttributes attributes, Terminal resolvedTerminal) {
        ValidationUtil.checkVoltageRegulation(validable, attributes.toAttributes(resolvedTerminal), index.getNetwork(), holderClass,
                index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        if (index.getNetwork().getVariantManager().getVariantIds().size() > 1
                && !Objects.equals(terminal, resolvedTerminal)) {
            throw new PowsyblException(validable.getMessageHeader() + "Cannot change terminal when there are multiple variants");
        }
    }

    private Terminal resolveTerminal(NetworkVoltageRegulationAttributes attributes) {
        return TerminalRefUtils.getTerminal(index, attributes.getTerminal());
    }

    private NetworkVoltageRegulationAttributes currentAttributes() {
        NetworkVoltageRegulationAttributes attributes = attributesSupplier.get();
        if (attributes == null) {
            throw new PowsyblException("Voltage regulation is not defined for " + validable.getMessageHeader());
        }
        return attributes;
    }

    private void updateAttribute(String attribute, Object oldValue, Object newValue,
                                 Consumer<NetworkVoltageRegulationAttributes> modifier) {
        attributesUpdater.update(modifier, "VoltageRegulation." + switch (attribute) {
            case "targetValue" -> "TargetValue";
            case "targetDeadband" -> "TargetDeadband";
            case "slope" -> "Slope";
            case "mode" -> "RegulationMode";
            case "regulating" -> "isRegulating";
            case "terminal" -> "Terminal";
            default -> throw new IllegalArgumentException("Unknown voltage regulation attribute: " + attribute);
        }, oldValue, newValue);
    }

    private static NetworkVoltageRegulationAttributes copy(NetworkVoltageRegulationAttributes source) {
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(source.getTargetValue())
                .targetDeadband(source.getTargetDeadband())
                .slope(source.getSlope())
                .mode(source.getMode())
                .regulating(source.isRegulating())
                .terminal(source.getTerminal())
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VoltageRegulationImpl other)
                || index.getNetwork() != other.index.getNetwork()
                || holderClass != other.holderClass) {
            return false;
        }
        if (!hasRegulatingPointAttributes || !other.hasRegulatingPointAttributes) {
            return validable == other.validable;
        }
        return Objects.equals(regulatingEquipmentId, other.regulatingEquipmentId)
                && Objects.equals(regulatingEquipmentType, other.regulatingEquipmentType)
                && Objects.equals(regulatingTapChangerType, other.regulatingTapChangerType);
    }

    @Override
    public int hashCode() {
        if (regulatingPoint == null) {
            return Objects.hash(System.identityHashCode(index.getNetwork()), holderClass,
                    System.identityHashCode(validable));
        }
        return Objects.hash(System.identityHashCode(index.getNetwork()), holderClass,
                regulatingEquipmentId, regulatingEquipmentType, regulatingTapChangerType);
    }
}
