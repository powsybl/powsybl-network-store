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
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerAdderImpl extends AbstractTapChanger implements RatioTapChangerAdder, Validable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatioTapChangerAdderImpl.class);

    private final TapChangerParentAttributes tapChangerParentAttributes;

    private final List<RatioTapChangerStepAttributes> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private double targetV = Double.NaN;

    private String id;

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
            if (Double.isNaN(rho)) {
                throw new ValidationException(RatioTapChangerAdderImpl.this, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(RatioTapChangerAdderImpl.this, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(RatioTapChangerAdderImpl.this, "step x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(RatioTapChangerAdderImpl.this, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(RatioTapChangerAdderImpl.this, "step b is not set");
            }

            RatioTapChangerStepAttributes ratioTapChangerStepAttributes = RatioTapChangerStepAttributes.builder()
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

    public RatioTapChangerAdderImpl(TapChangerParentAttributes tapChangerParentAttributes, String id) {
        this.tapChangerParentAttributes = tapChangerParentAttributes;
        this.id = id;
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
        if (tapPosition == null) {
            throw new ValidationException(this, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(this, "ratio tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(this, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        checkRatioTapChangerRegulation(this, regulating, targetV);
        ValidationUtil.checkTargetDeadband(this, "ratio tap changer", regulating, targetDeadband);

        RatioTapChangerAttributes ratioTapChangerAttributes = RatioTapChangerAttributes.builder()
                .loadTapChangingCapabilities(loadTapChangingCapabilities)
                .lowTapPosition(lowTapPosition)
                .tapPosition(tapPosition)
                .regulating(regulating)
                .targetDeadband(targetDeadband)
                .targetV(targetV)
                .steps(steps)
                .build();
        tapChangerParentAttributes.setRatioTapChangerAttributes(ratioTapChangerAttributes);

        Set<TapChangerAttributes> tapChangers = new HashSet<>();
        tapChangers.addAll(tapChangerParentAttributes.getAllTapChangersAttributes());
        tapChangers.remove(tapChangerParentAttributes.getRatioTapChangerAttributes());
        checkOnlyOneTapChangerRegulatingEnabled(this, tapChangers, regulating);
        if (tapChangerParentAttributes.hasPhaseTapChangerAttributes()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", tapChangerParentAttributes);
        }

        return new RatioTapChangerImpl(ratioTapChangerAttributes);
    }

    private void checkRatioTapChangerRegulation(Validable validable, boolean regulating, double targetV) {
        if (regulating) {
            if (Double.isNaN(targetV)) {
                throw new ValidationException(validable, "a target voltage has to be set for a regulating ratio tap changer");
            }
            if (targetV <= 0) {
                throw new ValidationException(validable, "bad target voltage " + targetV);
            }
        }
    }

    @Override
    public String getMessageHeader() {
        return "ratioTapChanger '" + id + "': ";
    }
}
