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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl implements PhaseTapChanger {

    private final PhaseTapChangerAttributes phaseTapChangerAttributes;

    public PhaseTapChangerImpl(PhaseTapChangerAttributes phaseTapChangerAttributes) {
        this.phaseTapChangerAttributes = phaseTapChangerAttributes;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return phaseTapChangerAttributes.getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        phaseTapChangerAttributes.setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return phaseTapChangerAttributes.getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        phaseTapChangerAttributes.setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return phaseTapChangerAttributes.getLowTapPosition();
    }

    @Override
    public PhaseTapChanger setLowTapPosition(int lowTapPosition) {
        phaseTapChangerAttributes.setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return phaseTapChangerAttributes.getLowTapPosition() + phaseTapChangerAttributes.getSteps().size() - 1;
    }

    @Override
    public int getTapPosition() {
        return phaseTapChangerAttributes.getTapPosition();
    }

    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        phaseTapChangerAttributes.setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return phaseTapChangerAttributes.getSteps().size();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new PhaseTapChangerStepImpl(phaseTapChangerAttributes.getSteps().get(tapPosition));
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new PhaseTapChangerStepImpl(phaseTapChangerAttributes.getSteps().get(phaseTapChangerAttributes.getTapPosition()));

    }

    @Override
    public boolean isRegulating() {
        return phaseTapChangerAttributes.isRegulating();
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        phaseTapChangerAttributes.setRegulating(regulating);
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
        return phaseTapChangerAttributes.getTargetDeadband();
    }

    @Override
    public PhaseTapChanger setTargetDeadband(double targetDeadband) {
        phaseTapChangerAttributes.setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
}
