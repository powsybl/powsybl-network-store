/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SwitchImpl extends AbstractIdentifiableImpl<Switch, SwitchAttributes> implements Switch {

    public SwitchImpl(NetworkObjectIndex index, Resource<SwitchAttributes> resource) {
        super(index, resource);
    }

    static SwitchImpl create(NetworkObjectIndex index, Resource<SwitchAttributes> resource) {
        return new SwitchImpl(index, resource);
    }

    @Override
    public VoltageLevelImpl getVoltageLevel() {
        return index.getVoltageLevel(getResource().getAttributes().getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public SwitchKind getKind() {
        return getResource().getAttributes().getKind();
    }

    int getNode1() {
        return getResource().getAttributes().getNode1();
    }

    int getNode2() {
        return getResource().getAttributes().getNode2();
    }

    String getBus1() {
        return getResource().getAttributes().getBus1();
    }

    String getBus2() {
        return getResource().getAttributes().getBus2();
    }

    @Override
    public boolean isOpen() {
        return getResource().getAttributes().isOpen();
    }

    @Override
    public void setOpen(boolean open) {
        var resource = getResource();
        boolean wasOpen = resource.getAttributes().isOpen();
        if (open != wasOpen) {
            updateResource(r -> r.getAttributes().setOpen(open),
                "open", wasOpen, open);
            // invalidate calculated buses
            getVoltageLevel().invalidateCalculatedBuses();
        }
    }

    @Override
    public boolean isRetained() {
        return getResource().getAttributes().isRetained();
    }

    @Override
    public void setRetained(boolean retained) {
        if (getVoltageLevel().getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        boolean oldValue = getResource().getAttributes().isRetained();
        if (retained != oldValue) {
            updateResource(r -> r.getAttributes().setRetained(retained),
                "retained", oldValue, retained);
            // invalidate calculated buses
            getVoltageLevel().invalidateCalculatedBuses();
        }
    }
}
