/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0, or (at your option) any later version.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

import java.util.function.Function;

final class VoltageRegulationAdderImpl<T extends VoltageRegulationHolderAdder<T>>
        extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<T>>
        implements VoltageRegulationAdder<T> {

    private final T holderAdder;

    VoltageRegulationAdderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                               Validable validable,
                               VoltageRegulationHolder<?> holder,
                               NetworkObjectIndex index,
                               T holderAdder,
                               Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter) {
        super(holderClass, validable, holder, index, setter);
        this.holderAdder = holderAdder;
    }

    @Override
    protected VoltageRegulationAdder<T> self() {
        return this;
    }

    @Override
    public T add() {
        setter.apply(checkAndGetAttributes());
        return holderAdder;
    }
}
