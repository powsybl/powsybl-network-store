/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;

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
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getBmax() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getVoltageSetPoint() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public StaticVarCompensator setVoltageSetPoint(double voltageSetPoint) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getReactivePowerSetPoint() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public StaticVarCompensator setReactivePowerSetPoint(double reactivePowerSetPoint) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RegulationMode getRegulationMode() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        throw new UnsupportedOperationException("TODO");
    }
}
