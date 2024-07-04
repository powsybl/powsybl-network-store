/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.RatioTapChanger.RegulationMode;
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

    private double regulationValue = Double.NaN;

    private RatioTapChanger.RegulationMode regulationMode;

    class StepAdderImpl implements RatioTapChangerAdder.StepAdder {

        private double rho = Double.NaN;

        private double r = 0;

        private double x = 0;

        private double g = 0;

        private double b = 0;

        @Override
        public RatioTapChangerAdder.StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public RatioTapChangerAdder endStep() {
            TapChangerStepAttributes ratioTapChangerStepAttributes = TapChangerStepAttributes.builder()
                    .b(b)
                    .g(g)
                    .r(r)
                    .rho(rho)
                    .x(x)
                    .build();
            RatioTapChangerImpl.validateStep(ratioTapChangerStepAttributes, tapChangerParent);
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
    public RatioTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    public RatioTapChangerAdder setRegulationTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    private Integer getRelativeNeutralPosition() {
        for (int i = 0; i < steps.size(); i++) {
            TapChangerStepAttributes tapChangerStepAttributes = steps.get(i);
            if (tapChangerStepAttributes.getRho() == 1) {
                return i;
            }
        }
        return null;
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
        ValidationUtil.checkRatioTapChangerRegulation(tapChangerParent, regulating, loadTapChangingCapabilities, regulatingTerminal, regulationMode, regulationValue, index.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        ValidationUtil.checkTargetDeadband(tapChangerParent, "ratio tap changer", regulating, targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>();
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
                .regulationMode(regulationMode)
                .regulationValue(regulationValue)
                .steps(steps)
                .regulatingTerminal(terminalRefAttributes)
                .relativeNeutralPosition(getRelativeNeutralPosition())
                .build();
        TapChangerParentAttributes tapChangerParentAttributes = attributesGetter.apply(tapChangerParent.getTransformer().getResource().getAttributes());
        if (tapChangerParentAttributes.getPhaseTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        tapChangerParent.setRatioTapChanger(ratioTapChangerAttributes);

        return new RatioTapChangerImpl(tapChangerParent, index, attributesGetter);
    }

    @Override
    public RatioTapChangerAdder setRegulationMode(RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        setRegulationValue(targetV);
        if (!Double.isNaN(targetV)) {
            setRegulationMode(RegulationMode.VOLTAGE);
        }
        return this;
    }
}
