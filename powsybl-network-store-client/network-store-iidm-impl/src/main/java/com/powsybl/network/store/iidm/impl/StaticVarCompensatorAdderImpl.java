/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorAdderImpl extends AbstractInjectionAdder<StaticVarCompensatorAdderImpl> implements StaticVarCompensatorAdder {

    private double bMin = Double.NaN;

    private double bMax = Double.NaN;

    private double voltageSetPoint = Double.NaN;

    private double reactivePowerSetPoint = Double.NaN;

    StaticVarCompensator.RegulationMode regulationMode;

    private Terminal regulatingTerminal;

    StaticVarCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public StaticVarCompensatorAdder setBmin(double bMin) {
        this.bMin = bMin;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBmax(double bMax) {
        this.bMax = bMax;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setVoltageSetPoint(double voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetPoint(double reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public StaticVarCompensator add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkSvcRegulator(this, voltageSetPoint, reactivePowerSetPoint, regulationMode, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);

        Resource<StaticVarCompensatorAttributes> resource = Resource.staticVarCompensatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .bmin(bMin)
                        .bmax(bMax)
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .regulationMode(regulationMode)
                        .regulatingTerminal(terminalRefAttributes)
                        .build())
                .build();
        StaticVarCompensatorImpl svc = getIndex().createStaticVarCompensator(resource);
        svc.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return svc;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.STATIC_VAR_COMPENSATOR.getDescription();
    }
}
