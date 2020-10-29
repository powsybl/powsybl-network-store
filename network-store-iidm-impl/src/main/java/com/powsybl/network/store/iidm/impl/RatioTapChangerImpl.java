/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.RatioTapChangerAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl implements RatioTapChanger {

    private final TapChangerParent parent;

    private final RatioTapChangerAttributes attributes;

    private final NetworkObjectIndex index;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, RatioTapChangerAttributes attributes) {
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
    public double getTargetV() {
        return attributes.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        double oldValue = attributes.getTargetV();
        attributes.setTargetV(targetV);
        notifyUpdate(() -> getTapChangerAttribute() + ".targetV", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return attributes.isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        boolean oldValue = attributes.isLoadTapChangingCapabilities();
        attributes.setLoadTapChangingCapabilities(status);
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, status);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return attributes.getLowTapPosition();
    }

    @Override
    public RatioTapChanger setLowTapPosition(int lowTapPosition) {
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
    public RatioTapChanger setTapPosition(int tapPosition) {
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
    public RatioTapChangerStep getStep(int tapPosition) {
        return new RatioTapChangerStepImpl(attributes.getSteps().get(tapPosition - attributes.getLowTapPosition()));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new RatioTapChangerStepImpl(attributes.getSteps().get(attributes.getTapPosition() - attributes.getLowTapPosition()));
    }

    @Override
    public boolean isRegulating() {
        return attributes.isRegulating();
    }

    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
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
    public RatioTapChanger setRegulationTerminal(Terminal regulatingTerminal) {
        TerminalRefAttributes oldValue = attributes.getRegulatingTerminal();
        attributes.setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getTargetDeadband() {
        return attributes.getTargetDeadband();
    }

    @Override
    public RatioTapChanger setTargetDeadband(double targetDeadBand) {
        double oldValue = attributes.getTargetDeadband();
        attributes.setTargetDeadband(targetDeadBand);
        notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetDeadBand);
        return this;
    }

    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }
}
