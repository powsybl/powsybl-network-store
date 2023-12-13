/**
 * Copyright (c) 2023, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChangerStepsReplacer;
import com.powsybl.iidm.network.RatioTapChangerStepsReplacerStepAdder;
import com.powsybl.network.store.model.TapChangerStepAttributes;

/**
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public class RatioTapChangerStepsReplacerImpl extends AbstractTapChangerStepsReplacer<RatioTapChangerStepsReplacerImpl> implements RatioTapChangerStepsReplacer {

    class StepAdderImpl implements RatioTapChangerStepsReplacerStepAdder {

        private double rho = Double.NaN;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        @Override
        public RatioTapChangerStepsReplacerStepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacerStepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacerStepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacerStepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacerStepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer endStep() {
            TapChangerStepAttributes phaseTapChangerStepAttributes =
                TapChangerStepAttributes.builder()
                    .b(b)
                    .g(g)
                    .r(r)
                    .rho(rho)
                    .x(x)
                    .build();
            steps.add(phaseTapChangerStepAttributes);
            return RatioTapChangerStepsReplacerImpl.this;
        }

    }

    RatioTapChangerStepsReplacerImpl(RatioTapChangerImpl ratioTapChanger) {
        super(ratioTapChanger);
    }

    @Override
    public RatioTapChangerStepsReplacerStepAdder beginStep() {
        return new StepAdderImpl();
    }
}
