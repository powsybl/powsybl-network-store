/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.network.store.iidm.impl.extensions.CgmesTapChangersImpl;
import com.powsybl.network.store.iidm.impl.extensions.ThreeWindingsTransformerPhaseAngleClockImpl;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class ThreeWindingsTransformerImpl extends AbstractConnectableImpl<ThreeWindingsTransformer, ThreeWindingsTransformerAttributes> implements ThreeWindingsTransformer {

    private final TerminalImpl<ThreeWindingsTransformerAttributes> terminal1;

    private final TerminalImpl<ThreeWindingsTransformerAttributes> terminal2;

    private final TerminalImpl<ThreeWindingsTransformerAttributes> terminal3;

    private final LegImpl leg1;

    private final LegImpl leg2;

    private final LegImpl leg3;

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
        public TerminalImpl<ThreeWindingsTransformerAttributes> getTerminal() {
            var attributes = getLegAttributes();
            return switch (attributes.getLegNumber()) {
                case 1 -> transformer.terminal1;
                case 2 -> transformer.terminal2;
                case 3 -> transformer.terminal3;
                default -> throw new AssertionError();
            };
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setR(r),
                    getLegAttribute() + ".r", oldValue, r);
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setX(x),
                    getLegAttribute() + ".x", oldValue, x);
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setG(g),
                    getLegAttribute() + ".g", oldValue, g);
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setB(b),
                    getLegAttribute() + ".b", oldValue, b);
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setRatedU(ratedU),
                    "ratedU", oldValue, ratedU);
            }
            return this;
        }

        @Override
        public Optional<CurrentLimits> getCurrentLimits() {
            return Optional.ofNullable(getNullableCurrentLimits());
        }

        @Override
        public CurrentLimits getNullableCurrentLimits() {
            var operationalLimitsGroup = getLegAttributes().getSelectedOperationalLimitsGroup();
            return operationalLimitsGroup != null && operationalLimitsGroup.getCurrentLimits() != null
                    ? new CurrentLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getCurrentLimits())
                    : null;
        }

        @Override
        public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup() {
            OperationalLimitsGroupAttributes operationalLimitsGroup = getLegAttributes().getSelectedOperationalLimitsGroup();
            if (operationalLimitsGroup != null) {
                return new OperationalLimitsGroupImpl<>(this, null, operationalLimitsGroup);
            }
            OperationalLimitsGroup newOperationalLimitsGroup = newOperationalLimitsGroup(DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID);
            setSelectedOperationalLimitsGroup(DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID);
            return newOperationalLimitsGroup;
        }

        @Override
        public Optional<ApparentPowerLimits> getApparentPowerLimits() {
            return Optional.ofNullable(getNullableApparentPowerLimits());
        }

        @Override
        public ApparentPowerLimits getNullableApparentPowerLimits() {
            var operationalLimitsGroup = getLegAttributes().getSelectedOperationalLimitsGroup();
            return operationalLimitsGroup != null && operationalLimitsGroup.getApparentPowerLimits() != null
                    ? new ApparentPowerLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getApparentPowerLimits())
                    : null;
        }

        @Override
        public Optional<ActivePowerLimits> getActivePowerLimits() {
            return Optional.ofNullable(getNullableActivePowerLimits());
        }

        @Override
        public ActivePowerLimits getNullableActivePowerLimits() {
            var operationalLimitsGroup = getLegAttributes().getSelectedOperationalLimitsGroup();
            return operationalLimitsGroup != null && operationalLimitsGroup.getActivePowerLimits() != null
                    ? new ActivePowerLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getActivePowerLimits())
                    : null;
        }

        private static final String DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "DEFAULT";
        private static final String SELECTED_OPERATIONAL_LIMITS_GROUP_ID = ".selectedOperationalLimitsGroupId";

        private String getSelectedLimitsGroupId() {
            return getLegAttributes().getSelectedOperationalLimitsGroupId() != null
                    ? getLegAttributes().getSelectedOperationalLimitsGroupId()
                    : DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
        }

        private void updateSelectedOperationalLimitsGroupIdIfNull(String id) {
            if (getLegAttributes().getSelectedOperationalLimitsGroupId() == null) {
                getLegAttributes().setSelectedOperationalLimitsGroupId(id);
            }
        }

        @Deprecated(since = "1.29.0")
        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
            return getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
        }

        @Deprecated(since = "1.29.0")
        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
            return getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
        }

        @Deprecated(since = "1.29.0")
        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
            return getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
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
        public void setPhaseTapChanger(PhaseTapChangerAttributes attributes) {
            PhaseTapChangerAttributes oldValue = getLegAttributes().getPhaseTapChangerAttributes();
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setPhaseTapChangerAttributes(attributes),
                "phaseTapChanger", oldValue, attributes);
        }

        @Override
        public void setRatioTapChanger(RatioTapChangerAttributes attributes) {
            RatioTapChangerAttributes oldValue = getLegAttributes().getRatioTapChangerAttributes();
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setRatioTapChangerAttributes(attributes),
                "ratioTapChanger", oldValue, attributes);
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
        public void setCurrentLimits(Void side, LimitsAttributes currentLimits, String operationalLimitsGroupId) {
            var operationalLimitsGroup = getLegAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
            LimitsAttributes oldValue = operationalLimitsGroup != null ? operationalLimitsGroup.getCurrentLimits() : null;
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setCurrentLimits(currentLimits),
                getLegAttribute() + "." + "currentLimits", oldValue, currentLimits);
        }

        @Override
        public AbstractIdentifiableImpl<?, ?> getIdentifiable() {
            return transformer;
        }

        @Override
        public void setApparentPowerLimits(Void side, LimitsAttributes apparentPowerLimitsAttributes, String operationalLimitsGroupId) {
            var operationalLimitsGroup = getLegAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
            LimitsAttributes oldValue = operationalLimitsGroup != null ? operationalLimitsGroup.getApparentPowerLimits() : null;
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setApparentPowerLimits(apparentPowerLimitsAttributes),
                getLegAttribute() + "." + "apparentLimits", oldValue, apparentPowerLimitsAttributes);
        }

        @Override
        public void setActivePowerLimits(Void side, LimitsAttributes activePowerLimitsAttributes, String operationalLimitsGroupId) {
            var operationalLimitsGroup = getLegAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
            LimitsAttributes oldValue = operationalLimitsGroup != null ? operationalLimitsGroup.getActivePowerLimits() : null;
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setActivePowerLimits(activePowerLimitsAttributes),
                getLegAttribute() + "." + "activePowerLimits", oldValue, activePowerLimitsAttributes);
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
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setRatedS(ratedS),
                    "ratedS", oldValue, ratedS);
            }
            return this;
        }

        @Override
        public ThreeSides getSide() {
            return ThreeSides.valueOf(getLegAttributes().getLegNumber());
        }

        @Override
        public Optional<? extends LoadingLimits> getLimits(LimitType limitType) {
            return switch (limitType) {
                case CURRENT -> getCurrentLimits();
                case ACTIVE_POWER -> getActivePowerLimits();
                case APPARENT_POWER -> getApparentPowerLimits();
                default ->
                        throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", limitType.name()));
            };
        }

        @Override
        public MessageHeader getMessageHeader() {
            return new DefaultMessageHeader("3 windings transformer leg" + getLegAttributes().getLegNumber(), transformer.getId());
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
        public Set<TapChanger<?, ?, ?, ?>> getAllTapChangers() {
            Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>();
            transformer.leg1.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg1.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            return tapChangers;
        }

        @Override
        public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
            return getLegAttributes().getOperationalLimitsGroups().values().stream()
                    .map(group -> new OperationalLimitsGroupImpl<>(this, null, group))
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<String> getSelectedOperationalLimitsGroupId() {
            return Optional.ofNullable(getLegAttributes().getSelectedOperationalLimitsGroupId());
        }

        @Override
        public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
            return getOperationalLimitsGroups().stream()
                    .filter(group -> group.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
            return getSelectedOperationalLimitsGroupId().flatMap(this::getOperationalLimitsGroup);
        }

        @Override
        public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
            var resource = getLegAttributes();
            var group = OperationalLimitsGroupAttributes.builder().id(id).build();
            resource.getOperationalLimitsGroups().put(id, group);
            return new OperationalLimitsGroupImpl<>(this, null, group);
        }

        @Override
        public void setSelectedOperationalLimitsGroup(String id) {
            var resource = getLegAttributes();
            String oldValue = resource.getSelectedOperationalLimitsGroupId();
            if (!id.equals(oldValue)) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setSelectedOperationalLimitsGroupId(id),
                    getLegAttribute() + SELECTED_OPERATIONAL_LIMITS_GROUP_ID, oldValue, id);
            }
        }

        @Override
        public void removeOperationalLimitsGroup(String id) {
            var resource = getLegAttributes();
            if (resource.getOperationalLimitsGroups().get(id) == null) {
                throw new IllegalArgumentException("Operational limits group '" + id + "' does not exist");
            }
            OperationalLimitsGroup oldLimits = getOperationalLimitsGroup(id).orElse(null);
            if (id.equals(resource.getSelectedOperationalLimitsGroupId())) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setSelectedOperationalLimitsGroupId(null),
                    getLegAttribute() + SELECTED_OPERATIONAL_LIMITS_GROUP_ID, id, null);
            }
            transformer.updateResource(res -> legGetter.apply(res.getAttributes()).getOperationalLimitsGroups().remove(id),
                getLegAttribute() + ".operationalLimitsGroupId", oldLimits, null);
        }

        @Override
        public void cancelSelectedOperationalLimitsGroup() {
            var resource = getLegAttributes();
            String oldValue = resource.getSelectedOperationalLimitsGroupId();
            if (oldValue != null) {
                transformer.updateResource(res -> legGetter.apply(res.getAttributes()).setSelectedOperationalLimitsGroupId(null),
                    getLegAttribute() + SELECTED_OPERATIONAL_LIMITS_GROUP_ID, oldValue, null);
            }
        }
    }

    ThreeWindingsTransformerImpl(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        super(index, resource);

        leg1 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg1, index);
        leg2 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg2, index);
        leg3 = new LegImpl(this, ThreeWindingsTransformerAttributes::getLeg3, index);

        terminal1 = new TerminalImpl<>(index, this, r -> new ThreeWindingsTransformerToInjectionAttributesAdapter(r.getAttributes(), ThreeSides.ONE));
        terminal2 = new TerminalImpl<>(index, this, r -> new ThreeWindingsTransformerToInjectionAttributesAdapter(r.getAttributes(), ThreeSides.TWO));
        terminal3 = new TerminalImpl<>(index, this, r -> new ThreeWindingsTransformerToInjectionAttributesAdapter(r.getAttributes(), ThreeSides.THREE));
    }

    static ThreeWindingsTransformerImpl create(NetworkObjectIndex index, Resource<ThreeWindingsTransformerAttributes> resource) {
        return new ThreeWindingsTransformerImpl(index, resource);
    }

    @Override
    public List<Terminal> getTerminals(ThreeSides side) {
        if (side == null) {
            return Arrays.asList(terminal1, terminal2, terminal3);
        } else {
            return switch (side) {
                case ONE -> Collections.singletonList(leg1.getTerminal());
                case TWO -> Collections.singletonList(leg2.getTerminal());
                case THREE -> Collections.singletonList(leg3.getTerminal());
            };
        }
    }

    @Override
    public Terminal getTerminal(ThreeSides side) {
        return switch (side) {
            case ONE -> leg1.getTerminal();
            case TWO -> leg2.getTerminal();
            case THREE -> leg3.getTerminal();
        };
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        boolean isLeg1ConnectedToVoltageLevel = isLegConnectedToVoltageLevel(getLeg1(), voltageLevelId);
        boolean isLeg2ConnectedToVoltageLevel = isLegConnectedToVoltageLevel(getLeg2(), voltageLevelId);
        boolean isLeg3ConnectedToVoltageLevel = isLegConnectedToVoltageLevel(getLeg3(), voltageLevelId);
        if (isLeg1ConnectedToVoltageLevel
            && isLeg2ConnectedToVoltageLevel
            && isLeg3ConnectedToVoltageLevel) {
            throw new PowsyblException("The three terminals are connected to the same voltage level " + voltageLevelId);
        } else if (isLeg1ConnectedToVoltageLevel && isLeg2ConnectedToVoltageLevel
            || isLeg3ConnectedToVoltageLevel && isLeg1ConnectedToVoltageLevel
            || isLeg2ConnectedToVoltageLevel && isLeg3ConnectedToVoltageLevel) {
            throw new PowsyblException("Two of the three terminals are connected to the same voltage level " + voltageLevelId);
        } else if (isLeg1ConnectedToVoltageLevel) {
            return getLeg1().getTerminal();
        } else if (isLeg2ConnectedToVoltageLevel) {
            return getLeg2().getTerminal();
        } else if (isLeg3ConnectedToVoltageLevel) {
            return getLeg3().getTerminal();
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    private boolean isLegConnectedToVoltageLevel(ThreeWindingsTransformer.Leg leg, String voltageLevelId) {
        return Optional.ofNullable(leg.getTerminal().getVoltageLevel())
            .map(vl -> voltageLevelId.equals(vl.getId()))
            .orElse(Boolean.FALSE);
    }

    @Override
    public ThreeSides getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (leg1.getTerminal() == terminal) {
            return ThreeSides.ONE;
        } else if (leg2.getTerminal() == terminal) {
            return ThreeSides.TWO;
        } else if (leg3.getTerminal() == terminal) {
            return ThreeSides.THREE;
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
        return getResource().getAttributes().getRatedU0();
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(double limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT)
            || checkPermanentLimit2(limitReduction, LimitType.CURRENT)
            || checkPermanentLimit3(limitReduction, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        Overload o1 = checkTemporaryLimits1(LimitType.CURRENT);
        Overload o2 = checkTemporaryLimits2(LimitType.CURRENT);
        Overload o3 = checkTemporaryLimits3(LimitType.CURRENT);
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration3 = o3 != null ? o3.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(Math.min(duration1, duration2), duration3);
    }

    @Override
    public boolean checkPermanentLimit(ThreeSides side, double limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, side, limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit(ThreeSides side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(double limitReduction, LimitType type) {
        return checkPermanentLimit(ThreeSides.ONE, limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(double limitReduction, LimitType type) {
        return checkPermanentLimit(ThreeSides.TWO, limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public boolean checkPermanentLimit3(double limitReduction, LimitType type) {
        return checkPermanentLimit(ThreeSides.THREE, limitReduction, type);
    }

    @Override
    public boolean checkPermanentLimit3(LimitType type) {
        return checkPermanentLimit3(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits(ThreeSides side, double limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, side, limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits(ThreeSides side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(double limitReduction, LimitType type) {
        return checkTemporaryLimits(ThreeSides.ONE, limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(double limitReduction, LimitType type) {
        return checkTemporaryLimits(ThreeSides.TWO, limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits3(double limitReduction, LimitType type) {
        return checkTemporaryLimits(ThreeSides.THREE, limitReduction, type);
    }

    @Override
    public Overload checkTemporaryLimits3(LimitType type) {
        return checkTemporaryLimits3(1f, type);
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2, terminal3);
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeThreeWindingsTransformer(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        var resource = getResource();
        if (type == CgmesTapChangers.class) {
            resource.getAttributes().setCgmesTapChangerAttributesList(new ArrayList<>());
        } else {
            super.addExtension(type, extension);
        }
    }

    private <E extends Extension<ThreeWindingsTransformer>> E createConnectablePositionExtension() {
        E extension = null;
        var resource = getResource();
        if (resource.getAttributes().getPosition1() != null
                || resource.getAttributes().getPosition2() != null
                || resource.getAttributes().getPosition3() != null) {
            return (E) new ConnectablePositionImpl<>(this,
                null,
                connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition1(),
                connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition2(),
                connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition3());
        }
        return extension;
    }

    @Override
    public <E extends Extension<ThreeWindingsTransformer>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        } else if (type == ThreeWindingsTransformerPhaseAngleClock.class) {
            extension = createPhaseAngleClock();
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
            extension = createConnectablePositionExtension();
        } else if (name.equals(ThreeWindingsTransformerPhaseAngleClock.NAME)) {
            extension = createPhaseAngleClock();
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
        E extension = createConnectablePositionExtension();
        if (extension != null) {
            result.add(extension);
        }
        extension = createPhaseAngleClock();
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
        var resource = getResource();
        ThreeWindingsTransformerPhaseAngleClockAttributes phaseAngleClock = resource.getAttributes().getPhaseAngleClock();
        if (phaseAngleClock != null) {
            extension = (E) new ThreeWindingsTransformerPhaseAngleClockImpl(this);
        }
        return extension;
    }

    private <E extends Extension<ThreeWindingsTransformer>> E createCgmesTapChangers() {
        E extension = null;
        var resource = getResource();
        if (resource.getAttributes().getCgmesTapChangerAttributesList() != null) {
            extension = (E) new CgmesTapChangersImpl(this);
        }
        return extension;
    }
}
