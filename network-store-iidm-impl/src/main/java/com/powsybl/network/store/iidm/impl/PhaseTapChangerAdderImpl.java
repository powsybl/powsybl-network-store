/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerAdderImpl extends AbstractTapChangerAdder implements PhaseTapChangerAdder, Validable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhaseTapChangerAdderImpl.class);

    private final TapChangerParent tapChangerParent;

    private final TapChangerParentAttributes tapChangerParentAttributes;

    private final List<PhaseTapChangerStepAttributes> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private String id;

    class StepAdderImpl implements StepAdder {

        private double alpha = Double.NaN;

        private double rho = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = Double.NaN;

        private double b = Double.NaN;

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
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step alpha is not set");
            }
            if (Double.isNaN(rho)) {
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(PhaseTapChangerAdderImpl.this, "step b is not set");
            }

            PhaseTapChangerStepAttributes phaseTapChangerStepAttributes =
                    PhaseTapChangerStepAttributes.builder()
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

    public PhaseTapChangerAdderImpl(TapChangerParent tapChangerParent, NetworkObjectIndex index, TapChangerParentAttributes tapChangerParentAttributes, String id) {
        super(index);
        this.tapChangerParent = tapChangerParent;
        this.tapChangerParentAttributes = tapChangerParentAttributes;
        this.id = id;
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
            throw new ValidationException(this, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(this, "a phase tap changer shall have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(this, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        checkPhaseTapChangerRegulation(this, regulationMode, regulationValue, regulating);
        ValidationUtil.checkTargetDeadband(this, "phase tap changer", regulating, targetDeadband);

        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, index.getNetwork());

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
        tapChangerParentAttributes.setPhaseTapChangerAttributes(phaseTapChangerAttributes);

        checkOnlyOneTapChangerRegulatingEnabled(this, tapChangerParentAttributes.getRatioTapChangerAttributes(), regulating);
        if (tapChangerParentAttributes.getRatioTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        return new PhaseTapChangerImpl(tapChangerParent, index, phaseTapChangerAttributes);
    }

    private static void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                                double regulationValue, boolean regulating) {
        if (regulationMode == null) {
            throw new ValidationException(validable, "phase regulation mode is not set");
        }
        if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
            throw new ValidationException(validable, "phase regulation is on and threshold/setpoint value is not set");
        }
        if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP && regulating) {
            throw new ValidationException(validable, "phase regulation cannot be on if mode is FIXED");
        }
    }

    @Override
    public String getMessageHeader() {
        return "phaseTapChanger '" + id + "': ";
    }
}
