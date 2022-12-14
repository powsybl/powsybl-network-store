/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;
import com.powsybl.network.store.model.StandbyAutomatonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StandbyAutomatonImpl implements StandbyAutomaton {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyAutomatonImpl.class);

    private StaticVarCompensator svc;

    private static double checkB0(double b0) {
        if (Double.isNaN(b0)) {
            throw new IllegalArgumentException("b0 is invalid");
        }
        return b0;
    }

    private static void checkVoltageConfig(double lowVoltageSetpoint, double highVoltageSetpoint,
                                           double lowVoltageThreshold, double highVoltageThreshold) {
        if (Double.isNaN(lowVoltageSetpoint)) {
            throw new IllegalArgumentException("lowVoltageSetpoint is invalid");
        }
        if (Double.isNaN(highVoltageSetpoint)) {
            throw new IllegalArgumentException("highVoltageSetpoint is invalid");
        }
        if (Double.isNaN(lowVoltageThreshold)) {
            throw new IllegalArgumentException("lowVoltageThreshold is invalid");
        }
        if (Double.isNaN(highVoltageThreshold)) {
            throw new IllegalArgumentException("highVoltageThreshold is invalid");
        }
        if (lowVoltageThreshold >= highVoltageThreshold) {
            throw new IllegalArgumentException("Inconsistent low (" + lowVoltageThreshold + ") and high (" + highVoltageThreshold + ") voltage thresholds");
        }
        if (lowVoltageSetpoint < lowVoltageThreshold) {
            LOGGER.warn("Invalid low voltage setpoint {} < threshold {}", lowVoltageSetpoint, lowVoltageThreshold);
        }
        if (highVoltageSetpoint > highVoltageThreshold) {
            LOGGER.warn("Invalid high voltage setpoint {} > threshold {}", highVoltageSetpoint, highVoltageThreshold);
        }
    }

    public static StandbyAutomatonAttributes createAttributes(StandbyAutomaton standbyAutomaton) {
        return createAttributes(standbyAutomaton.getB0(),
                                standbyAutomaton.isStandby(),
                                standbyAutomaton.getLowVoltageSetpoint(),
                                standbyAutomaton.getHighVoltageSetpoint(),
                                standbyAutomaton.getLowVoltageThreshold(),
                                standbyAutomaton.getHighVoltageThreshold());
    }

    public static StandbyAutomatonAttributes createAttributes(double b0, boolean standby, double lowVoltageSetpoint, double highVoltageSetpoint,
                                                              double lowVoltageThreshold, double highVoltageThreshold) {
        checkVoltageConfig(lowVoltageSetpoint, highVoltageSetpoint, lowVoltageThreshold, highVoltageThreshold);
        checkB0(b0);
        return StandbyAutomatonAttributes.builder()
                .b0(b0)
                .standby(standby)
                .lowVoltageSetpoint(lowVoltageSetpoint)
                .highVoltageSetpoint(highVoltageSetpoint)
                .lowVoltageThreshold(lowVoltageThreshold)
                .highVoltageThreshold(highVoltageThreshold)
                .build();
    }

    public StandbyAutomatonImpl(StaticVarCompensator svc) {
        this.svc = Objects.requireNonNull(svc);
    }

    private StandbyAutomatonAttributes getAttributes() {
        return ((StaticVarCompensatorImpl) svc).getResource().getAttributes().getStandbyAutomaton();
    }

    private void updateResource() {
        ((StaticVarCompensatorImpl) svc).updateResource();
    }

    @Override
    public boolean isStandby() {
        return getAttributes().isStandby();
    }

    @Override
    public StandbyAutomatonImpl setStandby(boolean standby) {
        getAttributes().setStandby(standby);
        return this;
    }

    @Override
    public double getB0() {
        return getAttributes().getB0();
    }

    @Override
    public StandbyAutomatonImpl setB0(double b0) {
        getAttributes().setB0(checkB0(b0));
        updateResource();
        return this;
    }

    @Override
    public double getHighVoltageSetpoint() {
        return getAttributes().getHighVoltageSetpoint();
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageSetpoint(double highVoltageSetpoint) {
        checkVoltageConfig(getLowVoltageSetpoint(), highVoltageSetpoint, getLowVoltageThreshold(), getHighVoltageThreshold());
        getAttributes().setHighVoltageSetpoint(highVoltageSetpoint);
        updateResource();
        return this;
    }

    @Override
    public double getHighVoltageThreshold() {
        return getAttributes().getHighVoltageThreshold();
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageThreshold(double highVoltageThreshold) {
        checkVoltageConfig(getLowVoltageSetpoint(), getHighVoltageSetpoint(), getLowVoltageThreshold(), highVoltageThreshold);
        getAttributes().setHighVoltageThreshold(highVoltageThreshold);
        updateResource();
        return this;
    }

    @Override
    public double getLowVoltageSetpoint() {
        return getAttributes().getLowVoltageSetpoint();
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageSetpoint(double lowVoltageSetpoint) {
        checkVoltageConfig(lowVoltageSetpoint, getHighVoltageSetpoint(), getLowVoltageThreshold(), getHighVoltageThreshold());
        getAttributes().setLowVoltageSetpoint(lowVoltageSetpoint);
        updateResource();
        return this;
    }

    @Override
    public double getLowVoltageThreshold() {
        return getAttributes().getLowVoltageThreshold();
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageThreshold(double lowVoltageThreshold) {
        checkVoltageConfig(getLowVoltageSetpoint(), getHighVoltageSetpoint(), lowVoltageThreshold, getHighVoltageThreshold());
        getAttributes().setLowVoltageThreshold(lowVoltageThreshold);
        return this;
    }

    @Override
    public StaticVarCompensator getExtendable() {
        return svc;
    }

    @Override
    public void setExtendable(StaticVarCompensator svc) {
        this.svc = svc;
    }
}
