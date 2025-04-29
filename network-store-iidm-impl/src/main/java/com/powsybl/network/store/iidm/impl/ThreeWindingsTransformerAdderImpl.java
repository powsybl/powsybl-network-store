/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.LegAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
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

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = 0;

        private double b = 0;

        private double ratedU = Double.NaN;

        private double ratedS = Double.NaN;

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
            if (connectableBus == null && bus != null) {
                connectableBus = bus;
            }
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
        public LegAdder setRatedS(double ratedS) {
            this.ratedS = ratedS;
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
            ValidationUtil.checkRatedS(this, ratedS);
        }

        private String getConnectionBus() {
            if (bus != null) {
                if (connectableBus != null && !bus.equals(connectableBus)) {
                    throw new ValidationException(this, "connection bus leg " + legNumber + " is different to connectable bus");
                }
                return bus;
            } else {
                return connectableBus;
            }
        }

        protected void checkNodeBus() {
            String connectionBus = getConnectionBus();
            if (node != null && connectionBus != null) {
                throw new ValidationException(this, "connection node and connection bus leg " + legNumber + " are exclusives");
            }

            if (node == null && connectionBus == null) {
                throw new ValidationException(this, "connectable bus is not set");
            }

            if (connectionBus != null && index.getConfiguredBus(connectionBus).isEmpty()) {
                throw new ValidationException(this, "connectable bus leg " + legNumber + " '" + connectionBus + " not found");
            }

            VoltageLevel voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
            if (connectionBus != null) {
                checkBus(connectionBus, voltageLevel);
            } else {
                checkNode(node, voltageLevel);
            }
        }

        protected VoltageLevel checkVoltageLevel() {
            if (voltageLevelId == null) {
                String defaultVoltageLevelId = checkAndGetDefaultVoltageLevelId(connectableBus);
                if (defaultVoltageLevelId == null) {
                    throw new ValidationException(this, "voltage level is not set and has no default value");
                } else {
                    voltageLevelId = defaultVoltageLevelId;
                }
            }
            VoltageLevel voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                throw new ValidationException(this, "voltage level '" + voltageLevelId + "' not found");
            }
            return voltageLevel;
        }

        @Override
        public ThreeWindingsTransformerAdder add() {
            checkParams();
            switch (legNumber) {
                case 1 -> legAdder1 = this;
                case 2 -> legAdder2 = this;
                case 3 -> legAdder3 = this;
                default -> throw new IllegalStateException("Unexpected side: " + legNumber);
            }
            return threeWindingsTransformerAdder;
        }

        @Override
        public String getMessageHeader() {
            return String.format("3 windings transformer leg%d in substation %s: ", legNumber, substation.getName());
        }

        protected LegAttributes toLegAttributes() {
            return LegAttributes.builder()
                .voltageLevelId(voltageLevelId)
                .node(node)
                .bus(bus)
                .connectableBus(connectableBus != null ? connectableBus : bus)
                .r(r)
                .x(x)
                .g(g)
                .b(b)
                .ratedU(ratedU)
                .ratedS(ratedS)
                .legNumber(legNumber)
                .build();
        }
    }

    private double ratedU0;

    private LegAdderImpl legAdder1;
    private LegAdderImpl legAdder2;
    private LegAdderImpl legAdder3;

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
        LegAdderImpl legAdder = new LegAdderImpl(2, this);
        legAdder.g = 0.0;
        legAdder.b = 0.0;
        return legAdder;
    }

    @Override
    public LegAdder newLeg3() {
        LegAdderImpl legAdder = new LegAdderImpl(3, this);
        legAdder.g = 0.0;
        legAdder.b = 0.0;
        return legAdder;
    }

    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        this.ratedU0 = ratedU0;
        return this;
    }

    @Override
    public ThreeWindingsTransformer add() {
        String id = checkAndGetUniqueId();

        // Leg 1
        if (legAdder1 == null) {
            throw new ValidationException(this, "Leg1 is not set");
        }
        VoltageLevel voltageLevel1 = legAdder1.checkVoltageLevel();
        legAdder1.checkNodeBus();
        LegAttributes leg1 = legAdder1.toLegAttributes();

        // Leg 2
        if (legAdder2 == null) {
            throw new ValidationException(this, "Leg2 is not set");
        }
        VoltageLevel voltageLevel2 = legAdder2.checkVoltageLevel();
        legAdder2.checkNodeBus();
        LegAttributes leg2 = legAdder2.toLegAttributes();

        // Leg 3
        if (legAdder3 == null) {
            throw new ValidationException(this, "Leg3 is not set");
        }
        VoltageLevel voltageLevel3 = legAdder3.checkVoltageLevel();
        legAdder3.checkNodeBus();
        LegAttributes leg3 = legAdder3.toLegAttributes();

        if (substation != null) {
            if (voltageLevel1.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel2.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel3.getSubstation().map(s -> s != substation).orElse(true)) {
                throw new ValidationException(this,
                        "the 3 windings of the transformer shall belong to the substation '"
                                + substation.getId() + "' ('" + voltageLevel1.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel2.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel3.getSubstation().map(Substation::getId).orElse("null") + "')");
            }
        } else if (voltageLevel1.getSubstation().isPresent() || voltageLevel2.getSubstation().isPresent() || voltageLevel3.getSubstation().isPresent()) {
            throw new ValidationException(this,
                    "the 3 windings of the transformer shall belong to a substation since there are located in voltage levels with substations ('"
                            + voltageLevel1.getId() + "', '" + voltageLevel2.getId() + "', '" + voltageLevel3.getId() + "')");
        }

        // Define ratedU0 equal to ratedU1 if it has not been defined
        if (Double.isNaN(ratedU0)) {
            ratedU0 = leg1.getRatedU();
            LOGGER.info("RatedU0 is not set. Fixed to leg1 ratedU: {}", leg1.getRatedU());
        }

        Resource<ThreeWindingsTransformerAttributes> resource = Resource.threeWindingsTransformerBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name(getName())
                        .fictitious(isFictitious())
                        .ratedU0(ratedU0)
                        .leg1(leg1)
                        .leg2(leg2)
                        .leg3(leg3)
                        .build())
                .build();
        ThreeWindingsTransformerImpl transformer = getIndex().createThreeWindingsTransformer(resource);
        transformer.getLeg1().getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        transformer.getLeg2().getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        transformer.getLeg3().getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return transformer;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.THREE_WINDINGS_TRANSFORMER.getDescription();
    }
}
