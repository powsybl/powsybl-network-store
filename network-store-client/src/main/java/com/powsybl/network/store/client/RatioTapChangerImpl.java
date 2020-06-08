/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.RatioTapChangerAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl implements RatioTapChanger {

    private final RatioTapChangerAttributes attributes;

    private final NetworkObjectIndex index;

    public RatioTapChangerImpl(NetworkObjectIndex index, RatioTapChangerAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes);
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public double getTargetV() {
        return attributes.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        attributes.setTargetV(targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return attributes.isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        attributes.setLoadTapChangingCapabilities(status);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return attributes.getLowTapPosition();
    }

    @Override
    public RatioTapChanger setLowTapPosition(int lowTapPosition) {
        attributes.setLowTapPosition(lowTapPosition);
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
        attributes.setTapPosition(tapPosition);
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
        attributes.setRegulating(regulating);
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
        return TerminalRefUtils.getRegulatingTerminal(index, terminalRefAttributes);
    }

    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulatingTerminal) {
        attributes.setRegulatingTerminal(TerminalRefUtils.regulatingTerminalToTerminaRefAttributes(regulatingTerminal));
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
        attributes.setTargetDeadband(targetDeadBand);
        return this;
    }
}
