/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T>, U extends BranchAttributes> extends AbstractIdentifiableImpl<T, U>
        implements Branch<T>, LimitsOwner<Branch.Side>, ConnectablePositionCreator<T> {

    private final Terminal terminal1;

    private final Terminal terminal2;

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
    public Terminal getTerminal1() {
        return terminal1;
    }

    @Override
    public Terminal getTerminal2() {
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
    public ApparentPowerLimits getApparentPowerLimits1() {
        return resource.getAttributes().getApparentPowerLimits1() != null ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits1()) : null;
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits2() {
        return resource.getAttributes().getApparentPowerLimits2() != null ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits2()) : null;
    }

    @Override
    public void setApparentPowerLimits(Branch.Side side, LimitsAttributes apparentPowerLimitsAttributes) {
        if (side == Branch.Side.ONE) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits1();
            resource.getAttributes().setApparentPowerLimits1(apparentPowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits1", oldApparentPowerLimits, apparentPowerLimitsAttributes);
        } else if (side == Branch.Side.TWO) {
            LimitsAttributes oldApparentPowerLimits = resource.getAttributes().getApparentPowerLimits2();
            resource.getAttributes().setApparentPowerLimits2(apparentPowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits2", oldApparentPowerLimits, apparentPowerLimitsAttributes);
        }
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
    public ActivePowerLimits getActivePowerLimits1() {
        return resource.getAttributes().getActivePowerLimits1() != null ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits1()) : null;
    }

    @Override
    public ActivePowerLimits getActivePowerLimits2() {
        return resource.getAttributes().getActivePowerLimits2() != null ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits2()) : null;
    }

    @Override
    public void setActivePowerLimits(Branch.Side side, LimitsAttributes activePowerLimitsAttributes) {
        if (side == Branch.Side.ONE) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits1();
            resource.getAttributes().setActivePowerLimits1(activePowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits1", oldActivePowerLimits, activePowerLimitsAttributes);
        } else if (side == Branch.Side.TWO) {
            LimitsAttributes oldActivePowerLimits = resource.getAttributes().getActivePowerLimits2();
            resource.getAttributes().setActivePowerLimits2(activePowerLimitsAttributes);
            index.notifyUpdate(this, "apparentPowerLimits2", oldActivePowerLimits, activePowerLimitsAttributes);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CurrentLimits getCurrentLimits(Branch.Side side) {
        switch (side) {
            case ONE:
                return getCurrentLimits1();
            case TWO:
                return getCurrentLimits2();
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return resource.getAttributes().getCurrentLimits1() != null ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits1()) : null;
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return resource.getAttributes().getCurrentLimits2() != null ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits2()) : null;
    }

    @Override
    public boolean isOverloaded() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getOverloadDuration() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit1() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean checkPermanentLimit2() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits1() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Branch.Overload checkTemporaryLimits2() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<T>) extension;
            resource.getAttributes().setPosition1(connectablePositionExtension.getFeeder1().getConnectablePositionAttributes());
            resource.getAttributes().setPosition2(connectablePositionExtension.getFeeder2().getConnectablePositionAttributes());
            updateResource();
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public ConnectablePositionImpl<T> createConnectablePositionExtension(Feeder feeder, Feeder feeder1, Feeder feeder2,
                                                                         Feeder feeder3) {
        Objects.requireNonNull(feeder2);
        if (feeder3 != null) {
            throw new IllegalArgumentException("feeder3 must be null for branches");
        }
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
        return new ConnectablePositionImpl<>(getBranch(),
                null,
                new ConnectablePositionImpl.FeederImpl(cpa1),
                new ConnectablePositionImpl.FeederImpl(cpa2),
                null);
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = (E) connectablePositionExtension;
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
        return extensions;
    }
}
