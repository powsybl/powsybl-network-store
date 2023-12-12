package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChangerStepsReplacer;
import com.powsybl.iidm.network.PhaseTapChangerStepsReplacerStepAdder;
import com.powsybl.network.store.model.TapChangerStepAttributes;

public class PhaseTapChangerStepsReplacerImpl extends AbstractTapChangerStepsReplacer<PhaseTapChangerStepsReplacerImpl> implements PhaseTapChangerStepsReplacer {

    class StepAdderImpl implements PhaseTapChangerStepsReplacerStepAdder {

        private double alpha = Double.NaN;

        private double rho = Double.NaN;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacer endStep() {
            TapChangerStepAttributes phaseTapChangerStepAttributes =
                TapChangerStepAttributes.builder()
                    .alpha(alpha)
                    .b(b)
                    .g(g)
                    .r(r)
                    .rho(rho)
                    .x(x)
                    .build();
            steps.add(phaseTapChangerStepAttributes);
            return PhaseTapChangerStepsReplacerImpl.this;
        }

    }

    PhaseTapChangerStepsReplacerImpl(PhaseTapChangerImpl phaseTapChanger) {
        super(phaseTapChanger);
    }

    @Override
    public PhaseTapChangerStepsReplacerStepAdder beginStep() {
        return new StepAdderImpl();
    }
}
