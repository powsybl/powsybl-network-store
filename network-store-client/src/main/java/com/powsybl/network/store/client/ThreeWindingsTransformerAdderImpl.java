/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.network.store.model.LegParentAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ThreeWindingsTransformerAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class ThreeWindingsTransformerAdderImpl implements ThreeWindingsTransformerAdder {

    private class LegAdderImpl implements LegAdder {

        private ThreeWindingsTransformerAdder threeWindingsTransformerAdder;

        private String voltageLevelId;

        private int node;

        private String bus;

        private String connectableBus;

        private double r;

        private double x;

        private double g;

        private double b;

        private double ratedU;

        private final int legNumber;

        public LegAdderImpl(int legNumber, ThreeWindingsTransformerAdder threeWindingsTransformerAdder) {
            this.threeWindingsTransformerAdder = threeWindingsTransformerAdder;
            this.legNumber = legNumber;
        }

        @Override
        public LegAdder setVoltageLevel(String voltageLevelId) {
            this.voltageLevelId = voltageLevelId;
            return this;
        }

        @Override
        public LegAdder setNode(int node) {
            this.node = node;
            return this;
        }

        @Override
        public LegAdder setBus(String bus) {
            this.bus = bus;
            return this;
        }

        @Override
        public LegAdder setConnectableBus(String connectableBus) {
            this.connectableBus = connectableBus;
            return this;
        }

        @Override
        public LegAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public LegAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public LegAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public LegAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public LegAdder setRatedU(double ratedU) {
            this.ratedU = ratedU;
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder add() {
            if (legNumber == 1) {
                leg1 = LegParentAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ratedU(ratedU)
                        .legNumber(legNumber)
                        .build();
            } else if (legNumber == 2) {
                leg2 = LegParentAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ratedU(ratedU)
                        .legNumber(legNumber)
                        .build();
            } else if (legNumber == 3) {
                leg3 = LegParentAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ratedU(ratedU)
                        .legNumber(legNumber)
                        .build();
            }

            return threeWindingsTransformerAdder;
        }
    }

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private double ratedU0;

    private LegParentAttributes leg1;

    private LegParentAttributes leg2;

    private LegParentAttributes leg3;

    ThreeWindingsTransformerAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public LegAdder newLeg1() {
        return new LegAdderImpl(1, this);
    }

    @Override
    public LegAdder newLeg2() {
        return new LegAdderImpl(2, this);
    }

    @Override
    public LegAdder newLeg3() {
        return new LegAdderImpl(3, this);
    }

    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        this.ratedU0 = ratedU0;
        return this;
    }

    @Override
    public ThreeWindingsTransformer add() {
        Resource<ThreeWindingsTransformerAttributes> resource = Resource.threeWindingsTransformerBuilder()
                .id(id)
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name(name)
                        .ratedU0(ratedU0)
                        .leg1(leg1)
                        .leg2(leg2)
                        .leg3(leg3)
                        .build())
                .build();
        return index.createThreeWindingsTransformer(resource);
    }

    @Override
    public ThreeWindingsTransformerAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public ThreeWindingsTransformerAdder setEnsureIdUnicity(boolean b) {
        return null;
    }

    @Override
    public ThreeWindingsTransformerAdder setName(String name) {
        this.name = name;
        return this;
    }
}
