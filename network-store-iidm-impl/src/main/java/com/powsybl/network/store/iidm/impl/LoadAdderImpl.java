/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadAdderImpl extends AbstractInjectionAdder<LoadAdderImpl> implements LoadAdder {

    private LoadType loadType = LoadType.UNDEFINED;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    LoadAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index, String parentNetwork) {
        super(voltageLevelResource, index, parentNetwork);
    }

    @Override
    public LoadAdder setLoadType(LoadType loadType) {
        this.loadType = loadType;
        return this;

    }

    @Override
    public LoadAdder setP0(double p0) {
        this.p0 = p0;
        return this;

    }

    @Override
    public LoadAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public Load add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkLoadType(this, loadType);
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Resource<LoadAttributes> resource = Resource.loadBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .parentNetwork(getParentNetwork())
                .attributes(LoadAttributes.builder()
                                          .voltageLevelId(getVoltageLevelResource().getId())
                                          .name(getName())
                                          .fictitious(isFictitious())
                                          .node(getNode())
                                          .bus(getBus())
                                          .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                                          .loadType(loadType)
                                          .p0(p0)
                                          .q0(q0)
                                          .build())
                .build();
        LoadImpl load = getIndex().createLoad(resource);
        load.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return load;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.LOAD.getDescription();
    }

    @Override
    public ZipLoadModelAdder newZipModel() {
        //FIXME Dummy zip load model adder
        return new ZipLoadModelAdder() {
            @Override
            public ZipLoadModelAdder setC0p(double v) {
                return this;
            }

            @Override
            public ZipLoadModelAdder setC1p(double v) {
                return this;
            }

            @Override
            public ZipLoadModelAdder setC2p(double v) {
                return this;
            }

            @Override
            public ZipLoadModelAdder setC0q(double v) {
                return this;
            }

            @Override
            public ZipLoadModelAdder setC1q(double v) {
                return this;
            }

            @Override
            public ZipLoadModelAdder setC2q(double v) {
                return this;
            }

            @Override
            public LoadAdder add() {
                return null;
            }
        };
    }

    @Override
    public ExponentialLoadModelAdder newExponentialModel() {
        //FIXME Dummy exponential load model adder
        return new ExponentialLoadModelAdder() {
            @Override
            public ExponentialLoadModelAdder setNp(double v) {
                return this;
            }

            @Override
            public ExponentialLoadModelAdder setNq(double v) {
                return this;
            }

            @Override
            public LoadAdder add() {
                return null;
            }
        };
    }
}
