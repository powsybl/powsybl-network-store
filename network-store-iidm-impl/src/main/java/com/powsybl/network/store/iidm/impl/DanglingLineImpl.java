/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;
import com.powsybl.network.store.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DanglingLineImpl extends AbstractInjectionImpl<DanglingLine, DanglingLineAttributes> implements DanglingLine, LimitsOwner<Void> {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private final DanglingLineImpl danglingLine;

        GenerationImpl(DanglingLineImpl danglingLine) {
            this.danglingLine = Objects.requireNonNull(danglingLine);
        }

        private static DanglingLineGenerationAttributes getAttributes(Resource<DanglingLineAttributes> resource) {
            return resource.getAttributes().getGeneration();
        }

        private DanglingLineGenerationAttributes getAttributes() {
            return getAttributes(danglingLine.getResource());
        }

        @Override
        public double getTargetP() {
            return getAttributes().getTargetP();
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            ValidationUtil.checkActivePowerSetpoint(danglingLine, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS, danglingLine.getNetwork().getReportNodeContext().getReportNode());
            double oldValue = getAttributes().getTargetP();
            if (targetP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetP(targetP),
                    "targetP", oldValue, targetP);
            }
            return this;
        }

        @Override
        public double getMaxP() {
            return getAttributes().getMaxP();
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            ValidationUtil.checkMaxP(danglingLine, maxP);
            ValidationUtil.checkActivePowerLimits(danglingLine, getMinP(), maxP);
            double oldValue = getAttributes().getMaxP();
            if (maxP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setMaxP(maxP),
                    "maxP", oldValue, maxP);
            }
            return this;
        }

        @Override
        public double getMinP() {
            return getAttributes().getMinP();
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            ValidationUtil.checkMinP(danglingLine, minP);
            ValidationUtil.checkActivePowerLimits(danglingLine, minP, getMaxP());
            double oldValue = getAttributes().getMinP();
            if (minP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setMinP(minP),
                    "minP", oldValue, minP);
            }
            return this;
        }

        @Override
        public double getTargetQ() {
            return getAttributes().getTargetQ();
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), getTargetV(), targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS, danglingLine.getNetwork().getReportNodeContext().getReportNode());
            double oldValue = getAttributes().getTargetQ();
            if (targetQ != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetQ(targetQ),
                    "targetQ", oldValue, targetQ);
            }
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return getAttributes().isVoltageRegulationOn();
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn, getTargetV(), getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS, danglingLine.getNetwork().getReportNodeContext().getReportNode());
            boolean oldValue = getAttributes().isVoltageRegulationOn();
            if (voltageRegulationOn != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setVoltageRegulationOn(voltageRegulationOn),
                    "voltageRegulationOn", oldValue, voltageRegulationOn);
            }
            return this;
        }

        @Override
        public double getTargetV() {
            return getAttributes().getTargetV();
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), targetV, getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS, danglingLine.getNetwork().getReportNodeContext().getReportNode());
            double oldValue = getAttributes().getTargetV();
            if (targetV != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetV(targetV),
                    "targetV", oldValue, targetV);
            }
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdderImpl<GenerationImpl> newReactiveCapabilityCurve() {
            return new ReactiveCapabilityCurveAdderImpl<>(this);
        }

        @Override
        public MinMaxReactiveLimitsAdderImpl<GenerationImpl> newMinMaxReactiveLimits() {
            return new MinMaxReactiveLimitsAdderImpl<>(this);
        }

        @Override
        public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
            ReactiveLimitsAttributes oldValue = getAttributes().getReactiveLimits();
            danglingLine.updateResource(res -> getAttributes(res).setReactiveLimits(reactiveLimits),
                "reactiveLimits", oldValue, reactiveLimits);
        }

        @Override
        public ReactiveLimits getReactiveLimits() {
            ReactiveLimitsAttributes reactiveLimits = getAttributes().getReactiveLimits();
            if (reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX) {
                return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimits);
            } else {
                return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimits);
            }
        }

        @Override
        public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
            ReactiveLimits reactiveLimits = getReactiveLimits();
            if (type == null) {
                throw new IllegalArgumentException("type is null");
            }
            if (type.isInstance(reactiveLimits)) {
                return type.cast(reactiveLimits);
            } else {
                throw new ValidationException(this, "incorrect reactive limits type " + type.getName() + ", expected " + reactiveLimits.getClass());
            }
        }

        @Override
        public String getMessageHeader() {
            return "generation part for dangling line '" + danglingLine.getId() + "': ";
        }
    }

    private static final String DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "DEFAULT";
    private static final String SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "selectedOperationalLimitsGroupId";

    private final DanglingLineBoundaryImpl boundary;

    public DanglingLineImpl(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        super(index, resource);
        boundary = new DanglingLineBoundaryImpl(this);
    }

    static DanglingLineImpl create(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        return new DanglingLineImpl(index, resource);
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
        index.getAreas().forEach(area -> area.removeAreaBoundary(new DanglingLineBoundaryImpl(this)));
        index.removeDanglingLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    protected DanglingLine getInjection() {
        return this;
    }

    @Override
    public boolean isPaired() {
        return getTieLine().isPresent();
    }

    @Override
    public double getP0() {
        return getResource().getAttributes().getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getP0();
        if (p0 != oldValue) {
            updateResource(res -> res.getAttributes().setP0(p0),
                "p0", oldValue, p0);
        }
        return this;
    }

    @Override
    public double getQ0() {
        return getResource().getAttributes().getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getQ0();
        if (q0 != oldValue) {
            updateResource(res -> res.getAttributes().setQ0(q0),
                "q0", oldValue, q0);
        }
        return this;
    }

    @Override
    public double getR() {
        return getResource().getAttributes().getR();
    }

    @Override
    public DanglingLine setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = getResource().getAttributes().getR();
        if (r != oldValue) {
            updateResource(res -> res.getAttributes().setR(r),
                "r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getResource().getAttributes().getX();
    }

    @Override
    public DanglingLine setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = getResource().getAttributes().getX();
        if (x != oldValue) {
            updateResource(res -> res.getAttributes().setX(x),
                "x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getG() {
        return getResource().getAttributes().getG();
    }

    @Override
    public DanglingLine setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = getResource().getAttributes().getG();
        if (g != oldValue) {
            updateResource(res -> res.getAttributes().setG(g),
                "g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getB() {
        return getResource().getAttributes().getB();
    }

    @Override
    public DanglingLine setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = getResource().getAttributes().getB();
        if (b != oldValue) {
            updateResource(res -> res.getAttributes().setB(b),
                "b", oldValue, b);
        }
        return this;
    }

    @Override
    public DanglingLine.Generation getGeneration() {
        var resource = getResource();
        if (resource.getAttributes().getGeneration() != null) {
            return new GenerationImpl(this);
        }
        return null;
    }

    @Override
    public String getPairingKey() {
        return getResource().getAttributes().getPairingKey();
    }

    @Override
    public DanglingLine setPairingKey(String s) {
        if (this.isPaired()) {
            throw new ValidationException(this, "pairing key cannot be set if dangling line is paired.");
        } else {
            String oldValue = getPairingKey();
            if (!StringUtils.equals(s, oldValue)) {
                updateResource(res -> res.getAttributes().setPairingKey(s),
                    "pairingKey", oldValue, s);
            }
        }
        return this;
    }

    private void updateSelectedOperationalLimitsGroupIdIfNull(String id) {
        var resource = getResource();
        if (resource.getAttributes().getSelectedOperationalLimitsGroupId() == null) {
            resource.getAttributes().setSelectedOperationalLimitsGroupId(id);
        }
    }

    @Override
    public void setCurrentLimits(Void side, LimitsAttributes currentLimits, String operationalLimitsGroupId) {
        var operationalLimitsGroup = getResource().getAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
        LimitsAttributes oldLimits = operationalLimitsGroup != null ? operationalLimitsGroup.getCurrentLimits() : null;
        LimitsAttributes newLimits = mergeLimitsAttribute(oldLimits, currentLimits);
        updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setCurrentLimits(newLimits),
            "currentLimits", oldLimits, currentLimits);
    }

    private LimitsAttributes mergeLimitsAttribute(LimitsAttributes oldLimits, LimitsAttributes completeValue) {
        if (oldLimits == null || completeValue == null) {
            return completeValue;
        }
        if (!Double.isNaN(completeValue.getPermanentLimit())) {
            oldLimits.setPermanentLimit(completeValue.getPermanentLimit());
        }
        if (completeValue.getTemporaryLimits() != null && !completeValue.getTemporaryLimits().isEmpty()) {
            if (oldLimits.getTemporaryLimits() == null) {
                oldLimits.setTemporaryLimits(completeValue.getTemporaryLimits());
            } else {
                oldLimits.getTemporaryLimits().putAll(completeValue.getTemporaryLimits());
            }
        }
        return oldLimits;

    }

    @Override
    public AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimits getNullableCurrentLimits() {
        var operationalLimitsGroup = getResource().getAttributes().getSelectedOperationalLimitsGroup();
        return operationalLimitsGroup != null && operationalLimitsGroup.getCurrentLimits() != null
                ? new CurrentLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getCurrentLimits())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return Optional.ofNullable(getNullableCurrentLimits());
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits() {
        var operationalLimitsGroup = getResource().getAttributes().getSelectedOperationalLimitsGroup();
        return operationalLimitsGroup != null && operationalLimitsGroup.getActivePowerLimits() != null
                ? new ActivePowerLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getActivePowerLimits())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return Optional.ofNullable(getNullableActivePowerLimits());
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits() {
        var operationalLimitsGroup = getResource().getAttributes().getSelectedOperationalLimitsGroup();
        return operationalLimitsGroup != null && operationalLimitsGroup.getApparentPowerLimits() != null
                ? new ApparentPowerLimitsImpl<>(this, null, operationalLimitsGroup.getId(), operationalLimitsGroup.getApparentPowerLimits())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return Optional.ofNullable(getNullableApparentPowerLimits());
    }

    private String getSelectedLimitsGroupId() {
        return getResource().getAttributes().getSelectedOperationalLimitsGroupId() != null
                ? getResource().getAttributes().getSelectedOperationalLimitsGroupId()
                : DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
        return new CurrentLimitsAdderImpl<>(null, this, getSelectedLimitsGroupId());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
        return new ApparentPowerLimitsAdderImpl<>(null, this, getSelectedLimitsGroupId());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        updateSelectedOperationalLimitsGroupIdIfNull(getSelectedLimitsGroupId());
        return new ActivePowerLimitsAdderImpl<>(null, this, getSelectedLimitsGroupId());
    }

    @Override
    public void setApparentPowerLimits(Void unused, LimitsAttributes apparentPowerLimitsAttributes, String operationalLimitsGroupId) {
        var operationalLimitsGroup = getResource().getAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
        LimitsAttributes oldValue = operationalLimitsGroup != null ? operationalLimitsGroup.getApparentPowerLimits() : null;
        LimitsAttributes newValue = mergeLimitsAttribute(oldValue, apparentPowerLimitsAttributes);
        updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setApparentPowerLimits(newValue),
            "apparentLimits", oldValue, apparentPowerLimitsAttributes);
    }

    @Override
    public void setActivePowerLimits(Void unused, LimitsAttributes activePowerLimitsAttributes, String operationalLimitsGroupId) {
        var operationalLimitsGroup = getResource().getAttributes().getOperationalLimitsGroup(operationalLimitsGroupId);
        LimitsAttributes oldValue = operationalLimitsGroup != null ? operationalLimitsGroup.getActivePowerLimits() : null;
        LimitsAttributes newValue = mergeLimitsAttribute(oldValue, activePowerLimitsAttributes);
        updateResource(res -> res.getAttributes().getOrCreateOperationalLimitsGroup(operationalLimitsGroupId).setActivePowerLimits(newValue),
            "activeLimits", oldValue, activePowerLimitsAttributes);
    }

    @Override
    public Boundary getBoundary() {
        return boundary;
    }

    void setTieLine(TieLineImpl tieLine) {
        var resource = getResource();
        String oldValue = resource.getAttributes().getTieLineId();
        String tieLineId = tieLine != null ? tieLine.getId() : null;
        updateResource(res -> res.getAttributes().setTieLineId(tieLineId),
            "tieLineId", oldValue, tieLineId);
        getTerminal().getVoltageLevel().invalidateCalculatedBuses();
    }

    void removeTieLine() {
        setTieLine(null);
    }

    @Override
    public Optional<TieLine> getTieLine() {
        var resource = getResource();
        return Optional.ofNullable(resource.getAttributes().getTieLineId())
                .flatMap(index::getTieLine);
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
        return getResource().getAttributes().getOperationalLimitsGroups().values().stream()
                .map(group -> new OperationalLimitsGroupImpl<>(this, null, group))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId() {
        return Optional.ofNullable(getResource().getAttributes().getSelectedOperationalLimitsGroupId());
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
        var resource = getResource();
        var group = OperationalLimitsGroupAttributes.builder().id(id).build();
        resource.getAttributes().getOperationalLimitsGroups().put(id, group);
        return new OperationalLimitsGroupImpl<>(this, null, group);
    }

    @Override
    public void setSelectedOperationalLimitsGroup(String id) {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId();
        if (!id.equals(oldValue)) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId(id),
                SELECTED_OPERATIONAL_LIMITS_GROUP_ID, oldValue, id);
        }
    }

    @Override
    public void removeOperationalLimitsGroup(String id) {
        var resource = getResource();
        if (resource.getAttributes().getOperationalLimitsGroups().get(id) == null) {
            throw new IllegalArgumentException("Operational limits group '" + id + "' does not exist");
        }
        if (id.equals(resource.getAttributes().getSelectedOperationalLimitsGroupId())) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId(null),
                SELECTED_OPERATIONAL_LIMITS_GROUP_ID, id, null);
        }
        var oldValue = getResource().getAttributes().getOperationalLimitsGroups();
        updateResource(res -> res.getAttributes().getOperationalLimitsGroups().remove(id),
            "operationalLimitsGroups", oldValue, null);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup() {
        var resource = getResource();
        String oldValue = resource.getAttributes().getSelectedOperationalLimitsGroupId();
        if (oldValue != null) {
            updateResource(res -> res.getAttributes().setSelectedOperationalLimitsGroupId(null),
                SELECTED_OPERATIONAL_LIMITS_GROUP_ID, oldValue, null);
        }
    }
}
