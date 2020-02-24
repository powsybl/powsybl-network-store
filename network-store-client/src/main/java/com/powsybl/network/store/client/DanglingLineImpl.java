/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.CurrentLimitsAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class DanglingLineImpl  extends AbstractInjectionImpl<DanglingLine, DanglingLineAttributes> implements DanglingLine, CurrentLimitsOwner<Void> {

    public DanglingLineImpl(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        super(index, resource);
    }

    static DanglingLineImpl create(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        return new DanglingLineImpl(index, resource);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.DANGLING_LINE;
    }

    @Override
    protected DanglingLine getInjection() {
        return this;
    }

    @Override
    public double getP0() {
        return resource.getAttributes().getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        resource.getAttributes().setP0(p0);
        return this;
    }

    @Override
    public double getQ0() {
        return resource.getAttributes().getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        resource.getAttributes().setQ0(q0);
        return this;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public DanglingLine setR(double r) {
        resource.getAttributes().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public DanglingLine setX(double x) {
        resource.getAttributes().setX(x);
        return this;
    }

    @Override
    public double getG() {
        return resource.getAttributes().getG();
    }

    @Override
    public DanglingLine setG(double g) {
        resource.getAttributes().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return resource.getAttributes().getB();
    }

    @Override
    public DanglingLine setB(double b) {
        resource.getAttributes().setB(b);
        return this;
    }

    @Override
    public String getUcteXnodeCode() {
        return resource.getAttributes().getUcteXnodeCode();
    }

    @Override
    public void setCurrentLimits(Void side, CurrentLimitsAttributes currentLimits) {
        resource.getAttributes().setCurrentLimits(currentLimits);
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return new CurrentLimitsImpl(resource.getAttributes().getCurrentLimits());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl<>(null, this);
    }
}
