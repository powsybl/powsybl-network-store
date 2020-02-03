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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl implements RatioTapChanger {

    RatioTapChangerAttributes attributes;

    public RatioTapChangerImpl(RatioTapChangerAttributes attributes) {
        this.attributes = attributes;
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
        return new RatioTapChangerStepImpl(attributes.getSteps().get(tapPosition));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new RatioTapChangerStepImpl(attributes.getSteps().get(attributes.getTapPosition()));
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
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        //TODO
        throw new UnsupportedOperationException("TODO");
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
