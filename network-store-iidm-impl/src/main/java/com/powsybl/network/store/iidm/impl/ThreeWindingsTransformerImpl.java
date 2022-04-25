/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.iidm.impl.extensions.BranchStatusImpl;
import com.powsybl.network.store.iidm.impl.extensions.CgmesTapChangersImpl;
import com.powsybl.network.store.iidm.impl.extensions.ThreeWindingsTransformerPhaseAngleClockImpl;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition.Feeder;

import java.util.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class ThreeWindingsTransformerImpl extends AbstractIdentifiableImpl<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> implements ThreeWindingsTransformer, ConnectablePositionCreator<ThreeWindingsTransformer> {

    private final TerminalImpl<ThreeWindingsTransformerToInjectionAttributesAdapter> terminal1;

    private final TerminalImpl<ThreeWindingsTransformerToInjectionAttributesAdapter> terminal2;

    private final TerminalImpl<ThreeWindingsTransformerToInjectionAttributesAdapter> terminal3;

    private final LegImpl leg1;

    private final LegImpl leg2;

    private final LegImpl leg3;

    private ConnectablePositionImpl<ThreeWindingsTransformer> connectablePositionExtension;

    private BranchStatus<ThreeWindingsTransformer> branchStatusExtension;

    static class LegImpl implements Leg, LimitsOwner<Void>, TapChangerParent {

        private final LegAttributes attributes;

        private final ThreeWindingsTransformerImpl transformer;

        private final NetworkObjectIndex index;

        public LegImpl(NetworkObjectIndex index, LegAttributes attributes, ThreeWindingsTransformerImpl transformer) {
            this.attributes = attributes;
            this.transformer = transformer;
            this.index = index;
        }

        protected String getLegAttribute() {
            return String.format("leg%d", attributes.getLegNumber());
        }

        @Override
        public TerminalImpl<ThreeWindingsTransformerToInjectionAttributesAdapter> getTerminal() {
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
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is invalid");
            }
            double oldValue = attributes.getR();
            attributes.setR(r);
            transformer.updateResource();
            index.notifyUpdate(transformer, getLegAttribute() + ".r", oldValue, r);
            return this;
        }

        @Override
        public double getX() {
            return attributes.getX();
        }

        @Override
        public Leg setX(double x) {
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is invalid");
            }
            double oldValue = attributes.getX();
            attributes.setX(x);
            transformer.updateResource();
            index.notifyUpdate(transformer, getLegAttribute() + ".x", oldValue, x);
            return this;
        }

        @Override
        public double getG() {
            return attributes.getG();
        }

        @Override
        public Leg setG(double g) {
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is invalid");
            }
            double oldValue = attributes.getG();
            attributes.setG(g);
            transformer.updateResource();
            index.notifyUpdate(transformer, getLegAttribute() + ".g", oldValue, g);
            return this;
        }

        @Override
        public double getB() {
            return attributes.getB();
        }

        @Override
        public Leg setB(double b) {
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is invalid");
            }
            double oldValue = attributes.getB();
            attributes.setB(b);
            transformer.updateResource();
            index.notifyUpdate(transformer, getLegAttribute() + ".b", oldValue, b);
            return this;
        }

        @Override
        public double getRatedU() {
            return attributes.getRatedU();
        }

        @Override
        public Leg setRatedU(double ratedU) {
            ValidationUtil.checkRatedU(this, ratedU, "");
            double oldValue = attributes.getRatedU();
            attributes.setRatedU(ratedU);
            transformer.updateResource();
            index.notifyUpdate(transformer, "ratedU", oldValue, ratedU);
            return this;
        }

        @Override
        public CurrentLimits getCurrentLimits() {
            return attributes.getCurrentLimitsAttributes() != null
                    ? new CurrentLimitsImpl(this, attributes.getCurrentLimitsAttributes())
                    : null;
        }

        @Override
        public ApparentPowerLimits getApparentPowerLimits() {
            return attributes.getApparentPowerLimitsAttributes() != null
                    ? new ApparentPowerLimitsImpl(this, attributes.getApparentPowerLimitsAttributes())
                    : null;
        }

        @Override
        public ActivePowerLimits getActivePowerLimits() {
            return attributes.getActivePowerLimitsAttributes() != null
                    ? new ActivePowerLimitsImpl(this, attributes.getActivePowerLimitsAttributes())
                    : null;
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            return new CurrentLimitsAdderImpl<>(null, this);
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            return new ApparentPowerLimitsAdderImpl<>(null, this);
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            return new ActivePowerLimitsAdderImpl<>(null, this);
        }

        @Override
        public PhaseTapChangerAdder newPhaseTapChanger() {
            return new PhaseTapChangerAdderImpl(this, index, attributes, transformer.getId());
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(this, index, attributes, transformer.getId());
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return attributes.getPhaseTapChangerAttributes() != null ? new PhaseTapChangerImpl(this, index, attributes.getPhaseTapChangerAttributes()) : null;
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return attributes.getRatioTapChangerAttributes() != null ? new RatioTapChangerImpl(this, index, attributes.getRatioTapChangerAttributes()) : null;
        }

        @Override
        public void setCurrentLimits(Void side, LimitsAttributes currentLimitsAttributes) {
            this.attributes.setCurrentLimitsAttributes(currentLimitsAttributes);
            transformer.updateResource();
        }

        @Override
        public AbstractIdentifiableImpl getIdentifiable() {
            return transformer;
        }

        @Override
        public void setApparentPowerLimits(Void side, LimitsAttributes apparentPowerLimitsAttributes) {
            this.attributes.setApparentPowerLimitsAttributes(apparentPowerLimitsAttributes);
        }

        @Override
        public void setActivePowerLimits(Void side, LimitsAttributes activePowerLimitsAttributes) {
            this.attributes.setActivePowerLimitsAttributes(activePowerLimitsAttributes);
        }

        @Override
        public double getRatedS() {
            return attributes.getRatedS();
        }

        @Override
        public Leg setRatedS(double ratedS) {
            ValidationUtil.checkRatedS(this, ratedS);
            double oldValue = attributes.getRatedS();
            attributes.setRatedS(ratedS);
            transformer.updateResource();
            index.notifyUpdate(transformer, "ratedS", oldValue, ratedS);
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "3 windings transformer leg" + attributes.getLegNumber() + " '" + transformer.getId() + "': ";
        }

        @Override
        public NetworkImpl getNetwork() {
            return transformer.getNetwork();
        }

        @Override
        public ThreeWindingsTransformerImpl getTransformer() {
            return transformer;
        }

        @Override
        public String getTapChangerAttribute() {
            return String.format("TapChanger%d", attributes.getLegNumber());
        }

        @Override
        public Set<TapChanger<?, ?>> getAllTapChangers() {
            Set<TapChanger<?, ?>> tapChangers = new HashSet<>();
            transformer.leg1.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg1.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            return tapChangers;
        }

        public void notifyUpdate(String attribute, Object oldValue, Object newValue) {
            index.notifyUpdate(transformer, getLegAttribute() + "." + attribute, oldValue, newValue);
        }
    }

    ThreeWindingsTransformerImpl(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        super(index, resource);

        leg1 = new LegImpl(index, resource.getAttributes().getLeg1(), this);
        leg2 = new LegImpl(index, resource.getAttributes().getLeg2(), this);
        leg3 = new LegImpl(index, resource.getAttributes().getLeg3(), this);

        terminal1 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(leg1, resource.getAttributes(), Side.ONE), this);
        terminal2 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(leg2, resource.getAttributes(), Side.TWO), this);
        terminal3 = TerminalImpl.create(index, new ThreeWindingsTransformerToInjectionAttributesAdapter(leg3, resource.getAttributes(), Side.THREE), this);

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
    public Optional<Substation> getSubstation() {
        return getLeg1().getTerminal().getVoltageLevel().getSubstation();
    }

    @Override
    public LegImpl getLeg1() {
        return leg1;
    }

    @Override
    public LegImpl getLeg2() {
        return leg2;
    }

    @Override
    public LegImpl getLeg3() {
        return leg3;
    }

    @Override
    public double getRatedU0() {
        return checkResource().getAttributes().getRatedU0();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2, terminal3);
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeThreeWindingsTransformer(resource.getId());
        index.getVoltageLevel(leg1.getTerminal().getVoltageLevelId()).get().invalidateCalculatedBuses();
        index.getVoltageLevel(leg2.getTerminal().getVoltageLevelId()).get().invalidateCalculatedBuses();
        index.getVoltageLevel(leg3.getTerminal().getVoltageLevelId()).get().invalidateCalculatedBuses();
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            leg1.getTerminal().removeDanglingSwitches();
            leg2.getTerminal().removeDanglingSwitches();
            leg3.getTerminal().removeDanglingSwitches();
        }
    }

    public BranchStatus.Status getBranchStatus() {
        return BranchStatus.Status.valueOf(checkResource().getAttributes().getBranchStatus());
    }

    public ThreeWindingsTransformer setBranchStatus(BranchStatus.Status branchStatus) {
        Objects.requireNonNull(branchStatus);
        checkResource().getAttributes().setBranchStatus(branchStatus.name());
        updateResource();
        return this;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<ThreeWindingsTransformer>) extension;
            resource.getAttributes().setPosition1(connectablePositionExtension.getFeeder1().getConnectablePositionAttributes());
            resource.getAttributes().setPosition2(connectablePositionExtension.getFeeder2().getConnectablePositionAttributes());
            resource.getAttributes().setPosition3(connectablePositionExtension.getFeeder3().getConnectablePositionAttributes());
            updateResource();
        } else if (type == ThreeWindingsTransformerPhaseAngleClock.class) {
            resource.getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg2(((ThreeWindingsTransformerPhaseAngleClock) extension).getPhaseAngleClockLeg2());
            resource.getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg3(((ThreeWindingsTransformerPhaseAngleClock) extension).getPhaseAngleClockLeg3());
            updateResource();
        } else if (type == BranchStatus.class) {
            BranchStatus branchStatus = (BranchStatus) extension;
            setBranchStatus(branchStatus.getStatus());
        } else if (type == CgmesTapChangers.class) {
            resource.getAttributes().setCgmesTapChangerAttributesList(new ArrayList<>());
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
                .order(feeder1.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder1.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa2 = ConnectablePositionAttributes.builder()
                .label(feeder2.getName())
                .order(feeder2.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder2.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa3 = ConnectablePositionAttributes.builder()
                .label(feeder3.getName())
                .order(feeder3.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder3.getDirection().name()))
                .build();
        return new ConnectablePositionImpl<>(this,
                null,
                new ConnectablePositionImpl.FeederImpl(cpa1),
                new ConnectablePositionImpl.FeederImpl(cpa2),
                new ConnectablePositionImpl.FeederImpl(cpa3)
        );
    }

    private <E extends Extension<ThreeWindingsTransformer>> E createBranchStatusExtension() {
        E extension = null;
        var resource = checkResource();
        String branchStatus = resource.getAttributes().getBranchStatus();
        if (branchStatus != null) {
            extension = (E) new BranchStatusImpl(this, BranchStatus.Status.valueOf(branchStatus));
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = (E) connectablePositionExtension;
        } else if (type == ThreeWindingsTransformerPhaseAngleClock.class) {
            extension = createPhaseAngleClock();
        } else if (type == BranchStatus.class) {
            extension = createBranchStatusExtension();
        } else if (type == CgmesTapChangers.class) {
            extension = createCgmesTapChangers();
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtensionByName(String name) {
        E extension;
        if (name.equals(ConnectablePosition.NAME)) {
            extension = (E) connectablePositionExtension;
        } else if (name.equals(ThreeWindingsTransformerPhaseAngleClock.NAME)) {
            extension = createPhaseAngleClock();
        } else if (name.equals(BranchStatus.NAME)) {
            extension = createBranchStatusExtension();
        } else if (name.equals(CgmesTapChangers.NAME)) {
            extension = createCgmesTapChangers();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> Collection<E> getExtensions() {
        Collection<E> superExtensions = super.getExtensions();
        Collection<E> result = new ArrayList<>(superExtensions);
        if (connectablePositionExtension != null) {
            result.add((E) connectablePositionExtension);
        }
        E extension = createPhaseAngleClock();
        if (extension != null) {
            result.add(extension);
        }
        extension = createBranchStatusExtension();
        if (extension != null) {
            result.add(extension);
        }
        extension = createCgmesTapChangers();
        if (extension != null) {
            result.add(extension);
        }
        return result;
    }

    private <E extends Extension<ThreeWindingsTransformer>> E createPhaseAngleClock() {
        E extension = null;
        var resource = checkResource();
        ThreeWindingsTransformerPhaseAngleClockAttributes phaseAngleClock = resource.getAttributes().getPhaseAngleClock();
        if (phaseAngleClock != null) {
            extension = (E) new ThreeWindingsTransformerPhaseAngleClockImpl(this, phaseAngleClock.getPhaseAngleClockLeg2(), phaseAngleClock.getPhaseAngleClockLeg3());
        }
        return extension;
    }

    public ThreeWindingsTransformerImpl initPhaseAngleClockAttributes(int phaseAngleClockLeg2, int phaseAngleClockLeg3) {
        checkResource().getAttributes().setPhaseAngleClock(new ThreeWindingsTransformerPhaseAngleClockAttributes(phaseAngleClockLeg2, phaseAngleClockLeg3));
        updateResource();
        return this;
    }

    private <E extends Extension<ThreeWindingsTransformer>> E createCgmesTapChangers() {
        E extension = null;
        var resource = checkResource();
        if (resource.getAttributes().getCgmesTapChangerAttributesList() != null) {
            extension = (E) new CgmesTapChangersImpl(this);
        }
        return extension;
    }
}
