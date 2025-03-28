/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorStartupAttributes;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class GeneratorStartupAdderImpl extends AbstractIidmExtensionAdder<Generator, GeneratorStartup> implements GeneratorStartupAdder {

    private double plannedActivePowerSetpoint = Double.NaN;

    private double startupCost = Double.NaN;

    private double marginalCost = Double.NaN;

    private double plannedOutageRate = Double.NaN;

    private double forcedOutageRate = Double.NaN;

    public GeneratorStartupAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorStartup createExtension(Generator generator) {
        GeneratorStartupAttributes oldValue = (GeneratorStartupAttributes) ((GeneratorImpl) generator).getResource().getAttributes().getExtensionAttributes().get(GeneratorStartup.NAME);
        var attributes = new GeneratorStartupAttributes(plannedActivePowerSetpoint, startupCost, marginalCost, plannedOutageRate, forcedOutageRate);
        ((GeneratorImpl) generator).updateResource(res -> res.getAttributes().getExtensionAttributes().put(GeneratorStartup.NAME, attributes),
            "generatorStartup", oldValue, attributes);
        return new GeneratorStartupImpl((GeneratorImpl) generator);
    }

    @Override
    public GeneratorStartupAdderImpl withPlannedActivePowerSetpoint(double plannedActivePowerSetpoint) {
        this.plannedActivePowerSetpoint = plannedActivePowerSetpoint;
        return this;
    }

    @Override
    public GeneratorStartupAdder withStartupCost(double startUpCost) {
        this.startupCost = startUpCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withMarginalCost(double marginalCost) {
        this.marginalCost = marginalCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withPlannedOutageRate(double plannedOutageRate) {
        this.plannedOutageRate = plannedOutageRate;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withForcedOutageRate(double forcedOutageRate) {
        this.forcedOutageRate = forcedOutageRate;
        return this;
    }
}
