/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.function.Function;

/**
 * Network-store implementation of {@link VoltageRegulationBuilder}.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
class VoltageRegulationBuilderImpl extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder> implements VoltageRegulationBuilder {

    private final Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> voltageRegulationSetter;

    VoltageRegulationBuilderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                 Validable validable,
                                 VoltageRegulationHolder<?> holder,
                                 NetworkImpl network,
                                 Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> voltageRegulationSetter) {
        super(holderClass, validable, holder, network);
        this.voltageRegulationSetter = voltageRegulationSetter;
    }

    @Override
    protected VoltageRegulationBuilder self() {
        return this;
    }

    @Override
    public VoltageRegulation build() {
        checkVoltageRegulationAttributes();
        VoltageRegulation.VoltageRegulationAttributes attributes =
            new VoltageRegulation.VoltageRegulationAttributes(targetValue, targetDeadband, slope, mode, regulating, terminal);
        ValidationUtil.checkLocalTargetQandV(validable,
            holderClass,
            holder.getLocalTargetV(),
            holder.getLocalTargetQ(),
            attributes,
            network.getMinValidationLevel(),
            network.getReportNodeContext().getReportNode());
        return voltageRegulationSetter.apply(attributes);
    }
}
