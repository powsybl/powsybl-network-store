/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlImpl implements HvdcAngleDroopActivePowerControl {

    private HvdcLineImpl hvdcLine;

    public HvdcAngleDroopActivePowerControlImpl(HvdcLineImpl hvdcLine) {
        this.hvdcLine = hvdcLine;
    }

    public HvdcAngleDroopActivePowerControlImpl(HvdcLineImpl hvdcLine, float p0, float droop, boolean enabled) {
        this(hvdcLine.initHvdcAngleDroopActivePowerControlAttributes(p0, droop, enabled));
    }

    @Override
    public HvdcLine getExtendable() {
        return hvdcLine;
    }

    @Override
    public void setExtendable(HvdcLine hvdcLine) {
        this.hvdcLine = (HvdcLineImpl) hvdcLine;
    }

    @Override
    public float getP0() {
        return hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().getP0();
    }

    @Override
    public float getDroop() {
        return hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().getDroop();
    }

    @Override
    public boolean isEnabled() {
        return hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().isEnabled();
    }

    @Override
    public HvdcAngleDroopActivePowerControl setP0(float p0) {
        hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().setP0(checkP0(p0));
        hvdcLine.updateResource();
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setDroop(float droop) {
        hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().setDroop(checkDroop(droop));
        hvdcLine.updateResource();
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setEnabled(boolean enabled) {
        hvdcLine.getResource().getAttributes().getHvdcAngleDroopActivePowerControl().setEnabled(enabled);
        hvdcLine.updateResource();
        return this;
    }

    private float checkP0(float p0) {
        if (Float.isNaN(p0)) {
            throw new IllegalArgumentException("p0 is not set");
        }
        return p0;
    }

    private float checkDroop(float droop) {
        if (Float.isNaN(droop)) {
            throw new IllegalArgumentException("droop is not set");
        }
        return droop;
    }
}
