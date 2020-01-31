/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.RatioTapChangerAttributes;
import com.powsybl.network.store.model.RatioTapChangerStepAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<RatioTapChangerStepAttributes> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private boolean regulating = false;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    class StepAdderImpl implements StepAdder {

        private double rho = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = Double.NaN;

        private double b = Double.NaN;

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
            RatioTapChangerStepAttributes ratioTapChangerStepAttributes = RatioTapChangerStepAttributes.builder()
                    .b(b)
                    .g(g)
                    .position(tapPosition)
                    .r(r)
                    .rho(rho)
                    .x(x)
                    .build();
            steps.add(ratioTapChangerStepAttributes);
            return RatioTapChangerAdderImpl.this;
        }
    }

    public RatioTapChangerAdderImpl(Resource<TwoWindingsTransformerAttributes> twoWindingsTransformerResource) {
        this.twoWindingsTransformerResource = twoWindingsTransformerResource;
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
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        //TODO
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

    @Override
    public RatioTapChanger add() {
        RatioTapChangerAttributes ratioTapChangerAttributes = RatioTapChangerAttributes.builder()
                .loadTapChangingCapabilities(loadTapChangingCapabilities)
                .lowTapPosition(lowTapPosition)
                .tapPosition(tapPosition)
                .regulating(regulating)
                .targetDeadband(targetDeadband)
                .targetV(targetV)
                .steps(steps)
                .build();
        twoWindingsTransformerResource.getAttributes().setRatioTapChangerAttributes(ratioTapChangerAttributes);
        return new RatioTapChangerImpl(ratioTapChangerAttributes);
    }
}
