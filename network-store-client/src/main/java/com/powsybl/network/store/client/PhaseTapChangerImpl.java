/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl implements PhaseTapChanger {

    private final PhaseTapChangerAttributes attributes;

    public PhaseTapChangerImpl(PhaseTapChangerAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public RegulationMode getRegulationMode() {
        return attributes.getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        attributes.setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return attributes.getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        attributes.setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return attributes.getLowTapPosition();
    }

    @Override
    public PhaseTapChanger setLowTapPosition(int lowTapPosition) {
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
    public PhaseTapChanger setTapPosition(int tapPosition) {
        attributes.setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return attributes.getSteps().size();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new PhaseTapChangerStepImpl(attributes.getSteps().get(tapPosition));
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new PhaseTapChangerStepImpl(attributes.getSteps().get(attributes.getTapPosition()));

    }

    @Override
    public boolean isRegulating() {
        return attributes.isRegulating();
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        attributes.setRegulating(regulating);
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        //TODO
        return null;
    }

    @Override
    public PhaseTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        //TODO
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return attributes.getTargetDeadband();
    }

    @Override
    public PhaseTapChanger setTargetDeadband(double targetDeadband) {
        attributes.setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
}
