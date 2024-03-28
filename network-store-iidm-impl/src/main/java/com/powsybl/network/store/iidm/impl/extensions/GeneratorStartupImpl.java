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
import com.powsybl.network.store.model.GeneratorStartupAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class GeneratorStartupImpl extends AbstractExtension<Generator> implements GeneratorStartup {

    public GeneratorStartupImpl(GeneratorImpl generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    private GeneratorStartupAttributes getGeneratorStartupAttributes() {
        return (GeneratorStartupAttributes) getGenerator().getResource().getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME);
    }

    @Override
    public double getPlannedActivePowerSetpoint() {
        return getGeneratorStartupAttributes().getPlannedActivePowerSetpoint();
    }

    @Override
    public GeneratorStartupImpl setPlannedActivePowerSetpoint(double predefinedActivePowerSetpoint) {
        getGenerator().updateResource(res -> ((GeneratorStartupAttributes) res.getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME)
        ).setPlannedActivePowerSetpoint(predefinedActivePowerSetpoint));
        return this;
    }

    @Override
    public double getStartupCost() {
        return getGeneratorStartupAttributes().getStartupCost();
    }

    @Override
    public GeneratorStartup setStartupCost(double startUpCost) {
        getGenerator().updateResource(res -> ((GeneratorStartupAttributes) res.getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME)
        )
                .setStartupCost(startUpCost));
        return this;
    }

    @Override
    public double getMarginalCost() {
        return getGeneratorStartupAttributes().getMarginalCost();
    }

    @Override
    public GeneratorStartupImpl setMarginalCost(double marginalCost) {
        getGenerator().updateResource(res -> ((GeneratorStartupAttributes) res.getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME)
        )
                .setMarginalCost(marginalCost));
        return this;
    }

    @Override
    public double getPlannedOutageRate() {
        return getGeneratorStartupAttributes().getPlannedOutageRate();
    }

    @Override
    public GeneratorStartupImpl setPlannedOutageRate(double plannedOutageRate) {
        getGenerator().updateResource(res -> ((GeneratorStartupAttributes) res.getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME)
        )
                .setPlannedOutageRate(plannedOutageRate));
        return this;
    }

    @Override
    public double getForcedOutageRate() {
        return getGeneratorStartupAttributes().getForcedOutageRate();
    }

    @Override
    public GeneratorStartupImpl setForcedOutageRate(double forcedOutageRate) {
        getGenerator().updateResource(res -> ((GeneratorStartupAttributes) res.getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME)
        )
                .setForcedOutageRate(forcedOutageRate));
        return this;
    }
}
