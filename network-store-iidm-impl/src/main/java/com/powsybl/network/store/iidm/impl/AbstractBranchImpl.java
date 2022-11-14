/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition.Feeder;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.iidm.impl.extensions.BranchStatusImpl;
import com.powsybl.network.store.model.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T>, U extends BranchAttributes> extends AbstractIdentifiableImpl<T, U>
        implements Branch<T>, LimitsOwner<Branch.Side>, ConnectablePositionCreator<T> {

    private final TerminalImpl<BranchToInjectionAttributesAdapter> terminal1;

    private final TerminalImpl<BranchToInjectionAttributesAdapter> terminal2;

    private ConnectablePositionImpl<T> connectablePositionExtension;

    protected AbstractBranchImpl(NetworkObjectIndex index, Resource<U> resource) {
        super(index, resource);
        terminal1 = TerminalImpl.create(index, new BranchToInjectionAttributesAdapter(this, resource.getAttributes(), true), getBranch());
        terminal2 = TerminalImpl.create(index, new BranchToInjectionAttributesAdapter(this, resource.getAttributes(), false), getBranch());
        ConnectablePositionAttributes cpa1 = resource.getAttributes().getPosition1();
        ConnectablePositionAttributes cpa2 = resource.getAttributes().getPosition2();
        if (cpa1 != null && cpa2 != null) {
            connectablePositionExtension = new ConnectablePositionImpl<>(getBranch(), null,
                    new ConnectablePositionImpl.FeederImpl(cpa1),
                    new ConnectablePositionImpl.FeederImpl(cpa2), null);
        }
    }

    protected abstract T getBranch();

    @Override
    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2);
    }

    @Override
    public TerminalImpl<BranchToInjectionAttributesAdapter> getTerminal1() {
        return terminal1;
    }

    @Override
    public TerminalImpl<BranchToInjectionAttributesAdapter> getTerminal2() {
        return terminal2;
    }

    @Override
    public Terminal getTerminal(Branch.Side side) {
        switch (side) {
            case ONE:
                return terminal1;
            case TWO:
                return terminal2;
            default:
                throw new UnsupportedOperationException();
        }
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
    public Branch.Side getSide(Terminal terminal) {
        if (terminal == terminal1) {
            return Branch.Side.ONE;
        } else if (terminal == terminal2) {
            return Branch.Side.TWO;
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
    public void setCurrentLimits(Branch.Side side, LimitsAttributes currentLimits) {
        var resource = checkResource();
        if (side == Branch.Side.ONE) {
            LimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits1();
            resource.getAttributes().setCurrentLimits1(currentLimits);
            updateResource();
            index.notifyUpdate(this, "currentLimits1", oldCurrentLimits, currentLimits);
        } else if (side == Branch.Side.TWO) {
            LimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits2();
            resource.getAttributes().setCurrentLimits2(currentLimits);
            updateResource();
            index.notifyUpdate(this, "currentLimits2", oldCurrentLimits, currentLimits);
        }
    }

    @Override
    public AbstractIdentifiableImpl getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.TWO, this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        return new ApparentPowerLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        return new ApparentPowerLimitsAdderImpl<>(Side.TWO, this);
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits(Side side) {
        switch (side) {
            case ONE:
                return getNullableApparentPowerLimits1();
            case TWO:
                return getNullableApparentPowerLimits2();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits(Side side) {
        return Optional.ofNullable(getNullableApparentPowerLimits(side));
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits1() {
        var resource = checkResource();
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
        var resource = checkResource();
        return resource.getAttributes().getApparentPowerLimits2() != null
                ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits2())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return Optional.ofNullable(getNullableApparentPowerLimits2());
    }

    @Override
    public void setApparentPowerLimits(Branch.Side side, LimitsAttributes apparentPowerLimitsAttributes) {
        var resource = checkResource();
        if (side == Branch.Side.ONE) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits1();
            resource.getAttributes().setApparentPowerLimits1(apparentPowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits1", oldApparentPowerLimits, apparentPowerLimitsAttributes);
        } else if (side == Branch.Side.TWO) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits2();
            resource.getAttributes().setApparentPowerLimits2(apparentPowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits2", oldApparentPowerLimits, apparentPowerLimitsAttributes);
        }
        updateResource();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        return new ActivePowerLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        return new ActivePowerLimitsAdderImpl<>(Side.TWO, this);
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits(Side side) {
        switch (side) {
            case ONE:
                return getNullableActivePowerLimits1();
            case TWO:
                return getNullableActivePowerLimits2();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits(Side side) {
        return Optional.ofNullable(getNullableActivePowerLimits(side));
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits1() {
        var resource = checkResource();
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
        var resource = checkResource();
        return resource.getAttributes().getActivePowerLimits2() != null
                ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits2())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return Optional.ofNullable(getNullableActivePowerLimits2());
    }

    @Override
    public void setActivePowerLimits(Branch.Side side, LimitsAttributes activePowerLimitsAttributes) {
        var resource = checkResource();
        if (side == Branch.Side.ONE) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits1();
            resource.getAttributes().setActivePowerLimits1(activePowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits1", oldActivePowerLimits, activePowerLimitsAttributes);
        } else if (side == Branch.Side.TWO) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits2();
            resource.getAttributes().setActivePowerLimits2(activePowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits2", oldActivePowerLimits, activePowerLimitsAttributes);
        }
        updateResource();
    }

    @Override
    public CurrentLimits getNullableCurrentLimits(Side side) {
        switch (side) {
            case ONE:
                return getNullableCurrentLimits1();
            case TWO:
                return getNullableCurrentLimits2();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits(Branch.Side side) {
        return Optional.ofNullable(getNullableCurrentLimits(side));
    }

    @Override
    public CurrentLimits getNullableCurrentLimits1() {
        var resource = checkResource();
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
        var resource = checkResource();
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
        Branch.Overload o1 = checkTemporaryLimits1(LimitType.CURRENT);
        Branch.Overload o2 = checkTemporaryLimits2(LimitType.CURRENT);
        int duration1 = o1 != null ? o1.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        int duration2 = o2 != null ? o2.getTemporaryLimit().getAcceptableDuration() : Integer.MAX_VALUE;
        return Math.min(duration1, duration2);
    }

    @Override
    public Overload checkTemporaryLimits(Side side, LimitType type) {
        return this.checkTemporaryLimits(side, 1.0F, type);
    }

    @Override
    public Overload checkTemporaryLimits(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return this.checkTemporaryLimits1(limitReduction, type);
            case TWO:
                return this.checkTemporaryLimits2(limitReduction, type);
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.ONE, limitReduction, this.getValueForLimit(this.getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return this.checkTemporaryLimits1(1.0F, type);
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, Side.TWO, limitReduction, this.getValueForLimit(this.getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return this.checkTemporaryLimits2(1.0F, type);
    }

    @Override
    public boolean checkPermanentLimit(Side side, float limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return checkPermanentLimit1(limitReduction, type);

            case TWO:
                return checkPermanentLimit2(limitReduction, type);

            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean checkPermanentLimit(Side side, LimitType type) {
        return checkPermanentLimit(side, 1f, type);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.ONE, limitReduction, getValueForLimit(getTerminal1(), type), type);
    }

    @Override
    public boolean checkPermanentLimit1(LimitType type) {
        return checkPermanentLimit1(1f, type);
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        return LimitViolationUtils.checkPermanentLimit(this, Side.TWO, limitReduction, getValueForLimit(getTerminal2(), type), type);
    }

    @Override
    public boolean checkPermanentLimit2(LimitType type) {
        return this.checkPermanentLimit2(1.0F, type);
    }

    public BranchStatus.Status getBranchStatus() {
        return BranchStatus.Status.valueOf(checkResource().getAttributes().getBranchStatus());
    }

    public double getValueForLimit(Terminal t, LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return t.getP();
            case APPARENT_POWER:
                return Math.sqrt(t.getP() * t.getP() + t.getQ() * t.getQ());
            case CURRENT:
                return t.getI();
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    public Branch setBranchStatus(BranchStatus.Status branchStatus) {
        Objects.requireNonNull(branchStatus);
        var resource = checkResource();
        String oldValue = resource.getAttributes().getBranchStatus();
        resource.getAttributes().setBranchStatus(branchStatus.name());
        updateResource();
        notifyUpdate("branchStatus", oldValue != null ? oldValue : BranchStatus.Status.IN_OPERATION.name(), branchStatus.name(), false);
        return this;
    }

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<T>) extension;
            if (connectablePositionExtension.getFeeder1() != null) {
                resource.getAttributes().setPosition1(connectablePositionExtension.getFeeder1().getConnectablePositionAttributes());
            }
            if (connectablePositionExtension.getFeeder2() != null) {
                resource.getAttributes().setPosition2(connectablePositionExtension.getFeeder2().getConnectablePositionAttributes());
                updateResource();
            }
        } else if (type == BranchStatus.class) {
            BranchStatus branchStatus = (BranchStatus) extension;
            setBranchStatus(branchStatus.getStatus());
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public ConnectablePositionImpl<T> createConnectablePositionExtension(Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        ConnectablePosition.check(feeder, feeder1, feeder2, feeder3);
        var cpa1 = ConnectablePositionAttributes.builder()
                .label(feeder1.getName())
                .order(feeder1.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder1.getDirection().name()))
                .build();
        var cpa2 = ConnectablePositionAttributes.builder()
                .label(feeder2.getName())
                .order(feeder2.getOrder().orElse(null))
                .direction(ConnectableDirection.valueOf(feeder2.getDirection().name()))
                .build();
        return new ConnectablePositionImpl<>(getBranch(),
                null,
                new ConnectablePositionImpl.FeederImpl(cpa1),
                new ConnectablePositionImpl.FeederImpl(cpa2),
                null);
    }

    private <E extends Extension<T>> E createBranchStatusExtension() {
        E extension = null;
        String branchStatus = checkResource().getAttributes().getBranchStatus();
        if (branchStatus != null) {
            extension = (E) new BranchStatusImpl(this, BranchStatus.Status.valueOf(branchStatus));
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = (E) connectablePositionExtension;
        } else if (type == BranchStatus.class) {
            extension = (E) createBranchStatusExtension();
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = (E) connectablePositionExtension;
        } else if (name.equals("branchStatus")) {
            extension = (E) createBranchStatusExtension();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        if (connectablePositionExtension != null) {
            extensions.add((E) connectablePositionExtension);
        }
        E branchStatusExtension = createBranchStatusExtension();
        if (branchStatusExtension != null) {
            extensions.add(branchStatusExtension);
        }
        return extensions;
    }
}
