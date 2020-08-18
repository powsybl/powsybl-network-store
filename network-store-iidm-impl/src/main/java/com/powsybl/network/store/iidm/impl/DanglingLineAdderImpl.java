/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class DanglingLineAdderImpl extends AbstractInjectionAdder<DanglingLineAdderImpl> implements DanglingLineAdder {

    // TODO : mock of dangling line generation part
    class GenerationAdderImpl implements GenerationAdder {

        @Override
        public GenerationAdder setTargetP(double targetP) {
            return this;
        }

        @Override
        public GenerationAdder setMaxP(double maxP) {
            return this;
        }

        @Override
        public GenerationAdder setMinP(double minP) {
            return this;
        }

        @Override
        public GenerationAdder setTargetQ(double targetQ) {
            return this;
        }

        @Override
        public GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn) {
            return this;
        }

        @Override
        public GenerationAdder setTargetV(double targetV) {
            return this;
        }

        @Override
        public DanglingLineAdder add() {
            generation = new DanglingLineImpl.GenerationImpl(0., 0., 0., 0., false, 0.);
            return DanglingLineAdderImpl.this;
        }
    }

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private String ucteXNodeCode = null;

    private DanglingLineImpl.GenerationImpl generation;

    DanglingLineAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public DanglingLineAdder setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public DanglingLineAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public DanglingLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DanglingLineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public DanglingLineAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public DanglingLineAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public DanglingLineAdder setUcteXnodeCode(String ucteXNodeCode) {
        this.ucteXNodeCode = ucteXNodeCode;
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        // TODO
        return new GenerationAdderImpl();
    }

    @Override
    public DanglingLine add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        Resource<DanglingLineAttributes> resource = Resource.danglingLineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(DanglingLineAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .p0(p0)
                        .q0(q0)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ucteXnodeCode(ucteXNodeCode)
                        .build())
                .build();
        return getIndex().createDanglingLine(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Dangling line";
    }
}
