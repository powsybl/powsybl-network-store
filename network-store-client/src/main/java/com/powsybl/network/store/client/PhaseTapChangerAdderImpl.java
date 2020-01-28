/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;
import com.powsybl.network.store.model.PhaseTapChangerStepAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerAdderImpl implements PhaseTapChangerAdder {

    Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerAttributesResource;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<PhaseTapChangerStepAttributes> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private boolean regulating = false;

    private double targetDeadband = Double.NaN;

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

            PhaseTapChangerStepAttributes phaseTapChangerStepAttributes =
                    PhaseTapChangerStepAttributes.builder()
                            .alpha(alpha)
                            .b(b)
                            .g(g)
                            .r(r)
                            .rho(rho)
                            .position(tapPosition)
                            .x(x)
                            .build();
            steps.add(phaseTapChangerStepAttributes);
            return PhaseTapChangerAdderImpl.this;
        }
    }

    public PhaseTapChangerAdderImpl(Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerAttributesResource) {
        this.twoWindingsTransformerAttributesResource = twoWindingsTransformerAttributesResource;
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
    public PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        //TODO
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

    @Override
    public PhaseTapChanger add() {
        PhaseTapChangerAttributes phaseTapChangerAttributes = PhaseTapChangerAttributes.builder()
                .lowTapPosition(lowTapPosition)
                .regulating(regulating)
                .regulationMode(regulationMode)
                .regulationValue(regulationValue)
                .steps(steps)
                .tapPosition(tapPosition)
                .targetDeadband(targetDeadband)
                .build();
        twoWindingsTransformerAttributesResource.getAttributes().setPhaseTapChangerAttributes(phaseTapChangerAttributes);
        return new PhaseTapChangerImpl(phaseTapChangerAttributes);
    }
}
