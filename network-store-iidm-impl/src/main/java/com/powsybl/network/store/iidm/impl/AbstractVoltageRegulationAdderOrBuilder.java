/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * Base class for {@link com.powsybl.iidm.network.regulation.VoltageRegulationAdder} and
 * {@link com.powsybl.iidm.network.regulation.VoltageRegulationBuilder} network-store implementations.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
abstract class AbstractVoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>> implements VoltageRegulationAdderOrBuilder<T> {

    // Context
    final Class<? extends VoltageRegulationHolder<?>> holderClass;
    final Validable validable;
    final VoltageRegulationHolder<?> holder;
    final NetworkImpl network;
    // VoltageRegulation attributes
    RegulationMode mode = null;
    boolean regulating = true;
    Terminal terminal = null;
    double targetValue = Double.NaN;
    double targetDeadband = Double.NaN;
    double slope = Double.NaN;

    AbstractVoltageRegulationAdderOrBuilder(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                            Validable validable,
                                            VoltageRegulationHolder<?> holder,
                                            NetworkImpl network) {
        this.holderClass = holderClass;
        this.holder = holder;
        this.validable = validable;
        this.network = network;
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
    public T withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return self();
    }

    @Override
    public T withMode(RegulationMode mode) {
        this.mode = mode;
        return self();
    }

    @Override
    public T withRegulating(boolean regulating) {
        this.regulating = regulating;
        return self();
    }

    void checkVoltageRegulationAttributes() {
        // RTC load tap changing capabilities
        if (holder instanceof RatioTapChanger ratioTapChanger) {
            ValidationUtil.checkRTCLoadTapChangingCapabilities(validable,
                ratioTapChanger.hasLoadTapChangingCapabilities(),
                regulating,
                network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode());
        }
        // MODE
        ValidationUtil.checkVoltageRegulationMode(validable,
            mode, regulating, isWithTerminal(),
            holderClass, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        // SLOPE
        ValidationUtil.checkVoltageRegulationSlope(validable,
            slope, mode, regulating,
            network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        // DEADBAND
        ValidationUtil.checkVoltageRegulationDeadband(validable,
            targetDeadband, regulating,
            holderClass,
            network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        // TERMINAL
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network);
        ValidationUtil.checkVoltageRegulationTerminal(validable,
            terminal, regulating,
            network,
            holderClass,
            network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        // TARGET VALUE (check after terminal and mode)
        ValidationUtil.checkVoltageRegulationTargetValue(validable,
            targetValue, mode, regulating, isWithTerminal(), holderClass,
            network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
    }

    private boolean isWithTerminal() {
        return terminal != null;
    }

    protected abstract T self();
}
