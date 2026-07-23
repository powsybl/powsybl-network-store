/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlImpl extends AbstractExtension<HvdcLine> implements HvdcAngleDroopActivePowerControl {

    public HvdcAngleDroopActivePowerControlImpl(HvdcLineImpl hvdcLine) {
        super(Objects.requireNonNull(hvdcLine));
    }

    private HvdcLineImpl getHvdcLine() {
        return (HvdcLineImpl) getExtendable();
    }

    private HvdcAngleDroopActivePowerControlAttributes getHvdcAngleDroopActivePowerControlAttributes() {
        return getHvdcLine().getResource().getAttributes().getHvdcAngleDroopActivePowerControl();
    }

    @Override
    public float getP0() {
        return getHvdcAngleDroopActivePowerControlAttributes().getP0();
    }

    @Override
    public float getDroop() {
        return getHvdcAngleDroopActivePowerControlAttributes().getDroop();
    }

    @Override
    public boolean isEnabled() {
        return getHvdcAngleDroopActivePowerControlAttributes().isEnabled();
    }

    @Override
    public HvdcAngleDroopActivePowerControl setP0(float p0) {
        float oldValue = getP0();
        if (oldValue != p0) {
            getHvdcLine().updateResourceExtension(this, res -> res.getAttributes().getHvdcAngleDroopActivePowerControl().setP0(checkP0(p0)), "p0", oldValue, p0);
        }
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setDroop(float droop) {
        float oldValue = getDroop();
        if (oldValue != droop) {
            getHvdcLine().updateResourceExtension(this, res -> res.getAttributes().getHvdcAngleDroopActivePowerControl().setDroop(checkDroop(droop)), "droop", oldValue, droop);
        }
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControl setEnabled(boolean enabled) {
        boolean oldValue = isEnabled();
        if (oldValue != enabled) {
            getHvdcLine().updateResourceExtension(this, res -> res.getAttributes().getHvdcAngleDroopActivePowerControl().setEnabled(enabled), "enabled", oldValue, enabled);
        }
        return this;
    }

    private float checkP0(float p0) {
        if (Float.isNaN(p0)) {
            throw new IllegalArgumentException(String.format("p0 value (%s) is invalid for HVDC line %s",
                    p0,
                    getHvdcLine().getId()));
        }
        return p0;
    }

    private float checkDroop(float droop) {
        if (Float.isNaN(droop)) {
            throw new IllegalArgumentException(String.format("droop value (%s) is invalid for HVDC line %s",
                    droop,
                    getHvdcLine().getId()));
        }
        return droop;
    }
}
