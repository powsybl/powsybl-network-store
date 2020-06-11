/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.LegAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ThreeWindingsTransformerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class ThreeWindingsTransformerAdderImpl extends AbstractIdentifiableAdder<ThreeWindingsTransformerAdderImpl> implements ThreeWindingsTransformerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeWindingsTransformerAdderImpl.class);

    private SubstationImpl substation;

    private class LegAdderImpl implements LegAdder, Validable {

        private ThreeWindingsTransformerAdder threeWindingsTransformerAdder;

        private String voltageLevelId;

        private Integer node;

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

        protected void checkParams() {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is not set");
            }
            ValidationUtil.checkRatedU(this, ratedU, "");
        }

        private String getConnectionBus() {
            if (bus != null) {
                if ((connectableBus != null) && (!bus.equals(connectableBus))) {
                    throw new ValidationException(this, "connection bus is different to connectable bus");
                }
                return bus;
            } else {
                return connectableBus;
            }
        }

        protected void checkNodeBus() {
            String connectionBus = getConnectionBus();
            if (node != null && connectionBus != null) {
                throw new ValidationException(this, "connection node and connection bus are exclusives");
            }

            if (node == null && connectionBus == null) {
                throw new ValidationException(this, "connectable bus is not set");
            }
        }

        protected VoltageLevel checkVoltageLevel() {
            if (voltageLevelId == null) {
                throw new ValidationException(this, "voltage level is not set");
            }
            VoltageLevel voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                throw new ValidationException(this, "voltage level '" + voltageLevelId + "' not found");
            }
            if (voltageLevel.getSubstation() != substation) {
                throw new ValidationException(this, "voltage level shall belong to the substation '" + substation.getId() + "'");
            }
            return voltageLevel;
        }

        @Override
        public ThreeWindingsTransformerAdder add() {
            checkParams();
            checkNodeBus();
            checkVoltageLevel();

            if (legNumber == 1) {
                leg1 = LegAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ratedU(ratedU)
                        .legNumber(legNumber)
                        .build();
            } else if (legNumber == 2) {
                leg2 = LegAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ratedU(ratedU)
                        .legNumber(legNumber)
                        .build();
            } else if (legNumber == 3) {
                leg3 = LegAttributes.builder()
                        .voltageLevelId(voltageLevelId)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
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

        @Override
        public String getMessageHeader() {
            return String.format("3 windings transformer leg%d: ", legNumber);
        }
    }

    private double ratedU0;

    private LegAttributes leg1;

    private LegAttributes leg2;

    private LegAttributes leg3;

    ThreeWindingsTransformerAdderImpl(NetworkObjectIndex index, SubstationImpl substation) {
        super(index);
        this.substation = substation;
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
        String id = checkAndGetUniqueId();

        if (leg1 == null) {
            throw new ValidationException(this, "Leg1 is not set");
        }
        if (leg2 == null) {
            throw new ValidationException(this, "Leg2 is not set");
        }
        if (leg3 == null) {
            throw new ValidationException(this, "Leg3 is not set");
        }

        // Define ratedU0 equal to ratedU1 if it has not been defined
        if (Double.isNaN(ratedU0)) {
            ratedU0 = leg1.getRatedU();
            LOGGER.info("RatedU0 is not set. Fixed to leg1 ratedU: {}", leg1.getRatedU());
        }

        Resource<ThreeWindingsTransformerAttributes> resource = Resource.threeWindingsTransformerBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name(getName())
                        .ratedU0(ratedU0)
                        .leg1(leg1)
                        .leg2(leg2)
                        .leg3(leg3)
                        .build())
                .build();
        return getIndex().createThreeWindingsTransformer(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }
}
