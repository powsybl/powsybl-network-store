/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0, or (at your option) any later version.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.function.Function;

final class VoltageRegulationBuilderImpl
        extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder>
        implements VoltageRegulationBuilder {

    VoltageRegulationBuilderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                 Validable validable,
                                 VoltageRegulationHolder<?> holder,
                                 NetworkObjectIndex index,
                                 Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter) {
        super(holderClass, validable, holder, index, setter);
    }

    @Override
    protected VoltageRegulationBuilder self() {
        return this;
    }

    @Override
    public VoltageRegulation build() {
        return setter.apply(checkAndGetAttributes());
    }
}
