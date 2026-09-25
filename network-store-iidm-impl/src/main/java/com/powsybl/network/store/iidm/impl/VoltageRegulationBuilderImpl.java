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
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

final class VoltageRegulationBuilderImpl implements VoltageRegulationBuilder {

    private final Class<? extends VoltageRegulationHolder<?>> holderClass;
    private final Validable validable;
    private final NetworkImpl network;
    private final DoubleSupplier localTargetVSupplier;
    private final DoubleSupplier localTargetQSupplier;
    private final Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter;
    private RegulationMode mode;
    private boolean regulating = true;
    private Terminal terminal;
    private double targetValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private double slope = Double.NaN;

    VoltageRegulationBuilderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                 Validable validable,
                                 NetworkImpl network,
                                 DoubleSupplier localTargetVSupplier,
                                 DoubleSupplier localTargetQSupplier,
                                 Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter) {
        this.holderClass = Objects.requireNonNull(holderClass);
        this.validable = Objects.requireNonNull(validable);
        this.network = Objects.requireNonNull(network);
        this.localTargetVSupplier = Objects.requireNonNull(localTargetVSupplier);
        this.localTargetQSupplier = Objects.requireNonNull(localTargetQSupplier);
        this.setter = Objects.requireNonNull(setter);
    }

    @Override
    public VoltageRegulationBuilder withTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    @Override
    public VoltageRegulationBuilder withTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public VoltageRegulationBuilder withSlope(double slope) {
        this.slope = slope;
        return this;
    }

    @Override
    public VoltageRegulationBuilder withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public VoltageRegulationBuilder withMode(RegulationMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public VoltageRegulationBuilder withRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public VoltageRegulation build() {
        VoltageRegulation.VoltageRegulationAttributes attributes = new VoltageRegulation.VoltageRegulationAttributes(
            targetValue, targetDeadband, slope, mode, regulating, terminal);
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network);
        VoltageRegulationValidation.checkAttributes(validable, attributes, holderClass, network);
        if (attributes.terminal() == null || holderClass != com.powsybl.iidm.network.ShuntCompensator.class) {
            ValidationUtil.checkLocalTargetQandV(validable, holderClass,
                localTargetVSupplier.getAsDouble(), localTargetQSupplier.getAsDouble(), attributes, network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode());
        }
        return setter.apply(attributes);
    }
}
