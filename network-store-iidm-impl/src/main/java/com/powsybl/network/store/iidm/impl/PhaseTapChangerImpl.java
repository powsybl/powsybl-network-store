/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl implements PhaseTapChanger {

    private final TapChangerParent parent;

    private final PhaseTapChangerAttributes attributes;

    private final NetworkObjectIndex index;

    public PhaseTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, PhaseTapChangerAttributes attributes) {
        this.parent = Objects.requireNonNull(parent);
        this.attributes = Objects.requireNonNull(attributes);
        this.index = Objects.requireNonNull(index);
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(parent.getTransformer(), attribute.get(), oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        index.notifyUpdate(parent.getTransformer(), attribute.get(), variantId, oldValue, newValue);
    }

    @Override
    public RegulationMode getRegulationMode() {
        return attributes.getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = attributes.getRegulationMode();
        attributes.setRegulationMode(regulationMode);
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return attributes.getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        double oldValue = attributes.getRegulationValue();
        attributes.setRegulationValue(regulationValue);
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationValue);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return attributes.getLowTapPosition();
    }

    @Override
    public PhaseTapChanger setLowTapPosition(int lowTapPosition) {
        int oldValue = attributes.getLowTapPosition();
        attributes.setLowTapPosition(lowTapPosition);
        notifyUpdate(() -> getTapChangerAttribute() + ".lowTapPosition", oldValue, lowTapPosition);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public int getTapPosition() {
        return attributes.getTapPosition();
    }

    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        int oldValue = attributes.getTapPosition();
        attributes.setTapPosition(tapPosition);
        notifyUpdate(() -> getTapChangerAttribute() + ".tapPosition", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return attributes.getSteps().size();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new PhaseTapChangerStepImpl(attributes.getSteps().get(tapPosition - attributes.getLowTapPosition()));
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new PhaseTapChangerStepImpl(attributes.getSteps().get(attributes.getTapPosition() - attributes.getLowTapPosition()));

    }

    @Override
    public boolean isRegulating() {
        return attributes.isRegulating();
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        boolean oldValue = attributes.isRegulating();
        attributes.setRegulating(regulating);
        notifyUpdate(() -> getTapChangerAttribute() + ".regulating", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulating);
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    @Override
    public PhaseTapChanger setRegulationTerminal(Terminal regulatingTerminal) {
        TerminalRefAttributes oldValue = attributes.getRegulatingTerminal();
        attributes.setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return attributes.getTargetDeadband();
    }

    @Override
    public PhaseTapChanger setTargetDeadband(double targetDeadband) {
        double oldValue = attributes.getTargetDeadband();
        attributes.setTargetDeadband(targetDeadband);
        notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    protected String getTapChangerAttribute() {
        return "phase" + parent.getTapChangerAttribute();
    }
}
