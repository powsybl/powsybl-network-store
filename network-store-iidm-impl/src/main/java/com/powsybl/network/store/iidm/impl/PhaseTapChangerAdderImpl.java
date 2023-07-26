/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerAdderImpl extends AbstractTapChangerAdder implements PhaseTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhaseTapChangerAdderImpl.class);

    private final TapChangerParent tapChangerParent;

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    private final List<TapChangerStepAttributes> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    class StepAdderImpl implements StepAdder {

        private double alpha = Double.NaN;

        private double rho = 1;

        private double r = 0;

        private double x = 0;

        private double g = 0;

        private double b = 0;

        @Override
        public PhaseTapChangerAdder.StepAdder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            if (Double.isNaN(alpha)) {
                throw new ValidationException(tapChangerParent, "step alpha is not set");
            }
            if (Double.isNaN(rho)) {
                throw new ValidationException(tapChangerParent, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(tapChangerParent, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(tapChangerParent, "step x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(tapChangerParent, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(tapChangerParent, "step b is not set");
            }

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
            return PhaseTapChangerAdderImpl.this;
        }
    }

    public PhaseTapChangerAdderImpl(TapChangerParent tapChangerParent, NetworkObjectIndex index,
                                    Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(index);
        this.tapChangerParent = tapChangerParent;
        this.attributesGetter = attributesGetter;
    }

    @Override
    public PhaseTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;

    }

    @Override
    public PhaseTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;

    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    public PhaseTapChangerAdder setRegulationTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public PhaseTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(tapChangerParent, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(tapChangerParent, "a phase tap changer shall have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(tapChangerParent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(tapChangerParent, regulationMode, regulationValue, regulating, regulatingTerminal, index.getNetwork(), true);
        ValidationUtil.checkTargetDeadband(tapChangerParent, "phase tap changer", regulating, targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Set<TapChanger<?, ?>> tapChangers = new HashSet<>();
        tapChangers.addAll(tapChangerParent.getAllTapChangers());
        tapChangers.remove(tapChangerParent.getPhaseTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(tapChangerParent, tapChangers, regulating, true);

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);

        PhaseTapChangerAttributes phaseTapChangerAttributes = PhaseTapChangerAttributes.builder()
                .lowTapPosition(lowTapPosition)
                .regulating(regulating)
                .regulationMode(regulationMode)
                .regulationValue(regulationValue)
                .steps(steps)
                .tapPosition(tapPosition)
                .targetDeadband(targetDeadband)
                .regulatingTerminal(terminalRefAttributes)
                .build();
        TapChangerParentAttributes tapChangerParentAttributes = attributesGetter.apply(tapChangerParent.getTransformer().getResource().getAttributes());
        if (tapChangerParentAttributes.getRatioTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        tapChangerParent.setPhaseTapChanger(phaseTapChangerAttributes);

        return new PhaseTapChangerImpl(tapChangerParent, index, attributesGetter);
    }
}
