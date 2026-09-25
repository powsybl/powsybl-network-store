/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0, or (at your option) any later version.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.Objects;
import java.util.function.Function;

abstract class AbstractVoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>>
        implements VoltageRegulationAdderOrBuilder<T> {

    protected final Class<? extends VoltageRegulationHolder<?>> holderClass;
    protected final Validable validable;
    protected final VoltageRegulationHolder<?> holder;
    protected final NetworkObjectIndex index;
    protected final Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter;

    private double targetValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private double slope = Double.NaN;
    private com.powsybl.iidm.network.Terminal terminal;
    private com.powsybl.iidm.network.regulation.RegulationMode mode;
    private boolean regulating = true;

    AbstractVoltageRegulationAdderOrBuilder(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                            Validable validable,
                                            VoltageRegulationHolder<?> holder,
                                            NetworkObjectIndex index,
                                            Function<VoltageRegulation.VoltageRegulationAttributes, VoltageRegulation> setter) {
        this.holderClass = Objects.requireNonNull(holderClass);
        this.validable = Objects.requireNonNull(validable);
        this.holder = holder;
        this.index = Objects.requireNonNull(index);
        this.setter = Objects.requireNonNull(setter);
    }

    @Override
    public T withTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return self();
    }

    @Override
    public T withTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return self();
    }

    @Override
    public T withSlope(double slope) {
        this.slope = slope;
        return self();
    }

    @Override
    public T withTerminal(com.powsybl.iidm.network.Terminal terminal) {
        this.terminal = terminal;
        return self();
    }

    @Override
    public T withMode(com.powsybl.iidm.network.regulation.RegulationMode mode) {
        this.mode = mode;
        return self();
    }

    @Override
    public T withRegulating(boolean regulating) {
        this.regulating = regulating;
        return self();
    }

    protected VoltageRegulation.VoltageRegulationAttributes checkAndGetAttributes() {
        boolean terminalAlreadyConfigured = holder != null
                && holder.getVoltageRegulation() != null
                && Objects.equals(holder.getVoltageRegulation().getTerminal(), terminal);
        if (terminal != null && index.getNetwork().getVariantManager().getVariantIds().size() > 1
                && !terminalAlreadyConfigured) {
            String action = holder != null && holder.getVoltageRegulation() != null ? "change" : "set";
            throw new com.powsybl.commons.PowsyblException(validable.getMessageHeader()
                    + "Cannot " + action + " terminal when there are multiple variants");
        }
        VoltageRegulation.VoltageRegulationAttributes attributes =
                new VoltageRegulation.VoltageRegulationAttributes(targetValue, targetDeadband, slope, mode, regulating, terminal);
        ValidationUtil.checkRegulatingTerminal(validable, terminal, index.getNetwork());
        ValidationUtil.checkVoltageRegulation(validable, attributes, index.getNetwork(), holderClass,
                index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        if (holder != null) {
            ValidationUtil.checkLocalTargetQandV(validable, holderClass,
                    holder.getLocalTargetV(), holder.getLocalTargetQ(), attributes,
                    index.getNetwork().getMinValidationLevel(), index.getNetwork().getReportNodeContext().getReportNode());
        }
        return attributes;
    }

    protected abstract T self();
}
