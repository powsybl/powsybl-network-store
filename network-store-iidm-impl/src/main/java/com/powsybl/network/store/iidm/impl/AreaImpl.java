/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.AreaAttributes;
import com.powsybl.network.store.model.AreaBoundaryAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AreaImpl extends AbstractIdentifiableImpl<Area, AreaAttributes> implements Area {

    private static final String AREA_BOUNDARIES = "area_boundaries";

    protected AreaImpl(NetworkObjectIndex index, Resource<AreaAttributes> resource) {
        super(index, resource);
    }

    static AreaImpl create(NetworkObjectIndex index, Resource<AreaAttributes> resource) {
        return new AreaImpl(index, resource);
    }

    @Override
    public String getAreaType() {
        return getResource().getAttributes().getAreaType();
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return getResource()
            .getAttributes()
            .getVoltageLevelIds()
            .stream()
            .map(id -> (VoltageLevel) index.getVoltageLevel(id).orElse(null))
            .filter(Objects::nonNull);
    }

    @Override
    public Area addVoltageLevel(VoltageLevel voltageLevel) {
        Set<String> oldVoltageLevelIds = getResource().getAttributes().getVoltageLevelIds();
        AtomicBoolean isAdded = new AtomicBoolean(false);
        updateResource(r -> isAdded.set(r.getAttributes().getVoltageLevelIds().add(voltageLevel.getId())),
            "VoltageLevelIds", null, oldVoltageLevelIds, this::getVoltageLevels);
        if (isAdded.get()) {
            voltageLevel.addArea(this);
        }
        return this;
    }

    @Override
    public Area removeVoltageLevel(VoltageLevel voltageLevel) {
        Set<String> oldVoltageLevelIds = getResource().getAttributes().getVoltageLevelIds();
        updateResource(r -> r.getAttributes().getVoltageLevelIds().remove(voltageLevel.getId()),
            "VoltageLevelIds", null, oldVoltageLevelIds, this::getVoltageLevels);
        if (Iterables.contains(voltageLevel.getAreas(), this)) {
            voltageLevel.removeArea(this);
        }
        return this;
    }

    @Override
    public AreaBoundaryAdder newAreaBoundary() {
        return new AreaBoundaryAdderImpl(this, index);
    }

    protected void addAreaBoundary(AreaBoundaryImpl areaBoundary) {
        Optional<Terminal> terminal = areaBoundary.getTerminal();
        Optional<Boundary> boundary = areaBoundary.getBoundary();
        AreaBoundaryAttributes.AreaBoundaryAttributesBuilder areaBoundaryBuilder = AreaBoundaryAttributes.builder()
            .ac(areaBoundary.isAc())
            .areaId(areaBoundary.getArea().getId());
        boundary.ifPresent(b -> {
            checkBoundaryNetwork(b.getDanglingLine().getParentNetwork(), "Boundary of DanglingLine" + b.getDanglingLine().getId());
            areaBoundaryBuilder.boundaryDanglingLineId(b.getDanglingLine().getId());
        });
        terminal.ifPresent(t -> {
            checkBoundaryNetwork(t.getConnectable().getParentNetwork(), "Terminal of connectable " + t.getConnectable().getId());
            areaBoundaryBuilder.terminal(TerminalRefUtils.getTerminalRefAttributes(t));
        });
        List<AreaBoundaryAttributes> oldAreaBoundaries = getResource().getAttributes().getAreaBoundaries();
        updateResource(r -> r.getAttributes().getAreaBoundaries().add(areaBoundaryBuilder.build()),
            AREA_BOUNDARIES, null, oldAreaBoundaries, this::getAreaBoundaries);
    }

    void checkBoundaryNetwork(Network network, String boundaryTypeAndId) {
        if (getParentNetwork() != network) {
            throw new PowsyblException(boundaryTypeAndId + " cannot be added to Area " + getId() + " boundaries. It does not belong to the same network or subnetwork.");
        }
    }

    @Override
    public Area removeAreaBoundary(Terminal terminal) {
        Objects.requireNonNull(terminal);
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(terminal);
        List<AreaBoundaryAttributes> oldAreaBoundaries = getResource().getAttributes().getAreaBoundaries();
        updateResource(r -> r.getAttributes().getAreaBoundaries().removeIf(b ->
                Objects.equals(b.getTerminal(), terminalRefAttributes)),
            AREA_BOUNDARIES, null, oldAreaBoundaries, this::getAreaBoundaries);
        return this;
    }

    @Override
    public Area removeAreaBoundary(Boundary boundary) {
        Objects.requireNonNull(boundary);
        List<AreaBoundaryAttributes> oldAreaBoundaries = getResource().getAttributes().getAreaBoundaries();
        updateResource(r -> r.getAttributes().getAreaBoundaries().removeIf(b ->
                Objects.equals(b.getBoundaryDanglingLineId(), boundary.getDanglingLine().getId())),
            AREA_BOUNDARIES, null, oldAreaBoundaries, this::getAreaBoundaries);
        return this;
    }

    @Override
    public AreaBoundary getAreaBoundary(Boundary boundary) {
        Objects.requireNonNull(boundary);
        return getAreaBoundaryStream()
            .filter(ab -> {
                Boundary abBoundary = ab.getBoundary().orElse(null);
                return abBoundary != null && abBoundary.getDanglingLine().getId().equals(boundary.getDanglingLine().getId());
            })
            .findFirst().orElse(null);
    }

    @Override
    public AreaBoundary getAreaBoundary(Terminal terminal) {
        Objects.requireNonNull(terminal);
        return getAreaBoundaryStream()
            .filter(ab -> Objects.equals(ab.getTerminal().orElse(null), terminal))
            .findFirst().orElse(null);
    }

    @Override
    public Iterable<AreaBoundary> getAreaBoundaries() {
        return getAreaBoundaryStream().collect(Collectors.toSet());
    }

    @Override
    public Stream<AreaBoundary> getAreaBoundaryStream() {
        return getResource()
            .getAttributes()
            .getAreaBoundaries()
            .stream()
            .map((AreaBoundaryAttributes areaBoundaryAttributes) -> new AreaBoundaryImpl(areaBoundaryAttributes, index));
    }

    @Override
    public OptionalDouble getInterchangeTarget() {
        double target = getResource().getAttributes().getInterchangeTarget();
        if (Double.isNaN(target)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(target);
    }

    @Override
    public Area setInterchangeTarget(double v) {
        double oldInterchangeTarget = getResource().getAttributes().getInterchangeTarget();
        updateResource(res -> res.getAttributes().setInterchangeTarget(v),
            "interchangeTarget", oldInterchangeTarget, v);
        return this;
    }

    @Override
    public double getAcInterchange() {
        return getInterchange(AreaBoundary::isAc);
    }

    @Override
    public double getDcInterchange() {
        return getInterchange(areaBoundary -> !areaBoundary.isAc());
    }

    @Override
    public double getInterchange() {
        return getInterchange(areaBoundary -> true);
    }

    double getInterchange(Predicate<AreaBoundary> predicate) {
        return getAreaBoundaryStream().filter(predicate).mapToDouble(AreaBoundary::getP).filter(p -> !Double.isNaN(p)).sum();
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        index.removeArea(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}
