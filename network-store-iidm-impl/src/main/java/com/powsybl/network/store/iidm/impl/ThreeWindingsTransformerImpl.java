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
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition.Feeder;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.iidm.impl.extensions.BranchStatusImpl;
import com.powsybl.network.store.iidm.impl.extensions.CgmesTapChangersImpl;
import com.powsybl.network.store.iidm.impl.extensions.ThreeWindingsTransformerPhaseAngleClockImpl;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;

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

        private final ThreeWindingsTransformerImpl transformer;

        private final Function<ThreeWindingsTransformerAttributes, LegAttributes> legGetter;

        private final NetworkObjectIndex index;

        public LegImpl(ThreeWindingsTransformerImpl transformer, Function<ThreeWindingsTransformerAttributes, LegAttributes> legGetter, NetworkObjectIndex index) {
            this.transformer = transformer;
            this.legGetter = legGetter;
            this.index = index;
        }

        private LegAttributes getLegAttributes() {
            return legGetter.apply(transformer.getResource().getAttributes());
        }

        protected String getLegAttribute() {
            return String.format("leg%d", getLegAttributes().getLegNumber());
        }

        @Override
        public TerminalImpl<ThreeWindingsTransformerToInjectionAttributesAdapter> getTerminal() {
            var attributes = getLegAttributes();
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
            return getLegAttributes().getR();
        }

        @Override
        public Leg setR(double r) {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is invalid");
            }
            double oldValue = getLegAttributes().getR();
            if (r != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setR(r));
                index.notifyUpdate(transformer, getLegAttribute() + ".r", oldValue, r);
            }
            return this;
        }

        @Override
        public double getX() {
            return getLegAttributes().getX();
        }

        @Override
        public Leg setX(double x) {
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is invalid");
            }
            double oldValue = getLegAttributes().getX();
            if (x != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setX(x));
                index.notifyUpdate(transformer, getLegAttribute() + ".x", oldValue, x);
            }
            return this;
        }

        @Override
        public double getG() {
            return getLegAttributes().getG();
        }

        @Override
        public Leg setG(double g) {
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is invalid");
            }
            double oldValue = getLegAttributes().getG();
            if (g != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setG(g));
                index.notifyUpdate(transformer, getLegAttribute() + ".g", oldValue, g);
            }
            return this;
        }

        @Override
        public double getB() {
            return getLegAttributes().getB();
        }

        @Override
        public Leg setB(double b) {
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is invalid");
            }
            double oldValue = getLegAttributes().getB();
            if (b != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setB(b));
                index.notifyUpdate(transformer, getLegAttribute() + ".b", oldValue, b);
            }
            return this;
        }

        @Override
        public double getRatedU() {
            return getLegAttributes().getRatedU();
        }

        @Override
        public Leg setRatedU(double ratedU) {
            ValidationUtil.checkRatedU(this, ratedU, "");
            double oldValue = getLegAttributes().getRatedU();
            if (ratedU != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setRatedU(ratedU));
                index.notifyUpdate(transformer, "ratedU", oldValue, ratedU);
            }
            return this;
        }

        @Override
        public Optional<CurrentLimits> getCurrentLimits() {
            return Optional.ofNullable(getNullableCurrentLimits());
        }

        @Override
        public CurrentLimits getNullableCurrentLimits() {
            var attributes = getLegAttributes();
            return attributes.getCurrentLimitsAttributes() != null
                    ? new CurrentLimitsImpl(this, attributes.getCurrentLimitsAttributes())
                    : null;
        }

        @Override
        public Optional<ApparentPowerLimits> getApparentPowerLimits() {
            return Optional.ofNullable(getNullableApparentPowerLimits());
        }

        @Override
        public ApparentPowerLimits getNullableApparentPowerLimits() {
            var attributes = getLegAttributes();
            return attributes.getApparentPowerLimitsAttributes() != null
                    ? new ApparentPowerLimitsImpl(this, attributes.getApparentPowerLimitsAttributes())
                    : null;
        }

        @Override
        public Optional<ActivePowerLimits> getActivePowerLimits() {
            return Optional.ofNullable(getNullableActivePowerLimits());
        }

        @Override
        public ActivePowerLimits getNullableActivePowerLimits() {
            var attributes = getLegAttributes();
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
            return new PhaseTapChangerAdderImpl(this, index, attributes -> legGetter.apply((ThreeWindingsTransformerAttributes) attributes));
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(this, index, attributes -> legGetter.apply((ThreeWindingsTransformerAttributes) attributes));
        }

        @Override
        public PhaseTapChanger getPhaseTapChanger() {
            return getLegAttributes().getPhaseTapChangerAttributes() != null
                    ? new PhaseTapChangerImpl(this, index, attributes -> legGetter.apply((ThreeWindingsTransformerAttributes) attributes))
                    : null;
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return getLegAttributes().getRatioTapChangerAttributes() != null
                    ? new RatioTapChangerImpl(this, index, attributes -> legGetter.apply((ThreeWindingsTransformerAttributes) attributes))
                    : null;
        }

        @Override
        public void setCurrentLimits(Void side, LimitsAttributes currentLimitsAttributes) {
            getLegAttributes().setCurrentLimitsAttributes(currentLimitsAttributes);
            transformer.updateResource();
        }

        @Override
        public AbstractIdentifiableImpl getIdentifiable() {
            return transformer;
        }

        @Override
        public void setApparentPowerLimits(Void side, LimitsAttributes apparentPowerLimitsAttributes) {
            getLegAttributes().setApparentPowerLimitsAttributes(apparentPowerLimitsAttributes);
        }

        @Override
        public void setActivePowerLimits(Void side, LimitsAttributes activePowerLimitsAttributes) {
            getLegAttributes().setActivePowerLimitsAttributes(activePowerLimitsAttributes);
        }

        @Override
        public double getRatedS() {
            return getLegAttributes().getRatedS();
        }

        @Override
        public Leg setRatedS(double ratedS) {
            ValidationUtil.checkRatedS(this, ratedS);
            double oldValue = getLegAttributes().getRatedS();
            if (ratedS != oldValue) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setRatedS(ratedS));
                index.notifyUpdate(transformer, "ratedS", oldValue, ratedS);
            }
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "3 windings transformer leg" + getLegAttributes().getLegNumber() + " '" + transformer.getId() + "': ";
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
            return String.format("TapChanger%d", getLegAttributes().getLegNumber());
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

        leg1 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg1, index);
        leg2 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg2, index);
        leg3 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg3, index);

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
    public void remove() {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeThreeWindingsTransformer(resource.getId());
        index.notifyAfterRemoval(resource.getId());
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
                .label(feeder1.getName().orElse(null))
                .order(feeder1.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder1.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa2 = ConnectablePositionAttributes.builder()
                .label(feeder2.getName().orElse(null))
                .order(feeder2.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder2.getDirection().name()))
                .build();
        ConnectablePositionAttributes cpa3 = ConnectablePositionAttributes.builder()
                .label(feeder3.getName().orElse(null))
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
