/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

import java.util.function.Consumer;

/**
 * Network-store implementation of {@link VoltageRegulationAdder}, used on equipment adders.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
class VoltageRegulationAdderImpl<T extends VoltageRegulationHolderAdder<T>> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<T>> implements VoltageRegulationAdder<T> {

    private final T equipmentAdder;
    private final Consumer<VoltageRegulation.VoltageRegulationAttributes> voltageRegulationAttributesConsumer;

    VoltageRegulationAdderImpl(Class<? extends com.powsybl.iidm.network.regulation.VoltageRegulationHolder<?>> holderClass,
                               Validable validable,
                               T equipmentAdder,
                               NetworkImpl network,
                               Consumer<VoltageRegulation.VoltageRegulationAttributes> voltageRegulationAttributesConsumer) {
        super(holderClass, validable, null, network);
        this.equipmentAdder = equipmentAdder;
        this.voltageRegulationAttributesConsumer = voltageRegulationAttributesConsumer;
    }

    @Override
    protected VoltageRegulationAdder<T> self() {
        return this;
    }

    @Override
    public T add() {
        if (voltageRegulationAttributesConsumer != null) {
            checkVoltageRegulationAttributes();
            voltageRegulationAttributesConsumer.accept(
                new VoltageRegulation.VoltageRegulationAttributes(targetValue, targetDeadband, slope, mode, regulating, terminal));
        }
        return equipmentAdder;
    }
}
