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
public class RatioTapChangerAdderImpl extends AbstractTapChangerAdder implements RatioTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatioTapChangerAdderImpl.class);

    private final TapChangerParent tapChangerParent;

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    private final List<TapChangerStepAttributes> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private double targetV = Double.NaN;

    class StepAdderImpl implements StepAdder {

        private double rho = Double.NaN;

        private double r = 0;

        private double x = 0;

        private double g = 0;

        private double b = 0;

        @Override
        public StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public RatioTapChangerAdder endStep() {
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

            TapChangerStepAttributes ratioTapChangerStepAttributes = TapChangerStepAttributes.builder()
                    .b(b)
                    .g(g)
                    .r(r)
                    .rho(rho)
                    .x(x)
                    .build();
            steps.add(ratioTapChangerStepAttributes);
            return RatioTapChangerAdderImpl.this;
        }
    }

    public RatioTapChangerAdderImpl(TapChangerParent tapChangerParent, NetworkObjectIndex index,
                                    Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(index);
        this.tapChangerParent = tapChangerParent;
        this.attributesGetter = attributesGetter;
    }

    @Override
    public RatioTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    public RatioTapChangerAdder setRegulationTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public RatioTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(tapChangerParent, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(tapChangerParent, "ratio tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(tapChangerParent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkRatioTapChangerRegulation(tapChangerParent, regulating, loadTapChangingCapabilities, regulatingTerminal, targetV, index.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        ValidationUtil.checkTargetDeadband(tapChangerParent, "ratio tap changer", regulating, targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Set<TapChanger<?, ?>> tapChangers = new HashSet<>();
        tapChangers.addAll(tapChangerParent.getAllTapChangers());
        tapChangers.remove(tapChangerParent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(tapChangerParent, tapChangers, regulating, true);

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);

        RatioTapChangerAttributes ratioTapChangerAttributes = RatioTapChangerAttributes.builder()
                .loadTapChangingCapabilities(loadTapChangingCapabilities)
                .lowTapPosition(lowTapPosition)
                .tapPosition(tapPosition)
                .regulating(regulating)
                .targetDeadband(targetDeadband)
                .targetV(targetV)
                .steps(steps)
                .regulatingTerminal(terminalRefAttributes)
                .build();
        TapChangerParentAttributes tapChangerParentAttributes = attributesGetter.apply(tapChangerParent.getTransformer().getResource().getAttributes());
        if (tapChangerParentAttributes.getPhaseTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }
        tapChangerParentAttributes.setRatioTapChangerAttributes(ratioTapChangerAttributes);

        return new RatioTapChangerImpl(tapChangerParent, index, attributesGetter);
    }
}
