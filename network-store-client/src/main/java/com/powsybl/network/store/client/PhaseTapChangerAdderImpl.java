/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerAdderImpl implements PhaseTapChangerAdder {

    class StepAdderImpl implements StepAdder {

        @Override
        public PhaseTapChangerAdder.StepAdder setAlpha(double alpha) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setRho(double rho) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setR(double r) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setX(double x) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setG(double g) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setB(double b) {
            // TODO
            return this;
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            return PhaseTapChangerAdderImpl.this;
        }
    }

    @Override
    public PhaseTapChangerAdder setLowTapPosition(int lowTapPosition) {
        // TODO
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTapPosition(int tapPosition) {
        // TODO
        return this;

    }

    @Override
    public PhaseTapChangerAdder setRegulating(boolean regulating) {
        // TODO
        return this;

    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        // TODO
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationValue(double regulationValue) {
        // TODO
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        // TODO
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTargetDeadband(double targetDeadband) {
        // TODO
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public PhaseTapChanger add() {
        return PhaseTapChangerImpl.create();
    }
}
