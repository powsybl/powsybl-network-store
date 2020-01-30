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

    RatioTapChangerAttributes ratioTapChangerAttributes;

    public RatioTapChangerImpl(RatioTapChangerAttributes ratioTapChangerAttributes) {
        this.ratioTapChangerAttributes = ratioTapChangerAttributes;
    }

    @Override
    public double getTargetV() {
        return ratioTapChangerAttributes.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        ratioTapChangerAttributes.setTargetV(targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return ratioTapChangerAttributes.isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        ratioTapChangerAttributes.setLoadTapChangingCapabilities(status);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return ratioTapChangerAttributes.getLowTapPosition();
    }

    @Override
    public RatioTapChanger setLowTapPosition(int lowTapPosition) {
        ratioTapChangerAttributes.setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return ratioTapChangerAttributes.getLowTapPosition() + ratioTapChangerAttributes.getSteps().size() - 1;
    }

    @Override
    public int getTapPosition() {
        return ratioTapChangerAttributes.getTapPosition();
    }

    @Override
    public RatioTapChanger setTapPosition(int tapPosition) {
        ratioTapChangerAttributes.setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return ratioTapChangerAttributes.getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return new RatioTapChangerStepImpl(ratioTapChangerAttributes.getSteps().get(tapPosition));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new RatioTapChangerStepImpl(ratioTapChangerAttributes.getSteps().get(ratioTapChangerAttributes.getTapPosition()));
    }

    @Override
    public boolean isRegulating() {
        return ratioTapChangerAttributes.isRegulating();
    }

    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
        ratioTapChangerAttributes.setRegulating(regulating);
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
        return ratioTapChangerAttributes.getTargetDeadband();
    }

    @Override
    public RatioTapChanger setTargetDeadband(double targetDeadBand) {
        ratioTapChangerAttributes.setTargetDeadband(targetDeadBand);
        return this;
    }
}
