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
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlAdderImpl extends AbstractIidmExtensionAdder<HvdcLine, HvdcAngleDroopActivePowerControl>
        implements HvdcAngleDroopActivePowerControlAdder {

    private float p0;

    private float droop;

    private boolean enabled;

    public HvdcAngleDroopActivePowerControlAdderImpl(HvdcLine hvdcLine) {
        super(hvdcLine);
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withP0(float p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withDroop(float droop) {
        this.droop = droop;
        return this;
    }

    @Override
    public HvdcAngleDroopActivePowerControlAdder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    protected HvdcAngleDroopActivePowerControl createExtension(HvdcLine hvdcLine) {
        ((HvdcLineImpl) hvdcLine).updateResource(res -> res.getAttributes().setHvdcAngleDroopActivePowerControl(
                HvdcAngleDroopActivePowerControlAttributes.builder()
                        .p0(p0)
                        .droop(droop)
                        .enabled(enabled)
                        .build()));
        return new HvdcAngleDroopActivePowerControlImpl((HvdcLineImpl) hvdcLine);
    }
}
