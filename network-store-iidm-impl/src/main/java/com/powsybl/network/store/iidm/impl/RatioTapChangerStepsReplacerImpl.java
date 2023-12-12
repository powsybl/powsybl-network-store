package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChangerStepsReplacer;
import com.powsybl.iidm.network.RatioTapChangerStepsReplacerStepAdder;
import com.powsybl.network.store.model.TapChangerStepAttributes;

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
