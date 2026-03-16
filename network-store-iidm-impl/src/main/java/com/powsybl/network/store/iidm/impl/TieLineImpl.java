/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TieLineAttributes;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineImpl extends AbstractIdentifiableImpl<TieLine, TieLineAttributes> implements TieLine {

    public TieLineImpl(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        super(index, resource);
    }

    @Override
    public String getPairingKey() {
        return Optional.ofNullable(getBoundaryLine1().getPairingKey()).orElseGet(() -> getBoundaryLine2().getPairingKey());
    }

    @Override
    public BoundaryLineImpl getBoundaryLine1() {
        return index.getBoundaryLine(getResource().getAttributes().getDanglingLine1Id()).orElseThrow();
    }

    @Override
    public BoundaryLineImpl getBoundaryLine2() {
        return index.getBoundaryLine(getResource().getAttributes().getDanglingLine2Id()).orElseThrow();
    }

    @Override
    public BoundaryLine getBoundaryLine(TwoSides side) {
        if (TwoSides.ONE == side) {
            return getBoundaryLine1();
        } else {
            return getBoundaryLine2();
        }
    }

    @Override
    public BoundaryLine getBoundaryLine(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (voltageLevelId.equals(getBoundaryLine1().getTerminal().getVoltageLevelId())) {
            return getBoundaryLine1();
        } else if (voltageLevelId.equals(getBoundaryLine2().getTerminal().getVoltageLevelId())) {
            return getBoundaryLine2();
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        remove(false);
    }

    @Override
    public void remove(boolean updateDanglingLines) {
        var resource = getResource();

        index.notifyBeforeRemoval(this);

        if (updateDanglingLines) {
            // TODO implement
        }

        getBoundaryLine1().removeTieLine();
        getBoundaryLine2().removeTieLine();
        index.removeTieLine(resource.getId());

        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public double getR() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getR(dl1, dl2);
    }

    @Override
    public double getX() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getX(dl1, dl2);
    }

    @Override
    public double getG1() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getG1(dl1, dl2);
    }

    @Override
    public double getG2() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getG2(dl1, dl2);
    }

    @Override
    public double getB1() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getB1(dl1, dl2);
    }

    @Override
    public double getB2() {
        BoundaryLine dl1 = getBoundaryLine1();
        BoundaryLine dl2 = getBoundaryLine2();
        return TieLineUtil.getB2(dl1, dl2);
    }

    static TieLineImpl create(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        return new TieLineImpl(index, resource);
    }

    @Override
    public Terminal getTerminal1() {
        return getBoundaryLine1().getTerminal();
    }

    @Override
    public Terminal getTerminal2() {
        return getBoundaryLine2().getTerminal();
    }

    @Override
    public Terminal getTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        if (TwoSides.ONE == side) {
            return getBoundaryLine1().getTerminal();
        } else {
            return getBoundaryLine2().getTerminal();
        }
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return getTerminal(voltageLevelId, getTerminal1(), getTerminal2());
    }

    public TwoSides getSide(Terminal terminal) {
        return getSide(terminal, getTerminal1(), getTerminal2());
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return getBoundaryLine1().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        getBoundaryLine1().getCurrentLimits().ifPresent(OperationalLimits::remove);
        return getBoundaryLine1().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup1() {
        return getBoundaryLine1().getOrCreateSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup2() {
        return getBoundaryLine2().getOrCreateSelectedOperationalLimitsGroup();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getBoundaryLine1().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getBoundaryLine1().getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getBoundaryLine1().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getBoundaryLine1().getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return getBoundaryLine2().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        getBoundaryLine2().getCurrentLimits().ifPresent(OperationalLimits::remove);
        return getBoundaryLine2().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getBoundaryLine2().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getBoundaryLine2().getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getBoundaryLine2().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    @Deprecated(since = "1.29.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getBoundaryLine2().getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(TwoSides side) {
        return getFromSide(side, this::getCurrentLimits1, this::getCurrentLimits2);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        return getFromSide(side, this::getActivePowerLimits1, this::getActivePowerLimits2);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        return getFromSide(side, this::getApparentPowerLimits1, this::getApparentPowerLimits2);
    }

    @Override
    public boolean isOverloaded() {
        return isOverloaded(1.0f);
    }

    @Override
    public boolean isOverloaded(double limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT) || checkPermanentLimit2(limitReduction, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        return getOverloadDuration(checkTemporaryLimits1(LimitType.CURRENT), checkTemporaryLimits2(LimitType.CURRENT));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, double limitReduction, LimitType type) {
        return getFromSide(side,
            () -> checkPermanentLimit1(limitReduction, type),
            () -> checkPermanentLimit2(limitReduction, type));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Collection<Overload> checkAllTemporaryLimits(TwoSides side, double limitReductionValue, LimitType type) {
        return LimitViolationUtils.checkAllTemporaryLimits(this, side, limitReductionValue, getValueForLimit(getTerminal(side), type), type);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, double limitReduction, LimitType type) {
        return getFromSide(side,
            () -> checkTemporaryLimits1(limitReduction, type),
            () -> checkTemporaryLimits2(limitReduction, type));
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return checkTemporaryLimits2(1f, type);
    }

    public double getValueForLimit(Terminal t, LimitType type) {
        return switch (type) {
            case ACTIVE_POWER -> t.getP();
            case APPARENT_POWER -> Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT -> t.getI();
            default ->
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        };
    }

    static Terminal getTerminal(String voltageLevelId, Terminal terminal1, Terminal terminal2) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = terminal1.getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = terminal2.getVoltageLevel().getId().equals(voltageLevelId);
        if (side1 && side2) {
            throw new PowsyblException("Both terminals are connected to voltage level " + voltageLevelId);
        } else if (side1) {
            return terminal1;
        } else if (side2) {
            return terminal2;
        } else {
            throw new PowsyblException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    public static TwoSides getSide(Terminal terminal, Terminal terminal1, Terminal terminal2) {
        Objects.requireNonNull(terminal);
        if (terminal1 == terminal) {
            return TwoSides.ONE;
        } else if (terminal2 == terminal) {
            return TwoSides.TWO;
        } else {
            throw new IllegalStateException("The terminal is not connected to this branch");
        }
    }

    static <T> T getFromSide(TwoSides side, Supplier<T> getter1, Supplier<T> getter2) {
        Objects.requireNonNull(side);
        if (side == TwoSides.ONE) {
            return getter1.get();
        } else if (side == TwoSides.TWO) {
            return getter2.get();
        }
        throw new IllegalStateException("Unexpected side: " + side);
    }

    static int getOverloadDuration(Overload o1, Overload o2) {
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups1() {
        return getBoundaryLine1().getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId1() {
        return getBoundaryLine1().getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id) {
        return getBoundaryLine1().getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup1() {
        return getBoundaryLine1().getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup1(String id) {
        return getBoundaryLine1().newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup1(String id) {
        getBoundaryLine1().setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup1(String id) {
        getBoundaryLine1().removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup1() {
        getBoundaryLine1().cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups2() {
        return getBoundaryLine2().getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId2() {
        return getBoundaryLine2().getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id) {
        return getBoundaryLine2().getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup2() {
        return getBoundaryLine2().getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup2(String id) {
        return getBoundaryLine2().newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup2(String id) {
        getBoundaryLine2().setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void removeOperationalLimitsGroup2(String id) {
        getBoundaryLine2().removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup2() {
        getBoundaryLine2().cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public Collection<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups(TwoSides side) {
        return side == TwoSides.ONE ?
            getBoundaryLine1().getAllSelectedOperationalLimitsGroups() :
            getBoundaryLine2().getAllSelectedOperationalLimitsGroups();
    }

    @Override
    public Collection<String> getAllSelectedOperationalLimitsGroupIds(TwoSides side) {
        return side == TwoSides.ONE ?
            getBoundaryLine1().getAllSelectedOperationalLimitsGroupIds() :
            getBoundaryLine2().getAllSelectedOperationalLimitsGroupIds();
    }

    @Override
    public List<String> getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides side) {
        return side == TwoSides.ONE ?
            getBoundaryLine1().getAllSelectedOperationalLimitsGroupIdsOrdered() :
            getBoundaryLine2().getAllSelectedOperationalLimitsGroupIdsOrdered();
    }

    @Override
    public void addSelectedOperationalLimitsGroups(TwoSides side, String... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        if (side == TwoSides.ONE) {
            getBoundaryLine1().addSelectedOperationalLimitsGroups(ids);
        } else {
            getBoundaryLine2().addSelectedOperationalLimitsGroups(ids);
        }
    }

    @Override
    public void addSelectedOperationalLimitsGroupByPredicate(TwoSides side, Predicate<String> operationalLimitsGroupIdPredicate) {
        if (side == TwoSides.ONE) {
            getBoundaryLine1().addSelectedOperationalLimitsGroupByPredicate(operationalLimitsGroupIdPredicate);
        } else {
            getBoundaryLine2().addSelectedOperationalLimitsGroupByPredicate(operationalLimitsGroupIdPredicate);
        }
    }

    @Override
    public void deselectOperationalLimitsGroups(TwoSides side, String... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        if (side == TwoSides.ONE) {
            getBoundaryLine1().deselectOperationalLimitsGroups(ids);
        } else {
            getBoundaryLine2().deselectOperationalLimitsGroups(ids);
        }
    }

    @Override
    public boolean connectBoundaryLines() {
        return connectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL_BREAKER, null);
    }

    @Override
    public boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate) {
        return connectBoundaryLines(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate, TwoSides side) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminalsOfDanglingLines(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnectBoundaryLines() {
        return disconnectBoundaryLines(SwitchPredicates.IS_CLOSED_BREAKER, null);
    }

    @Override
    public boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable) {
        return disconnectBoundaryLines(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable, TwoSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminalsOfDanglingLines(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    public List<Terminal> getTerminalsOfDanglingLines(TwoSides side) {
        return side == null ? List.of(getTerminal1(), getTerminal2()) : switch (side) {
            case ONE -> List.of(getTerminal1());
            case TWO -> List.of(getTerminal2());
        };
    }
}
