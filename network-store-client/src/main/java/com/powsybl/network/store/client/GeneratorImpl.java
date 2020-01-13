/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.ReactiveLimitsAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorImpl extends AbstractInjectionImpl<Generator, GeneratorAttributes> implements Generator, ReactiveLimitsOwner {

    public GeneratorImpl(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        super(index, resource);
    }

    static GeneratorImpl create(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        return new GeneratorImpl(index, resource);
    }

    @Override
    protected Generator getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.GENERATOR;
    }

    @Override
    public EnergySource getEnergySource() {
        return resource.getAttributes().getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        resource.getAttributes().setEnergySource(energySource);
        return this;
    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        resource.getAttributes().setMaxP(maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return resource.getAttributes().getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        resource.getAttributes().setMinP(minP);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getTargetV() {
        return resource.getAttributes().getTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        resource.getAttributes().setTargetV(targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return resource.getAttributes().getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        resource.getAttributes().setTargetP(targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return resource.getAttributes().getTargetQ();
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        resource.getAttributes().setTargetQ(targetQ);
        return this;
    }

    @Override
    public double getRatedS() {
        return resource.getAttributes().getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
        resource.getAttributes().setRatedS(ratedS);
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        //throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }
}
