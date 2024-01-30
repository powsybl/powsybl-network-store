/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.network.store.model.BranchAttributes;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T> & Connectable<T>, U extends BranchAttributes> extends AbstractIdentifiableImpl<T, U>
        implements Branch<T>, Connectable<T>, LimitsOwner<TwoSides> {

    private final TerminalImpl<U> terminal1;

    private final TerminalImpl<U> terminal2;

    protected AbstractBranchImpl(NetworkObjectIndex index, Resource<U> resource) {
        super(index, resource);
        terminal1 = new TerminalImpl<>(index, this, r -> new BranchToInjectionAttributesAdapter(this, r.getAttributes(), true));
        terminal2 = new TerminalImpl<>(index, this, r -> new BranchToInjectionAttributesAdapter(this, r.getAttributes(), false));
    }

    protected abstract T getBranch();

    @Override
    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2);
    }

    @Override
    public TerminalImpl<U> getTerminal1() {
        return terminal1;
    }

    @Override
    public TerminalImpl<U> getTerminal2() {
        return terminal2;
    }

    @Override
    public Terminal getTerminal(TwoSides side) {
        return switch (side) {
            case ONE -> terminal1;
            case TWO -> terminal2;
        };
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (terminal1.getVoltageLevel().getId().equals(voltageLevelId)) {
            return terminal1;
        } else if (terminal2.getVoltageLevel().getId().equals(voltageLevelId)) {
            return terminal2;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public TwoSides getSide(Terminal terminal) {
        if (terminal == terminal1) {
            return TwoSides.ONE;
        } else if (terminal == terminal2) {
            return TwoSides.TWO;
        } else {
            throw new AssertionError();
        }
    }

    public void notifyUpdate(String attribute, Object oldValue, Object newValue, boolean withVariantId) {
        if (withVariantId) {
            index.notifyUpdate(this, attribute, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, newValue);
        } else {
            index.notifyUpdate(this, attribute, oldValue, newValue);
        }
    }

    @Override
    public void setCurrentLimits(TwoSides side, LimitsAttributes currentLimits) {
        var resource = getResource();
        if (side == TwoSides.ONE) {
            LimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits1();
            if (currentLimits != oldCurrentLimits) {
                updateResource(res -> res.getAttributes().setCurrentLimits1(currentLimits));
                index.notifyUpdate(this, "currentLimits1", oldCurrentLimits, currentLimits);
            }
        } else if (side == TwoSides.TWO) {
            LimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits2();
            if (currentLimits != oldCurrentLimits) {
                updateResource(res -> res.getAttributes().setCurrentLimits2(currentLimits));
                index.notifyUpdate(this, "currentLimits2", oldCurrentLimits, currentLimits);
            }
        }
    }

    @Override
    public AbstractIdentifiableImpl getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(TwoSides.ONE, this);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(TwoSides.TWO, this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return new ApparentPowerLimitsAdderImpl<>(TwoSides.ONE, this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return new ApparentPowerLimitsAdderImpl<>(TwoSides.TWO, this);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableApparentPowerLimits1();
            case TWO -> getNullableApparentPowerLimits2();
        };
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        return Optional.ofNullable(getNullableApparentPowerLimits(side));
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        var resource = getResource();
        return resource.getAttributes().getApparentPowerLimits1() != null
                ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits1())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return Optional.ofNullable(getNullableApparentPowerLimits1());
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        var resource = getResource();
        return resource.getAttributes().getApparentPowerLimits2() != null
                ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits2())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return Optional.ofNullable(getNullableApparentPowerLimits2());
    }

    @Override
    public void setApparentPowerLimits(TwoSides side, LimitsAttributes apparentPowerLimitsAttributes) {
        var resource = getResource();
        if (side == TwoSides.ONE) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits1();
            if (apparentPowerLimitsAttributes != oldApparentPowerLimits) {
                updateResource(res -> res.getAttributes().setApparentPowerLimits1(apparentPowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits1", oldApparentPowerLimits, apparentPowerLimitsAttributes);
            }
        } else if (side == TwoSides.TWO) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits2();
            if (apparentPowerLimitsAttributes != oldApparentPowerLimits) {
                updateResource(res -> res.getAttributes().setApparentPowerLimits2(apparentPowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits2", oldApparentPowerLimits, apparentPowerLimitsAttributes);
            }
        }
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return new ActivePowerLimitsAdderImpl<>(TwoSides.ONE, this);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return new ActivePowerLimitsAdderImpl<>(TwoSides.TWO, this);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableActivePowerLimits1();
            case TWO -> getNullableActivePowerLimits2();
        };
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        return Optional.ofNullable(getNullableActivePowerLimits(side));
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        var resource = getResource();
        return resource.getAttributes().getActivePowerLimits1() != null
                ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits1())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return Optional.ofNullable(getNullableActivePowerLimits1());
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        var resource = getResource();
        return resource.getAttributes().getActivePowerLimits2() != null
                ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits2())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return Optional.ofNullable(getNullableActivePowerLimits2());
    }

    @Override
    public void setActivePowerLimits(TwoSides side, LimitsAttributes activePowerLimitsAttributes) {
        var resource = getResource();
        if (side == TwoSides.ONE) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits1();
            if (activePowerLimitsAttributes != oldActivePowerLimits) {
                updateResource(res -> res.getAttributes().setActivePowerLimits1(activePowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits1", oldActivePowerLimits, activePowerLimitsAttributes);
            }
        } else if (side == TwoSides.TWO) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits2();
            if (activePowerLimitsAttributes != oldActivePowerLimits) {
                updateResource(res -> res.getAttributes().setActivePowerLimits2(activePowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits2", oldActivePowerLimits, activePowerLimitsAttributes);
            }
        }
    }

    @Override
    public CurrentLimits getNullableCurrentLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableCurrentLimits1();
            case TWO -> getNullableCurrentLimits2();
        };
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(TwoSides side) {
        return Optional.ofNullable(getNullableCurrentLimits(side));
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        var resource = getResource();
        return resource.getAttributes().getCurrentLimits1() != null
                ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits1())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return Optional.ofNullable(getNullableCurrentLimits1());
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        var resource = getResource();
        return resource.getAttributes().getCurrentLimits2() != null
                ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits2())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return Optional.ofNullable(getNullableCurrentLimits2());
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
        Overload o1 = checkTemporaryLimits1(LimitType.CURRENT);
        Overload o2 = checkTemporaryLimits2(LimitType.CURRENT);
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, LimitType type) {
        return this.checkTemporaryLimits(side, 1.0F, type);
    }

    @Override
    public Overload checkTemporaryLimits(TwoSides side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        return switch (side) {
            case ONE -> this.checkTemporaryLimits1(limitReduction, type);
            case TWO -> this.checkTemporaryLimits2(limitReduction, type);
        };
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReduction, this.getValueForLimit(this.getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return this.checkTemporaryLimits1(1.0F, type);
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.TWO, limitReduction, this.getValueForLimit(this.getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return this.checkTemporaryLimits2(1.0F, type);
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        return switch (side) {
            case ONE -> checkPermanentLimit1(limitReduction, type);
            case TWO -> checkPermanentLimit2(limitReduction, type);
        };
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
        return this.checkPermanentLimit2(1.0F, type);
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

    private <E extends Extension<T>> E createConnectablePositionExtension() {
        E extension = null;
        var resource = getResource();
        if (resource.getAttributes().getPosition1() != null || resource.getAttributes().getPosition2() != null) {
            return (E) new ConnectablePositionImpl<>(getBranch(),
                null,
                connectable -> ((AbstractBranchImpl<?, ?>) connectable).getResource().getAttributes().getPosition1(),
                connectable -> ((AbstractBranchImpl<?, ?>) connectable).getResource().getAttributes().getPosition2(),
                null);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createConnectablePositionExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }
}
