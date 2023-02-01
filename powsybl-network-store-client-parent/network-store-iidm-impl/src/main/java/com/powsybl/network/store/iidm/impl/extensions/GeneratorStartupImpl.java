/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class GeneratorStartupImpl extends AbstractExtension<Generator> implements GeneratorStartup {

    public GeneratorStartupImpl(GeneratorImpl generator, double plannedActivePowerSetpoint, double startupCost, double marginalCost,
                                double plannedOutageRate, double forcedOutageRate) {
        super(generator.initGeneratorStartupAttributes(plannedActivePowerSetpoint, startupCost, marginalCost,
                plannedOutageRate, forcedOutageRate));
    }

    @Override
    public double getPlannedActivePowerSetpoint() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .getPlannedActivePowerSetpoint();
    }

    @Override
    public GeneratorStartupImpl setPlannedActivePowerSetpoint(double predefinedActivePowerSetpoint) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .setPlannedActivePowerSetpoint(predefinedActivePowerSetpoint);
        return this;
    }

    @Override
    public double getStartupCost() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .getStartupCost();
    }

    @Override
    public GeneratorStartup setStartupCost(double startUpCost) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .setStartupCost(startUpCost);
        return this;
    }

    @Override
    public double getMarginalCost() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .getMarginalCost();
    }

    @Override
    public GeneratorStartupImpl setMarginalCost(double marginalCost) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .setMarginalCost(marginalCost);
        return this;
    }

    @Override
    public double getPlannedOutageRate() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .getPlannedOutageRate();
    }

    @Override
    public GeneratorStartupImpl setPlannedOutageRate(double plannedOutageRate) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .setPlannedOutageRate(plannedOutageRate);
        return this;
    }

    @Override
    public double getForcedOutageRate() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .getForcedOutageRate();
    }

    @Override
    public GeneratorStartupImpl setForcedOutageRate(double forcedOutageRate) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorStartupAttributes()
                .setForcedOutageRate(forcedOutageRate);
        return this;
    }
}
