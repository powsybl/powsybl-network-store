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

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.CURRENT_LIMITER;

    private double regulationValue = Double.NaN;

    class StepAdderImpl implements PhaseTapChangerAdder.StepAdder {

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
            TapChangerStepAttributes phaseTapChangerStepAttributes =
                    TapChangerStepAttributes.builder()
                            .alpha(alpha)
                            .b(b)
                            .g(g)
                            .r(r)
                            .rho(rho)
                            .x(x)
                            .build();
            PhaseTapChangerImpl.validateStep(phaseTapChangerStepAttributes, tapChangerParent);
            steps.add(phaseTapChangerStepAttributes);
            return PhaseTapChangerAdderImpl.this;
        }
    }

    public PhaseTapChangerAdderImpl(TapChangerParent tapChangerParent, NetworkObjectIndex index,
                                    Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(index);
        this.tapChangerParent = tapChangerParent;
        this.attributesGetter = attributesGetter;
        this.loadTapChangingCapabilities = true;
    }

    @Override
    public PhaseTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return this;
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
    public PhaseTapChangerAdder setSolvedTapPosition(Integer integer) {
        // FIXME to be implemented
        return null;
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
    public PhaseTapChangerAdder.StepAdder beginStep() {
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
            throw new ValidationException(tapChangerParent, "phase tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(tapChangerParent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(tapChangerParent, regulationMode, regulationValue, regulating, loadTapChangingCapabilities, regulatingTerminal, tapChangerParent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, index.getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkTargetDeadband(tapChangerParent, "phase tap changer", regulating, targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS, tapChangerParent.getNetwork().getReportNodeContext().getReportNode());

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>();
        tapChangers.addAll(tapChangerParent.getAllTapChangers());
        tapChangers.remove(tapChangerParent.getPhaseTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(tapChangerParent, tapChangers, regulating, ValidationLevel.STEADY_STATE_HYPOTHESIS, tapChangerParent.getNetwork().getReportNodeContext().getReportNode());

        RegulatingPointAttributes regulatingPointAttributes = createRegulationPointAttributes(tapChangerParent, RegulatingTapChangerType.PHASE_TAP_CHANGER, regulationMode.toString(), regulating);

        PhaseTapChangerAttributes phaseTapChangerAttributes = PhaseTapChangerAttributes.builder()
                .loadTapChangingCapabilities(loadTapChangingCapabilities)
                .lowTapPosition(lowTapPosition)
                .regulationValue(regulationValue)
                .steps(steps)
                .tapPosition(tapPosition)
                .targetDeadband(targetDeadband)
                .regulatingPoint(regulatingPointAttributes)
                .build();
        TapChangerParentAttributes tapChangerParentAttributes = attributesGetter.apply(tapChangerParent.getTransformer().getResource().getAttributes());
        if (tapChangerParentAttributes.getRatioTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        tapChangerParent.setPhaseTapChanger(phaseTapChangerAttributes);
        PhaseTapChangerImpl tapChanger = new PhaseTapChangerImpl(tapChangerParent, index, attributesGetter);
        tapChanger.setRegulationTerminal(regulatingTerminal);
        return tapChanger;
    }
}
