/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorImpl extends AbstractInjectionImpl<StaticVarCompensator, StaticVarCompensatorAttributes> implements StaticVarCompensator {

    public StaticVarCompensatorImpl(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        super(index, resource);
    }

    static StaticVarCompensatorImpl create(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        return new StaticVarCompensatorImpl(index, resource);
    }

    @Override
    protected StaticVarCompensator getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.STATIC_VAR_COMPENSATOR;
    }

    @Override
    public double getBmin() {
        return resource.getAttributes().getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        resource.getAttributes().setBmin(bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return resource.getAttributes().getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        resource.getAttributes().setBmax(bMax);
        return this;
    }

    @Override
    public double getVoltageSetPoint() {
        return resource.getAttributes().getVoltageSetPoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetPoint(double voltageSetPoint) {
        resource.getAttributes().setVoltageSetPoint(voltageSetPoint);
        return this;
    }

    @Override
    public double getReactivePowerSetPoint() {
        return resource.getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetPoint(double reactivePowerSetPoint) {
        resource.getAttributes().setReactivePowerSetPoint(reactivePowerSetPoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return resource.getAttributes().getRegulationMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        resource.getAttributes().setRegulationMode(regulationMode);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        return TerminalRefUtils.getRegulatingTerminal(index, terminalRefAttributes);
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.regulatingTerminalToTerminaRefAttributes(regulatingTerminal));
        return this;
    }

    protected String getTypeDescription() {
        return "staticVarCompensator";
    }
}
