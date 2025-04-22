/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.AreaAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AreaAdderImpl extends AbstractIdentifiableAdder<AreaAdderImpl> implements AreaAdder {

    private String areaType;
    private final Map<Terminal, Boolean> terminalAreaBoundaries;
    private final Map<Boundary, Boolean> boundaryAreaBoundaries;
    private final Set<String> voltageLevelIds;
    private double interchangeTarget;

    AreaAdderImpl(NetworkObjectIndex index) {
        super(index);
        terminalAreaBoundaries = new HashMap<>();
        boundaryAreaBoundaries = new HashMap<>();
        voltageLevelIds = new HashSet<>();
        interchangeTarget = Double.NaN;
    }

    @Override
    public AreaAdderImpl setAreaType(String areaType) {
        this.areaType = areaType;
        return this;
    }

    @Override
    public AreaAdder setInterchangeTarget(double v) {
        interchangeTarget = v;
        return this;
    }

    @Override
    public AreaAdder addVoltageLevel(VoltageLevel voltageLevel) {
        voltageLevelIds.add(voltageLevel.getId());
        return this;
    }

    @Override
    public AreaAdder addAreaBoundary(Terminal terminal, boolean ac) {
        terminalAreaBoundaries.put(terminal, ac);
        return this;
    }

    @Override
    public AreaAdder addAreaBoundary(Boundary boundary, boolean ac) {
        boundaryAreaBoundaries.put(boundary, ac);
        return this;
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        Resource<AreaAttributes> resource = Resource.areaBuilder()
            .id(id)
            .variantNum(index.getWorkingVariantNum())
            .attributes(AreaAttributes.builder()
                .name(getName())
                .fictitious(isFictitious())
                .areaType(areaType)
                .interchangeTarget(interchangeTarget)
                .voltageLevelIds(new LinkedHashSet<>())
                .areaBoundaries(new ArrayList<>())
                .build())
            .build();
        AreaImpl area = getIndex().createArea(resource);
        terminalAreaBoundaries.forEach((terminal, ac) -> area.newAreaBoundary().setTerminal(terminal).setAc(ac).add());
        boundaryAreaBoundaries.forEach((boundary, ac) -> area.newAreaBoundary().setBoundary(boundary).setAc(ac).add());
        voltageLevelIds.forEach(voltageLevelId -> index.getVoltageLevel(voltageLevelId)
            .ifPresent(voltageLevel -> voltageLevel.addArea(area)));
        return area;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.AREA.getDescription();
    }

}
