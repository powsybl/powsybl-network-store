/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    class StepAdderImpl implements StepAdder {

        @Override
        public StepAdder setRho(double rho) {
            return this;
        }

        @Override
        public StepAdder setR(double r) {
            return this;
        }

        @Override
        public StepAdder setX(double x) {
            return this;
        }

        @Override
        public StepAdder setG(double g) {
            return this;
        }

        @Override
        public StepAdder setB(double b) {
            return this;
        }

        @Override
        public RatioTapChangerAdder endStep() {
            return RatioTapChangerAdderImpl.this;
        }
    }

    @Override
    public RatioTapChangerAdder setLowTapPosition(int lowTapPosition) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(int tapPosition) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetDeadband(double targetDeadband) {
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        return RatioTapChangerImpl.create();
    }
}
