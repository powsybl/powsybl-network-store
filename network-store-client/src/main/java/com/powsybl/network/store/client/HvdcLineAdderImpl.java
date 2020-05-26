/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HvdcLineAdderImpl implements HvdcLineAdder {

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private double r = Double.NaN;

    private HvdcLine.ConvertersMode convertersMode;

    private double nominalV = Double.NaN;

    private double activePowerSetpoint = Double.NaN;

    private double maxP = Double.NaN;

    private String converterStationId1;

    private String converterStationId2;

    public HvdcLineAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public HvdcLineAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public HvdcLineAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public HvdcLineAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public HvdcLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode) {
        this.convertersMode = convertersMode;
        return this;
    }

    @Override
    public HvdcLineAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    @Override
    public HvdcLineAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId1(String converterStationId1) {
        this.converterStationId1 = converterStationId1;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId2(String converterStationId2) {
        this.converterStationId2 = converterStationId2;
        return this;
    }

    @Override
    public HvdcLine add() {
        Resource<HvdcLineAttributes> resource = Resource.hvdcLineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(HvdcLineAttributes.builder()
                        .name(name)
                        .r(r)
                        .convertersMode(convertersMode)
                        .nominalV(nominalV)
                        .activePowerSetpoint(activePowerSetpoint)
                        .maxP(maxP)
                        .converterStationId1(converterStationId1)
                        .converterStationId2(converterStationId2)
                        .build())
                .build();
        return index.createHvdcLine(resource);
    }
}
