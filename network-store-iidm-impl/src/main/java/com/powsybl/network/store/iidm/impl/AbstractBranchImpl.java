/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition.Feeder;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T>, U extends BranchAttributes> extends AbstractIdentifiableImpl<T, U> implements CurrentLimitsOwner<Branch.Side>, ConnectablePositionCreator<T> {

    private final Terminal terminal1;

    private final Terminal terminal2;

    private ConnectablePositionImpl<T> connectablePositionExtension;

    protected AbstractBranchImpl(NetworkObjectIndex index, Resource<U> resource) {
        super(index, resource);
        terminal1 = TerminalImpl.create(index, new BranchToInjectionAttributesAdapter(resource.getAttributes(), true), getBranch());
        terminal2 = TerminalImpl.create(index, new BranchToInjectionAttributesAdapter(resource.getAttributes(), false), getBranch());
        ConnectablePositionAttributes cpa1 = resource.getAttributes().getPosition1();
        ConnectablePositionAttributes cpa2 = resource.getAttributes().getPosition2();
        if (cpa1 != null && cpa2 != null) {
            connectablePositionExtension = new ConnectablePositionImpl<>(getBranch(), null,
                    new ConnectablePositionImpl.FeederImpl(cpa1),
                    new ConnectablePositionImpl.FeederImpl(cpa2), null);
        }
    }

    protected abstract T getBranch();

    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2);
    }

    public Terminal getTerminal1() {
        return terminal1;
    }

    public Terminal getTerminal2() {
        return terminal2;
    }

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

    public Branch.Side getSide(Terminal terminal) {
        if (terminal == terminal1) {
            return Branch.Side.ONE;
        } else if (terminal == terminal2) {
            return Branch.Side.TWO;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public void setCurrentLimits(Branch.Side side, CurrentLimitsAttributes currentLimits) {
        if (side == Branch.Side.ONE) {
            CurrentLimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits1();
            resource.getAttributes().setCurrentLimits1(currentLimits);
            index.notifyUpdate(this, "currentLimits1", oldCurrentLimits, currentLimits);
        } else if (side == Branch.Side.TWO) {
            CurrentLimitsAttributes oldCurrentLimits = resource.getAttributes().getCurrentLimits2();
            resource.getAttributes().setCurrentLimits2(currentLimits);
            index.notifyUpdate(this, "currentLimits2", oldCurrentLimits, currentLimits);
        }
    }

    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.ONE, this);
    }

    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(Branch.Side.TWO, this);
    }

    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }

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

    public CurrentLimits getCurrentLimits1() {
        return resource.getAttributes().getCurrentLimits1() != null ? new CurrentLimitsImpl(resource.getAttributes().getCurrentLimits1()) : null;
    }

    public CurrentLimits getCurrentLimits2() {
        return resource.getAttributes().getCurrentLimits2() != null ? new CurrentLimitsImpl(resource.getAttributes().getCurrentLimits2()) : null;
    }

    public boolean isOverloaded() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isOverloaded(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getOverloadDuration() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit1() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit2() {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits1() {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits2() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<T>) extension;
            resource.getAttributes().setPosition1(connectablePositionExtension.getFeeder1().getConnectablePositionAttributes());
            resource.getAttributes().setPosition2(connectablePositionExtension.getFeeder2().getConnectablePositionAttributes());
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
