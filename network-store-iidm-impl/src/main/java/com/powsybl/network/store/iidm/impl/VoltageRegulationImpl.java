/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.NetworkVoltageRegulationAttributes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Network-store implementation of the native IIDM voltage regulation object.
 */
final class VoltageRegulationImpl implements VoltageRegulation, Referrer<Terminal> {

    private static final String PREFIX = "VoltageRegulation.";

    private final AbstractIdentifiableImpl<?, ?> identifiable;
    private final NetworkObjectIndex index;
    private final AbstractRegulatingPoint regulatingPoint;
    private final Class<? extends VoltageRegulationHolder<?>> holderClass;
    private final VoltageRegulationHolder<?> holder;
    private final Supplier<NetworkVoltageRegulationAttributes> attributesSupplier;
    private final Consumer<NetworkVoltageRegulationAttributes> attributesConsumer;

    VoltageRegulationImpl(AbstractIdentifiableImpl<?, ?> identifiable,
                           NetworkObjectIndex index,
                           AbstractRegulatingPoint regulatingPoint,
                           Class<? extends VoltageRegulationHolder<?>> holderClass,
                           VoltageRegulationHolder<?> holder,
                           Supplier<NetworkVoltageRegulationAttributes> attributesSupplier,
                           Consumer<NetworkVoltageRegulationAttributes> attributesConsumer) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.index = Objects.requireNonNull(index);
        this.regulatingPoint = Objects.requireNonNull(regulatingPoint);
        this.holderClass = Objects.requireNonNull(holderClass);
        this.holder = Objects.requireNonNull(holder);
        this.attributesSupplier = Objects.requireNonNull(attributesSupplier);
        this.attributesConsumer = Objects.requireNonNull(attributesConsumer);
        Terminal terminal = getTerminal();
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
        }
    }

    private NetworkVoltageRegulationAttributes attributes() {
        return Objects.requireNonNull(attributesSupplier.get(), "Voltage regulation attributes are not set");
    }

    private String notificationPrefix() {
        if (holder instanceof RatioTapChangerImpl ratioTapChanger) {
            return ratioTapChanger.getTapChangerAttribute() + ".";
        }
        return "";
    }

    @Override
    public double getTargetValue() {
        return attributes().getTargetValue();
    }

    @Override
    public VoltageRegulation setTargetValue(double targetValue) {
        NetworkVoltageRegulationAttributes attributes = attributes();
        NetworkVoltageRegulationAttributes candidate = copy(attributes);
        candidate.setTargetValue(targetValue);
        validate(candidate);
        double oldValue = attributes.getTargetValue();
        update(current -> current.setTargetValue(targetValue), notificationPrefix() + PREFIX + "TargetValue", oldValue, targetValue);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return attributes().getTargetDeadband();
    }

    @Override
    public VoltageRegulation setTargetDeadband(double targetDeadband) {
        if (targetDeadband < 0) {
            String type = holderClass == RatioTapChanger.class ? "RatioTapChanger" : "voltage regulation";
            throw new ValidationException(identifiable, "Unexpected value for target deadband of " + type + ": " + targetDeadband + " < 0");
        }
        double oldValue = getTargetDeadband();
        NetworkVoltageRegulationAttributes candidate = copy(attributes());
        candidate.setTargetDeadband(targetDeadband);
        validate(candidate);
        update(current -> current.setTargetDeadband(targetDeadband), notificationPrefix() + PREFIX + "TargetDeadband", oldValue, targetDeadband);
        return this;
    }

    @Override
    public double getSlope() {
        return attributes().getSlope();
    }

    @Override
    public VoltageRegulation setSlope(double slope) {
        double oldValue = getSlope();
        NetworkVoltageRegulationAttributes candidate = copy(attributes());
        candidate.setSlope(slope);
        validate(candidate);
        update(current -> current.setSlope(slope), notificationPrefix() + PREFIX + "Slope", oldValue, slope);
        return this;
    }

    @Override
    public Terminal getTerminal() {
        return TerminalRefUtils.getTerminal(index, attributes().getTerminal());
    }

    @Override
    public VoltageRegulation setTerminal(Terminal terminal, double targetValue) {
        if (identifiable.getNetwork().getVariantManager().getVariantIds().size() > 1) {
            throw new com.powsybl.commons.PowsyblException(identifiable.getMessageHeader() + "Cannot set terminal when there are multiple variants");
        }
        if (terminal != null && terminal.getVoltageLevel().getNetwork() != identifiable.getNetwork()) {
            throw new ValidationException(identifiable, "voltageRegulation.terminal is not part of the network");
        }
        if (terminal != null && isRegulating() && Double.isNaN(targetValue)) {
            throw new ValidationException(identifiable, "Voltage regulation targetValue must be set when a terminal is set");
        }
        NetworkVoltageRegulationAttributes candidate = copy(attributes());
        candidate.setTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal));
        candidate.setTargetValue(targetValue);
        validate(candidate);

        Terminal oldTerminal = getTerminal();
        if (oldTerminal != terminal) {
            unregisterTerminal(oldTerminal);
            regulatingPoint.setRegulatingTerminal(terminal);
            registerTerminal(terminal);
        }
        NetworkVoltageRegulationAttributes current = attributes();
        update(currentAttributes -> {
            currentAttributes.setTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal));
            currentAttributes.setTargetValue(targetValue);
        }, notificationPrefix() + PREFIX + "Terminal", oldTerminal, terminal);
        if (Double.compare(current.getTargetValue(), targetValue) != 0) {
            index.notifyUpdate(identifiable, notificationPrefix() + PREFIX + "TargetValue",
                identifiable.getNetwork().getVariantManager().getWorkingVariantId(), current.getTargetValue(), targetValue);
        }
        return this;
    }

    @Override
    public boolean isWithTerminal() {
        return attributes().getTerminal() != null;
    }

    @Override
    public RegulationMode getMode() {
        return attributes().getMode();
    }

    @Override
    public VoltageRegulation setMode(RegulationMode mode) {
        RegulationMode oldValue = getMode();
        NetworkVoltageRegulationAttributes candidate = copy(attributes());
        candidate.setMode(mode);
        validate(candidate);
        update(current -> current.setMode(mode), notificationPrefix() + PREFIX + "RegulationMode", oldValue, mode);
        return this;
    }

    @Override
    public boolean isRegulating() {
        return attributes().isRegulating();
    }

    @Override
    public VoltageRegulation setRegulating(boolean regulating) {
        boolean oldValue = isRegulating();
        NetworkVoltageRegulationAttributes candidate = copy(attributes());
        candidate.setRegulating(regulating);
        if (holder instanceof RatioTapChangerImpl ratioTapChanger) {
            ValidationUtil.checkRatioTapChangerRegulation(ratioTapChanger.parent, getAttributes(candidate),
                ratioTapChanger.hasLoadTapChangingCapabilities(), identifiable.getNetwork(),
                identifiable.getNetwork().getMinValidationLevel(), identifiable.getNetwork().getReportNodeContext().getReportNode());
            Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(ratioTapChanger.parent.getAllTapChangers());
            tapChangers.remove(ratioTapChanger.parent.getRatioTapChanger());
            ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(ratioTapChanger.parent, tapChangers, regulating,
                identifiable.getNetwork().getMinValidationLevel(), identifiable.getNetwork().getReportNodeContext().getReportNode());
        }
        if (regulating && holderClass == com.powsybl.iidm.network.ShuntCompensator.class
            && Double.isNaN(holder.getLocalTargetV()) && !isWithTerminal()) {
            throw new ValidationException(identifiable,
                "invalid value (NaN) for localTargetV (voltageRegulation is set with VOLTAGE mode and regulating true and the terminal is unset)");
        }
        validate(candidate);
        update(current -> current.setRegulating(regulating), notificationPrefix() + PREFIX + "isRegulating", oldValue, regulating);
        return this;
    }

    @Override
    public VoltageRegulation.VoltageRegulationAttributes getAttributes() {
        NetworkVoltageRegulationAttributes current = attributes();
        return new VoltageRegulation.VoltageRegulationAttributes(
            current.getTargetValue(), current.getTargetDeadband(), current.getSlope(), current.getMode(),
            current.isRegulating(), TerminalRefUtils.getTerminal(index, current.getTerminal()));
    }

    void setAttributes(com.powsybl.iidm.network.regulation.VoltageRegulation.VoltageRegulationAttributes newAttributes) {
        Objects.requireNonNull(newAttributes);
        if (identifiable.getNetwork().getVariantManager().getVariantIds().size() > 1
            && newAttributes.terminal() != getTerminal()) {
            throw new com.powsybl.commons.PowsyblException(identifiable.getMessageHeader() + "Cannot change terminal when there are multiple variants");
        }
        validate(NetworkVoltageRegulationAttributes.builder()
            .targetValue(newAttributes.targetValue())
            .targetDeadband(newAttributes.targetDeadband())
            .slope(newAttributes.slope())
            .mode(newAttributes.mode())
            .regulating(newAttributes.isRegulating())
            .terminal(TerminalRefUtils.getTerminalRefAttributes(newAttributes.terminal()))
            .build());
        Terminal newTerminal = newAttributes.terminal();
        if (getTerminal() != newTerminal) {
            unregisterTerminal(getTerminal());
            regulatingPoint.setRegulatingTerminal(newTerminal);
            registerTerminal(newTerminal);
        }
        NetworkVoltageRegulationAttributes oldAttributes = attributes();
        update(current -> {
            current.setTargetValue(newAttributes.targetValue());
            current.setTargetDeadband(newAttributes.targetDeadband());
            current.setSlope(newAttributes.slope());
            current.setMode(newAttributes.mode());
            current.setRegulating(newAttributes.isRegulating());
            current.setTerminal(TerminalRefUtils.getTerminalRefAttributes(newTerminal));
        }, null, oldAttributes, newAttributes);
    }

    void remove() {
        Terminal terminal = getTerminal();
        if (terminal != null) {
            unregisterTerminal(terminal);
            regulatingPoint.setRegulatingTerminal(null);
        }
    }

    void onRemove() {
        unregisterTerminal(getTerminal());
    }

    void clearTerminal() {
        Terminal terminal = getTerminal();
        if (terminal != null) {
            regulatingPoint.setRegulatingTerminal(null);
        }
    }

    void relocateTerminal(Terminal terminal) {
        unregisterTerminal(getTerminal());
        identifiable.updateResourceWithoutNotification(resource -> attributes().setTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal)));
        registerTerminal(terminal);
    }

    void deactivateAfterTerminalRemoval(boolean resetMode) {
        unregisterTerminal(getTerminal());
        NetworkVoltageRegulationAttributes current = attributes();
        if (current.isRegulating() && !Boolean.TRUE.equals(regulatingPoint.isRegulating())) {
            regulatingPoint.getAttributes().setRegulating(true);
        }
        if (current.isRegulating()) {
            regulatingPoint.setRegulating("regulating", false);
        }
        identifiable.updateResourceWithoutNotification(resource -> {
            NetworkVoltageRegulationAttributes attributes = attributes();
            attributes.setTerminal(null);
            attributes.setTargetValue(Double.NaN);
            if (resetMode) {
                attributes.setMode(RegulationMode.VOLTAGE);
            }
            attributes.setRegulating(false);
        });
    }

    void updateTerminalReference(Terminal terminal) {
        unregisterTerminal(getTerminal());
        if (terminal != null) {
            registerTerminal(terminal);
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedReferenced) {
        Terminal localTerminal = holder.getTerminal();
        boolean sameBus = localTerminal != null && removedReferenced != null
            && localTerminal.getBusBreakerView().getConnectableBus() != null
            && removedReferenced.getBusBreakerView().getConnectableBus() != null
            && Objects.equals(localTerminal.getBusBreakerView().getConnectableBus().getId(),
                removedReferenced.getBusBreakerView().getConnectableBus().getId());
        boolean isSvc = holderClass == com.powsybl.iidm.network.StaticVarCompensator.class;
        if (sameBus && (!isSvc || getMode() == RegulationMode.VOLTAGE)) {
            relocateTerminal(localTerminal);
        } else {
            deactivateAfterTerminalRemoval(isSvc);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (getTerminal() == oldReferenced) {
            relocateTerminal(newReferenced);
        }
    }

    private void registerTerminal(Terminal terminal) {
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
        }
    }

    private void unregisterTerminal(Terminal terminal) {
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
        }
    }

    private void update(Consumer<NetworkVoltageRegulationAttributes> modifier, String attribute, Object oldValue, Object newValue) {
        identifiable.updateResource(resource -> {
            NetworkVoltageRegulationAttributes current = attributes();
            modifier.accept(current);
            attributesConsumer.accept(current);
        }, attribute, oldValue, newValue);
    }

    private void validate(NetworkVoltageRegulationAttributes attributes) {
        VoltageRegulationValidation.checkAttributes(identifiable, getAttributes(attributes), holderClass,
            identifiable.getNetwork());
        if (holderClass != com.powsybl.iidm.network.ShuntCompensator.class
            || attributes.getTerminal() != null) {
            VoltageRegulationValidation.check(identifiable, getAttributes(attributes), holderClass,
                holder.getLocalTargetV(), holder.getLocalTargetQ(), identifiable.getNetwork());
        }
    }

    private NetworkVoltageRegulationAttributes copy(NetworkVoltageRegulationAttributes source) {
        return NetworkVoltageRegulationAttributes.builder()
            .targetValue(source.getTargetValue())
            .targetDeadband(source.getTargetDeadband())
            .slope(source.getSlope())
            .mode(source.getMode())
            .regulating(source.isRegulating())
            .terminal(source.getTerminal())
            .build();
    }

    private VoltageRegulation.VoltageRegulationAttributes getAttributes(NetworkVoltageRegulationAttributes attributes) {
        return new VoltageRegulation.VoltageRegulationAttributes(attributes.getTargetValue(), attributes.getTargetDeadband(),
            attributes.getSlope(), attributes.getMode(), attributes.isRegulating(), TerminalRefUtils.getTerminal(index, attributes.getTerminal()));
    }
}
