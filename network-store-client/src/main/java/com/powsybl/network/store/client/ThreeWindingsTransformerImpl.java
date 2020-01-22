/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class ThreeWindingsTransformerImpl extends AbstractIdentifiableImpl<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> implements ThreeWindingsTransformer, CurrentLimitsOwner<ThreeWindingsTransformer.Side> {

    private final Terminal terminal1;

    private final Terminal terminal2;

    private final Terminal terminal3;

    static class LegImpl implements Leg {

        private final LegAttributes attributes;

        private final ThreeWindingsTransformerImpl transformer;

        public LegImpl(LegAttributes attributes, ThreeWindingsTransformerImpl transformer) {
            this.attributes = attributes;
            this.transformer = transformer;
        }

        @Override
        public Terminal getTerminal() {
            if (attributes.getLegNumber() == 1) {
                return transformer.terminal1;
            } else if (attributes.getLegNumber() == 2) {
                return transformer.terminal2;
            } else if (attributes.getLegNumber() == 3) {
                return transformer.terminal3;
            } else {
                throw new AssertionError();
            }
        }

        @Override
        public double getR() {
            return attributes.getR();
        }

        @Override
        public Leg setR(double r) {
            attributes.setR(r);
            return this;
        }

        @Override
        public double getX() {
            return attributes.getX();
        }

        @Override
        public Leg setX(double x) {
            attributes.setX(x);
            return this;
        }

        @Override
        public double getG() {
            return attributes.getG();
        }

        @Override
        public Leg setG(double g) {
            attributes.setG(g);
            return this;
        }

        @Override
        public double getB() {
            return attributes.getB();
        }

        @Override
        public Leg setB(double b) {
            attributes.setB(b);
            return this;
        }

        @Override
        public double getRatedU() {
            return attributes.getRatedU();
        }

        @Override
        public Leg setRatedU(double ratedU) {
            attributes.setRatedU(ratedU);
            return this;
        }

        @Override
        public CurrentLimits getCurrentLimits() {
            return null;
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            return null;
        }

        @Override
        public PhaseTapChangerAdder newPhaseTapChanger() {
            return null;
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return null;
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return null;
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return null;
        }
    }

    ThreeWindingsTransformerImpl(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        super(index, resource);

        terminal1 = TerminalNodeBreakerImpl.create(index, resource, attributes -> new InjectionAttributes() {

            @Override
            public String getVoltageLevelId() {
                return attributes.getVoltageLevelId1();
            }

            @Override
            public int getNode() {
                return attributes.getNode1();
            }

            @Override
            public double getP() {
                return attributes.getP1();
            }

            @Override
            public void setP(double p) {
                attributes.setP1(p);
            }

            @Override
            public double getQ() {
                return attributes.getQ1();
            }

            @Override
            public void setQ(double q) {
                attributes.setQ1(q);
            }

            @Override
            public ConnectablePositionAttributes getPosition() {
                return attributes.getPosition1();
            }

            @Override
            public void setPosition(ConnectablePositionAttributes position) {
                attributes.setPosition1(position);
            }

            @Override
            public String getName() {
                return attributes.getName();
            }

            @Override
            public Map<String, String> getProperties() {
                return attributes.getProperties();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                attributes.setProperties(properties);
            }
        }, this);

        terminal2 = TerminalNodeBreakerImpl.create(index, resource, attributes -> new InjectionAttributes() {

            @Override
            public String getVoltageLevelId() {
                return attributes.getVoltageLevelId2();
            }

            @Override
            public int getNode() {
                return attributes.getNode2();
            }

            @Override
            public double getP() {
                return attributes.getP2();
            }

            @Override
            public void setP(double p) {
                attributes.setP2(p);
            }

            @Override
            public double getQ() {
                return attributes.getQ2();
            }

            @Override
            public void setQ(double q) {
                attributes.setQ2(q);
            }

            @Override
            public ConnectablePositionAttributes getPosition() {
                return attributes.getPosition2();
            }

            @Override
            public void setPosition(ConnectablePositionAttributes position) {
                attributes.setPosition2(position);
            }

            @Override
            public String getName() {
                return attributes.getName();
            }

            @Override
            public Map<String, String> getProperties() {
                return attributes.getProperties();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                attributes.setProperties(properties);
            }
        }, this);

        terminal3 = TerminalNodeBreakerImpl.create(index, resource, attributes -> new InjectionAttributes() {

            @Override
            public String getVoltageLevelId() {
                return attributes.getVoltageLevelId3();
            }

            @Override
            public int getNode() {
                return attributes.getNode3();
            }

            @Override
            public double getP() {
                return attributes.getP3();
            }

            @Override
            public void setP(double p) {
                attributes.setP3(p);
            }

            @Override
            public double getQ() {
                return attributes.getQ3();
            }

            @Override
            public void setQ(double q) {
                attributes.setQ3(q);
            }

            @Override
            public ConnectablePositionAttributes getPosition() {
                return attributes.getPosition3();
            }

            @Override
            public void setPosition(ConnectablePositionAttributes position) {
                attributes.setPosition3(position);
            }

            @Override
            public String getName() {
                return attributes.getName();
            }

            @Override
            public Map<String, String> getProperties() {
                return attributes.getProperties();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                attributes.setProperties(properties);
            }
        }, this);
    }

    static ThreeWindingsTransformerImpl create(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        return new ThreeWindingsTransformerImpl(index, resource);
    }

    @Override
    public void setCurrentLimits(ThreeWindingsTransformer.Side side, CurrentLimitsAttributes currentLimits) {

    }

    @Override
    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return getLeg1().getTerminal();

            case TWO:
                return getLeg2().getTerminal();

            case THREE:
                return getLeg3().getTerminal();

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Side getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (getLeg1().getTerminal() == terminal) {
            return Side.ONE;
        } else if (getLeg2().getTerminal() == terminal) {
            return Side.TWO;
        } else if (getLeg3().getTerminal() == terminal) {
            return Side.THREE;
        } else {
            throw new AssertionError("The terminal is not connected to this three windings transformer");
        }
    }

    @Override
    public Substation getSubstation() {
        return getLeg1().getTerminal().getVoltageLevel().getSubstation();
    }

    @Override
    public Leg getLeg1() {
        return new LegImpl(resource.getAttributes().getLeg1(), this);
    }

    @Override
    public Leg getLeg2() {
        return new LegImpl(resource.getAttributes().getLeg2(), this);
    }

    @Override
    public Leg getLeg3() {
        return new LegImpl(resource.getAttributes().getLeg3(), this);
    }

    @Override
    public double getRatedU0() {
        return resource.getAttributes().getRatedU0();
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.THREE_WINDINGS_TRANSFORMER;
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return null;
    }

    @Override
    public void remove() {

    }
}
