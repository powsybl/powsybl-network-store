/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerAdderImpl extends AbstractBranchAdder<TwoWindingsTransformerAdderImpl> implements TwoWindingsTransformerAdder {

    private SubstationImpl substation;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

    private double ratedS = Double.NaN;

    TwoWindingsTransformerAdderImpl(NetworkObjectIndex index, SubstationImpl substation) {
        super(index);
        this.substation = substation;
    }

    @Override
    public TwoWindingsTransformerAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU1(double ratedU1) {
        this.ratedU1 = ratedU1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU2(double ratedU2) {
        this.ratedU2 = ratedU2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public TwoWindingsTransformer add() {
        String id = checkAndGetUniqueId();
        VoltageLevel voltageLevel1 = checkVoltageLevel1();
        VoltageLevel voltageLevel2 = checkVoltageLevel2();

        if (substation != null) {
            if (voltageLevel1.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel2.getSubstation().map(s -> s != substation).orElse(true)) {
                throw new ValidationException(this,
                        "the 2 windings of the transformer shall belong to the substation '"
                                + substation.getId() + "' ('" + voltageLevel1.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel2.getSubstation().map(Substation::getId).orElse("null") + "')");
            }
        } else if (voltageLevel1.getSubstation().isPresent() || voltageLevel2.getSubstation().isPresent()) {
            throw new ValidationException(this,
                    "the 2 windings of the transformer shall belong to a substation since there are located in voltage levels with substations ('"
                            + voltageLevel1.getId() + "', '" + voltageLevel2.getId() + "')");
        }

        checkNodeBus1();
        checkNodeBus2();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);
        ValidationUtil.checkRatedU1(this, ratedU1);
        ValidationUtil.checkRatedU2(this, ratedU2);
        ValidationUtil.checkRatedS(this, ratedS);

        Resource<TwoWindingsTransformerAttributes> resource = Resource.twoWindingsTransformerBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1(getVoltageLevelId1())
                        .voltageLevelId2(getVoltageLevelId2())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node1(getNode1())
                        .node2(getNode2())
                        .bus1(getBus1())
                        .bus2(getBus2())
                        .connectableBus1(getConnectableBus1() != null ? getConnectableBus1() : getBus1())
                        .connectableBus2(getConnectableBus2() != null ? getConnectableBus2() : getBus2())
                        .r(r)
                        .x(x)
                        .b(b)
                        .g(g)
                        .ratedU1(ratedU1)
                        .ratedU2(ratedU2)
                        .ratedS(ratedS)
                        .fictitious(isFictitious())
                        .build())
                .build();
        TwoWindingsTransformerImpl transformer = getIndex().createTwoWindingsTransformer(resource);
        transformer.getTerminal1().getVoltageLevel().invalidateCalculatedBuses();
        transformer.getTerminal2().getVoltageLevel().invalidateCalculatedBuses();
        return transformer;
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }
}
