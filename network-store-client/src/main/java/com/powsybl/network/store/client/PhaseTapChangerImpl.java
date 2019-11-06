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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl implements PhaseTapChanger {

    public PhaseTapChangerImpl() {
    }

    static PhaseTapChangerImpl create() {
        return new PhaseTapChangerImpl();
    }

    @Override
    public RegulationMode getRegulationMode() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getRegulationValue() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getLowTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setLowTapPosition(int lowTapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getHighTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getStepCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isRegulating() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Terminal getRegulationTerminal() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getTargetDeadband() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PhaseTapChanger setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }
}
