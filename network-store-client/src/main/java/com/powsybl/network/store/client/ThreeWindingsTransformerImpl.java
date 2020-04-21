/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class ThreeWindingsTransformerImpl extends AbstractIdentifiableImpl<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> implements ThreeWindingsTransformer {

    private final Terminal terminal1;

    private final Terminal terminal2;

    private final Terminal terminal3;

    private final Leg leg1;

    private final Leg leg2;

    private final Leg leg3;

    static class LegImpl implements Leg, CurrentLimitsOwner<Void> {

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
            return new PhaseTapChangerAdderImpl(attributes, transformer.getId());
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(attributes, transformer.getId());
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return attributes.getPhaseTapChangerAttributes() != null ? new PhaseTapChangerImpl(attributes.getPhaseTapChangerAttributes()) : null;
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return attributes.getRatioTapChangerAttributes() != null ? new RatioTapChangerImpl(attributes.getRatioTapChangerAttributes()) : null;
        }

        @Override
        public void setCurrentLimits(Void side, CurrentLimitsAttributes currentLimitsAttributes) {
            this.attributes.setCurrentLimitsAttributes(currentLimitsAttributes);
        }

        @Override
        public double getRatedS() {
            return 100; // TODO
        }

        @Override
        public Leg setRatedS(double ratedS) {
            return this; // TODO
        }

        @Override
        public String getMessageHeader() {
            return "leg" + attributes.getLegNumber() + " '" + transformer.getId() + "': ";
        }
    }

    ThreeWindingsTransformerImpl(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        super(index, resource);

        leg1 = new LegImpl(resource.getAttributes().getLeg1(), this);
        leg2 = new LegImpl(resource.getAttributes().getLeg2(), this);
        leg3 = new LegImpl(resource.getAttributes().getLeg3(), this);

        terminal1 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.ONE), this);
        terminal2 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.TWO), this);
        terminal3 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(resource.getAttributes(), Side.THREE), this);
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

    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ConnectablePosition.class) {
            ConnectablePosition position = (ConnectablePosition) extension;
            resource.getAttributes().setPosition1(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder1().getName())
                    .order(position.getFeeder1().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder1().getDirection().name()))
                    .build());
            resource.getAttributes().setPosition2(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder2().getName())
                    .order(position.getFeeder2().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder2().getDirection().name()))
                    .build());
            resource.getAttributes().setPosition3(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder3().getName())
                    .order(position.getFeeder3().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder3().getDirection().name()))
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension<ThreeWindingsTransformer>> E createConnectablePositionExtension() {
        E extension = null;
        ConnectablePositionAttributes positionAttributes1 = resource.getAttributes().getPosition1();
        ConnectablePositionAttributes positionAttributes2 = resource.getAttributes().getPosition2();
        ConnectablePositionAttributes positionAttributes3 = resource.getAttributes().getPosition3();
        if (positionAttributes1 != null && positionAttributes2 != null && positionAttributes3 != null) {
            extension = (E) new ConnectablePosition<>(this,
                    null,
                    new ConnectablePosition.Feeder(positionAttributes1.getLabel(),
                            positionAttributes1.getOrder(),
                            ConnectablePosition.Direction.valueOf(positionAttributes1.getDirection().name())),
                    new ConnectablePosition.Feeder(positionAttributes2.getLabel(),
                            positionAttributes2.getOrder(),
                            ConnectablePosition.Direction.valueOf(positionAttributes2.getDirection().name())),
                    new ConnectablePosition.Feeder(positionAttributes3.getLabel(),
                            positionAttributes3.getOrder(),
                            ConnectablePosition.Direction.valueOf(positionAttributes3.getDirection().name()))
                    );
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }
}
