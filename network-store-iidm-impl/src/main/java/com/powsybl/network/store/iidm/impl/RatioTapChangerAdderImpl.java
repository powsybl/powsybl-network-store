/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
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

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    private final List<TapChangerStepAttributes> steps = new ArrayList<>();

    private double regulationValue = Double.NaN;

    private RegulationMode regulationMode;

    private Boolean regulating;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes = null;

    class StepAdderImpl extends AbstractBasePropertiesHolder implements RatioTapChangerAdder.StepAdder {

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
                    .properties(properties)
                    .build();
            RatioTapChangerImpl.validateStep(ratioTapChangerStepAttributes, tapChangerParent);
            steps.add(ratioTapChangerStepAttributes);
            return RatioTapChangerAdderImpl.this;
        }
    }

    public RatioTapChangerAdderImpl(TapChangerParent tapChangerParent, NetworkObjectIndex index,
                                    Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(tapChangerParent, index);
        this.attributesGetter = attributesGetter;
        this.loadTapChangingCapabilities = false;
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
    public RatioTapChangerAdder setSolvedTapPosition(Integer solvedTapPosition) {
        this.solvedTapPosition = solvedTapPosition;
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

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public VoltageRegulationAdder<RatioTapChangerAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(RatioTapChanger.class, tapChangerParent, this, index.getNetwork(), this::setVoltageRegulationAttributes);
    }

    @Override
    public double getLocalTargetQ() {
        return Double.NaN;
    }

    @Override
    public RatioTapChangerAdder setLocalTargetQ(double localTargetQ) {
        LOGGER.warn("Ignored operation, the local target reactive power is not supported for ratio tap changer");
        return this;
    }

    @Override
    public RatioTapChangerAdder setLocalTargetV(double localTargetV) {
        LOGGER.warn("Ignored operation, the local target voltage is not supported for ratio tap changer");
        return this;
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public RatioTapChanger add() {
        checkPosition();
        if (steps.isEmpty()) {
            throw new ValidationException(tapChangerParent, "ratio tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        checkPositionRange(tapPosition, lowTapPosition, highTapPosition, "tap position");
        checkPositionRange(solvedTapPosition, lowTapPosition, highTapPosition, "solved tap position");
        NetworkImpl network = index.getNetwork();

        // Backward compatibility
        if (voltageRegulationAttributes == null && regulating != null) {
            this.newVoltageRegulation()
                .withMode(regulationMode)
                .withTargetValue(regulationValue)
                .withTerminal(regulatingTerminal)
                .withRegulating(regulating)
                .withTargetDeadband(targetDeadband)
                .add();
        }

        if (voltageRegulationAttributes != null) {
            ValidationUtil.checkRatioTapChangerRegulation(tapChangerParent, voltageRegulationAttributes,
                loadTapChangingCapabilities, network, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        }

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>();
        tapChangers.addAll(tapChangerParent.getAllTapChangers());
        tapChangers.remove(tapChangerParent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(tapChangerParent, tapChangers,
            voltageRegulationAttributes != null && voltageRegulationAttributes.isRegulating(), network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());

        VoltageRegulationAttributes persistedVoltageRegulation = null;
        if (voltageRegulationAttributes != null) {
            RegulatingPointAttributes regulatingPointAttributes = createRegulationPointAttributes(tapChangerParent, RegulatingTapChangerType.RATIO_TAP_CHANGER,
                voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null, voltageRegulationAttributes.isRegulating(), voltageRegulationAttributes.terminal());
            persistedVoltageRegulation = VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(regulatingPointAttributes.getRegulatingEquipmentId())
                .regulatingResourceType(regulatingPointAttributes.getRegulatingResourceType())
                .regulatingTapChangerType(regulatingPointAttributes.getRegulatingTapChangerType())
                .localTerminal(regulatingPointAttributes.getLocalTerminal())
                .regulatingTerminal(regulatingPointAttributes.getRegulatingTerminal())
                .regulatedResourceType(regulatingPointAttributes.getRegulatedResourceType())
                .regulationMode(regulatingPointAttributes.getRegulationMode())
                .regulating(regulatingPointAttributes.getRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build();
        }

        RatioTapChangerAttributes ratioTapChangerAttributes = RatioTapChangerAttributes.builder()
                .loadTapChangingCapabilities(loadTapChangingCapabilities)
                .lowTapPosition(lowTapPosition)
                .tapPosition(tapPosition)
                .solvedTapPosition(solvedTapPosition)
                .voltageRegulation(persistedVoltageRegulation)
                .properties(properties)
                .steps(steps)
                .build();
        TapChangerParentAttributes tapChangerParentAttributes = attributesGetter.apply(tapChangerParent.getTransformer().getResource().getAttributes());
        if (tapChangerParentAttributes.getPhaseTapChangerAttributes() != null) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        tapChangerParent.setRatioTapChanger(ratioTapChangerAttributes);
        RatioTapChangerImpl ratioTapChanger = new RatioTapChangerImpl(tapChangerParent, index, attributesGetter);
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(ratioTapChanger.getRegulatingPoint());
            ((VoltageRegulationImpl) ratioTapChanger.getVoltageRegulation()).registerOnTerminal();
        }
        return ratioTapChanger;
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
