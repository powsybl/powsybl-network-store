/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T> & Connectable<T>, U extends BranchAttributes> extends AbstractConnectableImpl<T, U>
        implements Branch<T>, Connectable<T>, LimitsOwner<TwoSides> {

    private final TerminalImpl<U> terminal1;

    private final TerminalImpl<U> terminal2;

    protected AbstractBranchImpl(NetworkObjectIndex index, Resource<U> resource) {
        super(index, resource);
        terminal1 = new TerminalImpl<>(index, this, r -> new BranchToInjectionAttributesAdapter(this, r.getAttributes(), true));
        terminal2 = new TerminalImpl<>(index, this, r -> new BranchToInjectionAttributesAdapter(this, r.getAttributes(), false));
    }

    private static final String DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "DEFAULT";
    private static final String SELECTED_OPERATIONAL_LIMITS_GROUP_ID1 = "selectedOperationalLimitsGroupId1";
    private static final String SELECTED_OPERATIONAL_LIMITS_GROUP_ID2 = "selectedOperationalLimitsGroupId2";

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

    public void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(this, attribute, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, newValue);
    }

    private String getSelectedOperationalLimitsGroupId(TwoSides side) {
        return switch (side) {
            case ONE -> getResource().getAttributes().getSelectedOperationalLimitsGroupId1() != null ? getResource().getAttributes().getSelectedOperationalLimitsGroupId1() : DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
            case TWO -> getResource().getAttributes().getSelectedOperationalLimitsGroupId2() != null ? getResource().getAttributes().getSelectedOperationalLimitsGroupId2() : DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
        };
    }

    @Override
    public void setCurrentLimits(TwoSides side, LimitsAttributes currentLimits, String operationalLimitsGroupId) {
        var attributes = getResource().getAttributes();
        if (side == TwoSides.ONE) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup1(operationalLimitsGroupId);
            var oldCurrentLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getCurrentLimits() : null;
            if (currentLimits != oldCurrentLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setCurrentLimits(currentLimits));
                index.notifyUpdate(this, "currentLimits1", index.getNetwork().getVariantManager().getWorkingVariantId(), oldCurrentLimits, currentLimits);
            }
        } else if (side == TwoSides.TWO) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup2(operationalLimitsGroupId);
            var oldCurrentLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getCurrentLimits() : null;
            if (currentLimits != oldCurrentLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setCurrentLimits(currentLimits));
                index.notifyUpdate(this, "currentLimits2", index.getNetwork().getVariantManager().getWorkingVariantId(), oldCurrentLimits, currentLimits);
            }
        }
    }

    @Override
    public AbstractIdentifiableImpl getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.ONE, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
        return new CurrentLimitsAdderImpl<>(TwoSides.ONE, this, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.TWO, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
        return new CurrentLimitsAdderImpl<>(TwoSides.TWO, this, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.ONE, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
        return new ApparentPowerLimitsAdderImpl<>(TwoSides.ONE, this, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.TWO, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
        return new ApparentPowerLimitsAdderImpl<>(TwoSides.TWO, this, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
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
        loadOperationalLimitsGroup(TwoSides.ONE);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup1();
        return group != null && group.getApparentPowerLimits() != null
                ? new ApparentPowerLimitsImpl<>(this, TwoSides.ONE, group.getId(), group.getApparentPowerLimits())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return Optional.ofNullable(getNullableApparentPowerLimits1());
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits2() {
        loadOperationalLimitsGroup(TwoSides.TWO);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup2();
        return group != null && group.getApparentPowerLimits() != null
                ? new ApparentPowerLimitsImpl<>(this, TwoSides.TWO, group.getId(), group.getApparentPowerLimits())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return Optional.ofNullable(getNullableApparentPowerLimits2());
    }

    @Override
    public void setApparentPowerLimits(TwoSides side, LimitsAttributes apparentPowerLimitsAttributes, String operationalLimitsGroupId) {
        var attributes = getResource().getAttributes();
        if (side == TwoSides.ONE) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup1(operationalLimitsGroupId);
            var oldApparentPowerLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getApparentPowerLimits() : null;
            if (apparentPowerLimitsAttributes != oldApparentPowerLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setApparentPowerLimits(apparentPowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits1", index.getNetwork().getVariantManager().getWorkingVariantId(), oldApparentPowerLimits, apparentPowerLimitsAttributes);
            }
        } else if (side == TwoSides.TWO) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup2(operationalLimitsGroupId);
            var oldApparentPowerLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getApparentPowerLimits() : null;
            if (apparentPowerLimitsAttributes != oldApparentPowerLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setApparentPowerLimits(apparentPowerLimitsAttributes));
                index.notifyUpdate(this, "apparentPowerLimits2", index.getNetwork().getVariantManager().getWorkingVariantId(), oldApparentPowerLimits, apparentPowerLimitsAttributes);
            }
        }
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.ONE, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
        return new ActivePowerLimitsAdderImpl<>(TwoSides.ONE, this, getSelectedOperationalLimitsGroupId(TwoSides.ONE));
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        updateSelectedOperationalLimitsGroupIdIfNull(TwoSides.TWO, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
        return new ActivePowerLimitsAdderImpl<>(TwoSides.TWO, this, getSelectedOperationalLimitsGroupId(TwoSides.TWO));
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
        loadOperationalLimitsGroup(TwoSides.ONE);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup1();
        return group != null && group.getActivePowerLimits() != null
                ? new ActivePowerLimitsImpl<>(this, TwoSides.ONE, group.getId(), group.getActivePowerLimits())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits1() {
        return Optional.ofNullable(getNullableActivePowerLimits1());
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits2() {
        loadOperationalLimitsGroup(TwoSides.TWO);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup2();
        return group != null && group.getActivePowerLimits() != null
                ? new ActivePowerLimitsImpl<>(this, TwoSides.TWO, group.getId(), group.getActivePowerLimits())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits2() {
        return Optional.ofNullable(getNullableActivePowerLimits2());
    }

    private void updateSelectedOperationalLimitsGroupIdIfNull(TwoSides side, String id) {
        var resource = getResource();
        if (side == TwoSides.ONE && resource.getAttributes().getSelectedOperationalLimitsGroupId1() == null) {
            resource.getAttributes().setSelectedOperationalLimitsGroupId1(id);
        } else if (side == TwoSides.TWO && resource.getAttributes().getSelectedOperationalLimitsGroupId2() == null) {
            resource.getAttributes().setSelectedOperationalLimitsGroupId2(id);
        }
    }

    @Override
    public void setActivePowerLimits(TwoSides side, LimitsAttributes activePowerLimitsAttributes, String operationalLimitsGroupId) {
        var attributes = getResource().getAttributes();
        if (side == TwoSides.ONE) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup1(operationalLimitsGroupId);
            var oldActivePowerLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getActivePowerLimits() : null;
            if (activePowerLimitsAttributes != oldActivePowerLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setActivePowerLimits(activePowerLimitsAttributes));
                index.notifyUpdate(this, "activePowerLimits1", index.getNetwork().getVariantManager().getWorkingVariantId(), oldActivePowerLimits, activePowerLimitsAttributes);
            }
        } else if (side == TwoSides.TWO) {
            var operationalLimitsGroup = attributes.getOperationalLimitsGroup2(operationalLimitsGroupId);
            var oldActivePowerLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getActivePowerLimits() : null;
            if (activePowerLimitsAttributes != oldActivePowerLimits) {
                updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setActivePowerLimits(activePowerLimitsAttributes));
                index.notifyUpdate(this, "activePowerLimits2", index.getNetwork().getVariantManager().getWorkingVariantId(), oldActivePowerLimits, activePowerLimitsAttributes);
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
        loadCurrentLimitsGroup(TwoSides.ONE);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup1();
        return group != null && group.getCurrentLimits() != null
                ? new CurrentLimitsImpl<>(this, TwoSides.ONE, group.getId(), group.getCurrentLimits())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits1() {
        return Optional.ofNullable(getNullableCurrentLimits1());
    }

    @Override
    public CurrentLimits getNullableCurrentLimits2() {
        loadCurrentLimitsGroup(TwoSides.TWO);
        var group = getResource().getAttributes().getSelectedOperationalLimitsGroup2();
        return group != null && group.getCurrentLimits() != null
                ? new CurrentLimitsImpl<>(this, TwoSides.TWO, group.getId(), group.getCurrentLimits())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits2() {
        return Optional.ofNullable(getNullableCurrentLimits2());
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups1() {
        return getResource().getAttributes().getOperationalLimitsGroups1().values().stream()
                .map(group -> new OperationalLimitsGroupImpl<>(this, TwoSides.ONE, group))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId1() {
        return Optional.ofNullable(getResource().getAttributes().getSelectedOperationalLimitsGroupId1());
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id) {
        return getOperationalLimitsGroups1().stream()
                .filter(group -> group.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup1() {
        return getSelectedOperationalLimitsGroupId1().flatMap(this::getOperationalLimitsGroup1);
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup1(String id) {
        var resource = getResource();
        var group = OperationalLimitsGroupAttributes.builder().id(id).build();
        resource.getAttributes().getOperationalLimitsGroups1().put(id, group);
        return new OperationalLimitsGroupImpl<>(this, TwoSides.ONE, group);
    }

    @Override
    public void setSelectedOperationalLimitsGroup1(String id) {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId1();
        if (!id.equals(oldValue)) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId1(id));
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID1, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, id);
        }
    }

    @Override
    public void removeOperationalLimitsGroup1(String id) {
        var resource = getResource();
        if (resource.getAttributes().getOperationalLimitsGroups1().get(id) == null) {
            throw new IllegalArgumentException("Operational limits group '" + id + "' does not exist");
        }

        if (id.equals(resource.getAttributes().getSelectedOperationalLimitsGroupId1())) {
            resource.getAttributes().setSelectedOperationalLimitsGroupId1(null);
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID1, index.getNetwork().getVariantManager().getWorkingVariantId(), id, null);
            index.removeOperationalLimitsGroupAttributes(resource.getType(), getId(), id, 1);
        }
        updateResource(res -> res.getAttributes().getOperationalLimitsGroups1().remove(id));
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup1() {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId1();
        if (oldValue != null) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId1(null));
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID1, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, null);
        }
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups2() {
        return getResource().getAttributes().getOperationalLimitsGroups2().values().stream()
                .map(group -> new OperationalLimitsGroupImpl<>(this, TwoSides.TWO, group))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId2() {
        return Optional.ofNullable(getResource().getAttributes().getSelectedOperationalLimitsGroupId2());
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id) {
        return getOperationalLimitsGroups2().stream()
                .filter(group -> group.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup2() {
        return getSelectedOperationalLimitsGroupId2().flatMap(this::getOperationalLimitsGroup2);
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup2(String id) {
        var resource = getResource();
        var group = OperationalLimitsGroupAttributes.builder().id(id).build();
        resource.getAttributes().getOperationalLimitsGroups2().put(id, group);
        return new OperationalLimitsGroupImpl<>(this, TwoSides.TWO, group);
    }

    @Override
    public void setSelectedOperationalLimitsGroup2(String id) {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId2();
        if (!id.equals(oldValue)) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId2(id));
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID2, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, id);
        }
    }

    @Override
    public void removeOperationalLimitsGroup2(String id) {
        var resource = getResource();
        if (resource.getAttributes().getOperationalLimitsGroups2().get(id) == null) {
            throw new IllegalArgumentException("Operational limits group '" + id + "' does not exist");
        }
        if (id.equals(resource.getAttributes().getSelectedOperationalLimitsGroupId2())) {
            resource.getAttributes().setSelectedOperationalLimitsGroupId2(null);
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID2, index.getNetwork().getVariantManager().getWorkingVariantId(), id, null);
            index.removeOperationalLimitsGroupAttributes(resource.getType(), getId(), id, 2);
        }
        updateResource(res -> res.getAttributes().getOperationalLimitsGroups2().remove(id));

    }

    @Override
    public void cancelSelectedOperationalLimitsGroup2() {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId2();
        if (oldValue != null) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId2(null));
            index.notifyUpdate(this, SELECTED_OPERATIONAL_LIMITS_GROUP_ID2, index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, null);
        }
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
    public Overload checkTemporaryLimits(TwoSides side, double limitReduction, LimitType type) {
        Objects.requireNonNull(side);
        return switch (side) {
            case ONE -> this.checkTemporaryLimits1(limitReduction, type);
            case TWO -> this.checkTemporaryLimits2(limitReduction, type);
        };
    }

    @Override
    public Overload checkTemporaryLimits1(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.ONE, limitReduction, this.getValueForLimit(this.getTerminal1(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits1(LimitType type) {
        return this.checkTemporaryLimits1(1.0F, type);
    }

    @Override
    public Overload checkTemporaryLimits2(double limitReduction, LimitType type) {
        return LimitViolationUtils.checkTemporaryLimits(this, TwoSides.TWO, limitReduction, this.getValueForLimit(this.getTerminal2(), type), type);
    }

    @Override
    public Overload checkTemporaryLimits2(LimitType type) {
        return this.checkTemporaryLimits2(1.0F, type);
    }

    @Override
    public boolean checkPermanentLimit(TwoSides side, double limitReduction, LimitType type) {
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

    @Override
    public List<Terminal> getTerminals(ThreeSides side) {
        if (side == null) {
            return Arrays.asList(terminal1, terminal2);
        } else {
            return switch (side) {
                case ONE -> Collections.singletonList(terminal1);
                case TWO -> Collections.singletonList(terminal2);
                default -> Collections.emptyList();
            };
        }
    }

    private void loadOperationalLimitsGroup(TwoSides side) {
        String groupId;
        if (side == TwoSides.ONE) {
            groupId = getResource().getAttributes().getSelectedOperationalLimitsGroupId1();
        } else if (side == TwoSides.TWO) {
            groupId = getResource().getAttributes().getSelectedOperationalLimitsGroupId2();
        } else {
            throw new PowsyblException("can not load limits on branch for a side null");
        }
        if (groupId != null) {
            index.loadOperationalLimitsGroupAttributes(ResourceType.convert(getType()), getId(), groupId, side.getNum());
        }
    }

    private void loadCurrentLimitsGroup(TwoSides side) {
        String groupId;
        if (side == TwoSides.ONE) {
            groupId = getResource().getAttributes().getSelectedOperationalLimitsGroupId1();
        } else if (side == TwoSides.TWO) {
            groupId = getResource().getAttributes().getSelectedOperationalLimitsGroupId2();
        } else {
            throw new PowsyblException("can not load limits on branch for a side null");
        }
        if (groupId != null) {
            index.loadCurrentLimitsGroupAttributes(ResourceType.convert(getType()), getId(), groupId, side.getNum());
        }
    }
}
