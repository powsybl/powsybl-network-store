/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

import java.util.Objects;
import java.util.function.Consumer;

final class VoltageRegulationAdderImpl<T extends VoltageRegulationHolderAdder<T>> implements VoltageRegulationAdder<T> {

    private final Class<? extends VoltageRegulationHolder<?>> holderClass;
    private final Validable validable;
    private final NetworkImpl network;
    private final T holderAdder;
    private final Consumer<VoltageRegulation.VoltageRegulationAttributes> consumer;
    private RegulationMode mode;
    private boolean regulating = true;
    private Terminal terminal;
    private double targetValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private double slope = Double.NaN;

    VoltageRegulationAdderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                               Validable validable,
                               NetworkImpl network,
                               T holderAdder,
                               Consumer<VoltageRegulation.VoltageRegulationAttributes> consumer) {
        this.holderClass = Objects.requireNonNull(holderClass);
        this.validable = Objects.requireNonNull(validable);
        this.network = Objects.requireNonNull(network);
        this.holderAdder = Objects.requireNonNull(holderAdder);
        this.consumer = Objects.requireNonNull(consumer);
    }

    @Override
    public VoltageRegulationAdder<T> withTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withSlope(double slope) {
        this.slope = slope;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withMode(RegulationMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public VoltageRegulationAdder<T> withRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public T add() {
        VoltageRegulation.VoltageRegulationAttributes attributes = new VoltageRegulation.VoltageRegulationAttributes(
            targetValue, targetDeadband, slope, mode, regulating, terminal);
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network);
        VoltageRegulationValidation.checkAttributes(validable, attributes, holderClass, network);
        consumer.accept(attributes);
        return holderAdder;
    }
}
