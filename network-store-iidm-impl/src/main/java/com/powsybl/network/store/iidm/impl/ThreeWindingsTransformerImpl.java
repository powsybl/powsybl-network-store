/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition.Feeder;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class ThreeWindingsTransformerImpl extends AbstractIdentifiableImpl<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> implements ThreeWindingsTransformer, ConnectablePositionCreator<ThreeWindingsTransformer> {

    private final Terminal terminal1;

    private final Terminal terminal2;

    private final Terminal terminal3;

    private final Leg leg1;

    private final Leg leg2;

    private final Leg leg3;

    private ConnectablePositionImpl<ThreeWindingsTransformer> connectablePositionExtension;

    static class LegImpl implements Leg, CurrentLimitsOwner<Void> {

        private final LegAttributes attributes;

        private final ThreeWindingsTransformerImpl transformer;

        private final NetworkObjectIndex index;

        public LegImpl(NetworkObjectIndex index, LegAttributes attributes, ThreeWindingsTransformerImpl transformer) {
            this.attributes = attributes;
            this.transformer = transformer;
            this.index = index;
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
            return attributes.getCurrentLimitsAttributes() != null
                    ? new CurrentLimitsImpl(attributes.getCurrentLimitsAttributes())
                    : null;
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            return new CurrentLimitsAdderImpl<>(null, this);
        }

        @Override
        public PhaseTapChangerAdder newPhaseTapChanger() {
            return new PhaseTapChangerAdderImpl(index, attributes, transformer.getId());
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(index, attributes, transformer.getId());
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return attributes.getPhaseTapChangerAttributes() != null ? new PhaseTapChangerImpl(index, attributes.getPhaseTapChangerAttributes()) : null;
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return attributes.getRatioTapChangerAttributes() != null ? new RatioTapChangerImpl(index, attributes.getRatioTapChangerAttributes()) : null;
        }

        @Override
        public void setCurrentLimits(Void side, CurrentLimitsAttributes currentLimitsAttributes) {
            this.attributes.setCurrentLimitsAttributes(currentLimitsAttributes);
        }

        @Override
        public double getRatedS() {
            return attributes.getRatedS();
        }

        @Override
        public Leg setRatedS(double ratedS) {
            ValidationUtil.checkRatedS(this, ratedS);
            attributes.setRatedS(ratedS);
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "leg" + attributes.getLegNumber() + " '" + transformer.getId() + "': ";
        }
    }

    ThreeWindingsTransformerImpl(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        super(index, resource);

        leg1 = new LegImpl(index, resource.getAttributes().getLeg1(), this);
        leg2 = new LegImpl(index, resource.getAttributes().getLeg2(), this);
        leg3 = new LegImpl(index, resource.getAttributes().getLeg3(), this);

        terminal1 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.ONE), this);
        terminal2 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.TWO), this);
        terminal3 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.THREE), this);

        ConnectablePositionAttributes cpa1 = resource.getAttributes().getPosition1();
        ConnectablePositionAttributes cpa2 = resource.getAttributes().getPosition2();
        ConnectablePositionAttributes cpa3 = resource.getAttributes().getPosition3();
        if (cpa1 != null && cpa2 != null && cpa3 != null) {
            connectablePositionExtension = new ConnectablePositionImpl<>(this, null,
                    new ConnectablePositionImpl.FeederImpl(cpa1),
                    new ConnectablePositionImpl.FeederImpl(cpa2),
                    new ConnectablePositionImpl.FeederImpl(cpa3));
        }
    }

    static ThreeWindingsTransformerImpl create(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        return new ThreeWindingsTransformerImpl(index, resource);
    }

    @Override
    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return leg1.getTerminal();

            case TWO:
                return leg2.getTerminal();

            case THREE:
                return leg3.getTerminal();

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Side getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (leg1.getTerminal() == terminal) {
            return Side.ONE;
        } else if (leg2.getTerminal() == terminal) {
            return Side.TWO;
        } else if (leg3.getTerminal() == terminal) {
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
        return leg1;
    }

    @Override
    public Leg getLeg2() {
        return leg2;
    }

    @Override
    public Leg getLeg3() {
        return leg3;
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
        return Arrays.asList(terminal1, terminal2, terminal3);
    }

    @Override
    public void remove() {
        index.removeThreeWindingsTransformer(resource.getId());
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<ThreeWindingsTransformer>) extension;
            resource.getAttributes().setPosition1(connectablePositionExtension.getFeeder1().getConnectablePositionAttributes());
            resource.getAttributes().setPosition2(connectablePositionExtension.getFeeder2().getConnectablePositionAttributes());
            resource.getAttributes().setPosition3(connectablePositionExtension.getFeeder3().getConnectablePositionAttributes());
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public ConnectablePositionImpl<ThreeWindingsTransformer> createConnectablePositionExtension(Feeder feeder,
            Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        Objects.requireNonNull(feeder3);
        ConnectablePosition.check(feeder, feeder1, feeder2, feeder3);
        ConnectablePositionAttributes cpa1 = ConnectablePositionAttributes.builder()
                .label(feeder1.getName())
                .order(feeder1.getOrder())
                .direction(ConnectableDirection.valueOf(feeder1.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa2 = ConnectablePositionAttributes.builder()
                .label(feeder2.getName())
                .order(feeder2.getOrder())
                .direction(ConnectableDirection.valueOf(feeder2.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa3 = ConnectablePositionAttributes.builder()
                .label(feeder3.getName())
                .order(feeder3.getOrder())
                .direction(ConnectableDirection.valueOf(feeder3.getDirection().name()))
                .build();
        return new ConnectablePositionImpl<>(this,
                null,
                new ConnectablePositionImpl.FeederImpl(cpa1),
                new ConnectablePositionImpl.FeederImpl(cpa2),
                new ConnectablePositionImpl.FeederImpl(cpa3)
                );
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = (E) connectablePositionExtension;
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = (E) connectablePositionExtension;
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> Collection<E> getExtensions() {
        Collection<E> superExtensions = super.getExtensions();
        Collection<E> result;
        if (connectablePositionExtension != null) {
            result = new ArrayList<>();
            result.addAll(superExtensions);
            result.add((E) connectablePositionExtension);
        } else {
            result = superExtensions;
        }
        return result;
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }
}
