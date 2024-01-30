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
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TieLineAttributes;

import java.util.*;
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
        return Optional.ofNullable(getDanglingLine1().getPairingKey()).orElseGet(() -> getDanglingLine2().getPairingKey());
    }

    @Override
    public DanglingLineImpl getDanglingLine1() {
        return index.getDanglingLine(getResource().getAttributes().getDanglingLine1Id()).orElseThrow();
    }

    @Override
    public DanglingLineImpl getDanglingLine2() {
        return index.getDanglingLine(getResource().getAttributes().getDanglingLine2Id()).orElseThrow();
    }

    @Override
    public DanglingLine getDanglingLine(TwoSides side) {
        if (TwoSides.ONE == side) {
            return getDanglingLine1();
        } else {
            return getDanglingLine2();
        }
    }

    @Override
    public DanglingLine getDanglingLine(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (voltageLevelId.equals(getDanglingLine1().getTerminal().getVoltageLevelId())) {
            return getDanglingLine1();
        } else if (voltageLevelId.equals(getDanglingLine2().getTerminal().getVoltageLevelId())) {
            return getDanglingLine2();
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

        getDanglingLine1().removeTieLine();
        getDanglingLine2().removeTieLine();
        index.removeTieLine(resource.getId());

        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public double getR() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getR(dl1, dl2);
    }

    @Override
    public double getX() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getX(dl1, dl2);
    }

    @Override
    public double getG1() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getG1(dl1, dl2);
    }

    @Override
    public double getG2() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getG2(dl1, dl2);
    }

    @Override
    public double getB1() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getB1(dl1, dl2);
    }

    @Override
    public double getB2() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getB2(dl1, dl2);
    }

    static TieLineImpl create(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        return new TieLineImpl(index, resource);
    }

    @Override
    public Terminal getTerminal1() {
        return getDanglingLine1().getTerminal();
    }

    @Override
    public Terminal getTerminal2() {
        return getDanglingLine2().getTerminal();
    }

    @Override
    public Terminal getTerminal(TwoSides side) {
        Objects.requireNonNull(side);
        if (TwoSides.ONE == side) {
            return getDanglingLine1().getTerminal();
        } else {
            return getDanglingLine2().getTerminal();
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
    public Collection<OperationalLimits> getOperationalLimits1() {
        return getDanglingLine1().getOperationalLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return getDanglingLine1().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return getDanglingLine1().newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getDanglingLine1().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return getDanglingLine1().newApparentPowerLimits();
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits2() {
        return getDanglingLine2().getOperationalLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getDanglingLine1().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return getDanglingLine1().newActivePowerLimits();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return getDanglingLine2().getCurrentLimits();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return getDanglingLine2().newCurrentLimits();
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getDanglingLine2().getApparentPowerLimits();
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return getDanglingLine2().newApparentPowerLimits();
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getDanglingLine2().getActivePowerLimits();
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return getDanglingLine2().newActivePowerLimits();
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
    public boolean isOverloaded(float limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT) || checkPermanentLimit2(limitReduction, LimitType.CURRENT);
    }

    @Override
    public int getOverloadDuration() {
        return getOverloadDuration(checkTemporaryLimits1(LimitType.CURRENT), checkTemporaryLimits2(LimitType.CURRENT));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, float limitReduction, LimitType type) {
        return getFromSide(side,
            () -> checkPermanentLimit1(limitReduction, type),
            () -> checkPermanentLimit2(limitReduction, type));
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, TwoSides.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return checkPermanentLimit2(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, float limitReduction, LimitType type) {
        return getFromSide(side,
            () -> checkTemporaryLimits1(limitReduction, type),
            () -> checkTemporaryLimits2(limitReduction, type));
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, LimitType type) {
        return checkTemporaryLimits(side, 1f, type);
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return checkTemporaryLimits1(1f, type);
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
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
}
