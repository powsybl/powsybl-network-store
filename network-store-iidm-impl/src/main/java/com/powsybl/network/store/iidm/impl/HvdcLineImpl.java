/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HvdcLineImpl extends AbstractIdentifiableImpl<HvdcLine, HvdcLineAttributes> implements HvdcLine {

    public HvdcLineImpl(NetworkObjectIndex index, Resource<HvdcLineAttributes> resource) {
        super(index, resource);
    }

    static HvdcLineImpl create(NetworkObjectIndex index, Resource<HvdcLineAttributes> resource) {
        return new HvdcLineImpl(index, resource);
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return index.getHvdcConverterStation(resource.getAttributes().getConverterStationId1()).orElseThrow(IllegalStateException::new);
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return index.getHvdcConverterStation(resource.getAttributes().getConverterStationId2()).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void remove() {
        index.removeHvdcLine(resource.getId());
    }

    @Override
    public HvdcConverterStation<?> getConverterStation(Side side) {
        switch (side) {
            case ONE:
                return getConverterStation1();
            case TWO:
                return getConverterStation2();
            default:
                throw new IllegalStateException("Unknown side: " + side);
        }
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return resource.getAttributes().getConvertersMode();
    }

    @Override
    public HvdcLine setConvertersMode(ConvertersMode mode) {
        resource.getAttributes().setConvertersMode(mode);
        return this;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public HvdcLine setR(double r) {
        resource.getAttributes().setR(r);
        return this;
    }

    @Override
    public double getNominalV() {
        return resource.getAttributes().getNominalV();
    }

    @Override
    public HvdcLine setNominalV(double nominalV) {
        resource.getAttributes().setNominalV(nominalV);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return resource.getAttributes().getActivePowerSetpoint();
    }

    @Override
    public HvdcLine setActivePowerSetpoint(double activePowerSetpoint) {
        resource.getAttributes().setActivePowerSetpoint(activePowerSetpoint);
        return this;
    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public HvdcLine setMaxP(double maxP) {
        resource.getAttributes().setMaxP(maxP);
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "hvdcLine";
    }
}
