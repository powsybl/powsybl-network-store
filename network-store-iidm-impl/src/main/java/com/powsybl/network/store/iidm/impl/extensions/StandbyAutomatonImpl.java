/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.StandbyAutomatonAttributes;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StandbyAutomatonImpl extends AbstractExtension<StaticVarCompensator> implements StandbyAutomaton {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyAutomatonImpl.class);

    private static double checkB0(Validable validable, double b0) {
        if (Double.isNaN(b0)) {
            throw new ValidationException(validable, "b0 is invalid");
        }
        return b0;
    }

    private static void checkVoltageConfig(Validable validable, double lowVoltageSetpoint, double highVoltageSetpoint,
                                           double lowVoltageThreshold, double highVoltageThreshold, boolean standby, String svcId) {
        if (Double.isNaN(lowVoltageSetpoint)) {
            throw new ValidationException(validable, String.format("low voltage setpoint (%s) is invalid", lowVoltageSetpoint));

        }
        if (Double.isNaN(highVoltageSetpoint)) {
            throw new ValidationException(validable, String.format("high voltage setpoint (%s) is invalid", highVoltageSetpoint));
        }
        if (Double.isNaN(lowVoltageThreshold)) {
            throw new ValidationException(validable, String.format("low voltage threshold (%s) is invalid", lowVoltageThreshold));
        }
        if (Double.isNaN(highVoltageThreshold)) {
            throw new ValidationException(validable, String.format("high voltage threshold (%s) is invalid", highVoltageThreshold));

        }
        if (lowVoltageThreshold >= highVoltageThreshold) {
            if (standby) {
                throw new ValidationException(validable,
                    String.format("Inconsistent low (%s) and high (%s) voltage thresholds",
                        lowVoltageThreshold,
                        highVoltageThreshold));
            } else {
                LOGGER.warn("Inconsistent low {} and high ({}) voltage thresholds for StaticVarCompensator {}",
                    lowVoltageSetpoint, lowVoltageThreshold, svcId);
            }
        }
        if (lowVoltageSetpoint < lowVoltageThreshold) {
            LOGGER.warn("Invalid low voltage setpoint {} < threshold {}", lowVoltageSetpoint, lowVoltageThreshold);
        }
        if (highVoltageSetpoint > highVoltageThreshold) {
            LOGGER.warn("Invalid high voltage setpoint {} > threshold {}", highVoltageSetpoint, highVoltageThreshold);
        }
    }

    public static StandbyAutomatonAttributes createAttributes(Validable validable, double b0, boolean standby, double lowVoltageSetpoint, double highVoltageSetpoint,
                                                              double lowVoltageThreshold, double highVoltageThreshold, String svcId) {
        checkVoltageConfig(validable, lowVoltageSetpoint, highVoltageSetpoint, lowVoltageThreshold, highVoltageThreshold, standby, svcId);
        checkB0(validable, b0);
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
        super(svc);
    }

    private StaticVarCompensatorImpl getSvc() {
        return (StaticVarCompensatorImpl) getExtendable();
    }

    private StandbyAutomatonAttributes getAttributes() {
        return getAttributes(getSvc().getResource());
    }

    private static StandbyAutomatonAttributes getAttributes(Resource<StaticVarCompensatorAttributes> resource) {
        return resource.getAttributes().getStandbyAutomaton();
    }

    @Override
    public boolean isStandby() {
        return getAttributes().isStandby();
    }

    @Override
    public StandbyAutomatonImpl setStandby(boolean standby) {
        checkVoltageConfig(getSvc(), getLowVoltageSetpoint(), getHighVoltageSetpoint(), getLowVoltageThreshold(),
            getHighVoltageThreshold(), standby, getSvc().getId());
        getAttributes().setStandby(standby);
        return this;
    }

    @Override
    public double getB0() {
        return getAttributes().getB0();
    }

    @Override
    public StandbyAutomatonImpl setB0(double b0) {
        double oldValue = getB0();
        if (oldValue != b0) {
            getSvc().updateResourceExtension(this, res -> getAttributes(res).setB0(checkB0(getSvc(), b0)), "b0", oldValue, b0);
        }
        return this;
    }

    @Override
    public double getHighVoltageSetpoint() {
        return getAttributes().getHighVoltageSetpoint();
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageSetpoint(double highVoltageSetpoint) {
        checkVoltageConfig(getSvc(), getLowVoltageSetpoint(), highVoltageSetpoint, getLowVoltageThreshold(), getHighVoltageThreshold(), isStandby(), getSvc().getId());
        double oldValue = getHighVoltageSetpoint();
        if (oldValue != highVoltageSetpoint) {
            getSvc().updateResourceExtension(this, res -> getAttributes(res).setHighVoltageSetpoint(highVoltageSetpoint), "highVoltageSetpoint", oldValue, highVoltageSetpoint);
        }
        return this;
    }

    @Override
    public double getHighVoltageThreshold() {
        return getAttributes().getHighVoltageThreshold();
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageThreshold(double highVoltageThreshold) {
        checkVoltageConfig(getSvc(), getLowVoltageSetpoint(), getHighVoltageSetpoint(), getLowVoltageThreshold(), highVoltageThreshold, isStandby(), getSvc().getId());
        double oldValue = getHighVoltageThreshold();
        if (oldValue != highVoltageThreshold) {
            getSvc().updateResourceExtension(this, res -> getAttributes(res).setHighVoltageThreshold(highVoltageThreshold), "highVoltageThreshold", oldValue, highVoltageThreshold);
        }
        return this;
    }

    @Override
    public double getLowVoltageSetpoint() {
        return getAttributes().getLowVoltageSetpoint();
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageSetpoint(double lowVoltageSetpoint) {
        checkVoltageConfig(getSvc(), lowVoltageSetpoint, getHighVoltageSetpoint(), getLowVoltageThreshold(), getHighVoltageThreshold(), isStandby(), getSvc().getId());
        double oldValue = getLowVoltageSetpoint();
        if (oldValue != lowVoltageSetpoint) {
            getSvc().updateResourceExtension(this, res -> getAttributes(res).setLowVoltageSetpoint(lowVoltageSetpoint), "lowVoltageSetpoint", oldValue, lowVoltageSetpoint);
        }
        return this;
    }

    @Override
    public double getLowVoltageThreshold() {
        return getAttributes().getLowVoltageThreshold();
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageThreshold(double lowVoltageThreshold) {
        checkVoltageConfig(getSvc(), getLowVoltageSetpoint(), getHighVoltageSetpoint(), lowVoltageThreshold, getHighVoltageThreshold(), isStandby(), getSvc().getId());
        double oldValue = getLowVoltageThreshold();
        if (oldValue != lowVoltageThreshold) {
            getSvc().updateResourceExtension(this, res -> getAttributes(res).setLowVoltageThreshold(lowVoltageThreshold), "lowVoltageThreshold", oldValue, lowVoltageThreshold);
        }
        return this;
    }
}
