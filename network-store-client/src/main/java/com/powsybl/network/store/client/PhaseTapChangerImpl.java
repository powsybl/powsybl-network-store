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
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl implements PhaseTapChanger {

    private final Resource<PhaseTapChangerAttributes> resource;

    public PhaseTapChangerImpl(Resource<PhaseTapChangerAttributes> resource) {
        this.resource = resource;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return resource.getAttributes().getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        resource.getAttributes().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return resource.getAttributes().getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        resource.getAttributes().setRegulationValue(regulationValue);
        return this;
    }

    @Override
    public int getLowTapPosition() {
        return resource.getAttributes().getLowTapPosition();
    }

    @Override
    public PhaseTapChanger setLowTapPosition(int lowTapPosition) {
        resource.getAttributes().setLowTapPosition(lowTapPosition);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return resource.getAttributes().getLowTapPosition() + this.resource.getAttributes().getSteps().size() - 1;
    }

    @Override
    public int getTapPosition() {
        return resource.getAttributes().getTapPosition();
    }

    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        resource.getAttributes().setTapPosition(tapPosition);
        return this;
    }

    @Override
    public int getStepCount() {
        return resource.getAttributes().getSteps().size();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return resource.getAttributes().getSteps().get(tapPosition);
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return resource.getAttributes().getSteps().get(resource.getAttributes().getTapPosition());
    }

    @Override
    public boolean isRegulating() {
        return resource.getAttributes().isRegulating();
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        resource.getAttributes().setRegulating(regulating);
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
        return resource.getAttributes().getTargetDeadband();
    }

    @Override
    public PhaseTapChanger setTargetDeadband(double targetDeadband) {
        resource.getAttributes().setTargetDeadband(targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
}
