/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
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
        return index.getVoltageLevel(resource.getAttributes().getVoltageLevelId()).orElseThrow(AssertionError::new);
    }

    @Override
    public SwitchKind getKind() {
        return resource.getAttributes().getKind();
    }

    int getNode1() {
        return resource.getAttributes().getNode1();
    }

    int getNode2() {
        return resource.getAttributes().getNode2();
    }

    String getBus1() {
        return resource.getAttributes().getBus1();
    }

    String getBus2() {
        return resource.getAttributes().getBus2();
    }

    @Override
    public boolean isOpen() {
        return resource.getAttributes().isOpen();
    }

    @Override
    public void setOpen(boolean open) {
        boolean wasOpen = resource.getAttributes().isOpen();
        if (open != wasOpen) {
            resource.getAttributes().setOpen(open);

            // invalidate calculated buses
            getVoltageLevel().invalidateCalculatedBuses();
        }
    }

    @Override
    public boolean isRetained() {
        return resource.getAttributes().isRetained();
    }

    @Override
    public void setRetained(boolean retained) {
        resource.getAttributes().setRetained(retained);
    }

    @Override
    public boolean isFictitious() {
        return resource.getAttributes().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        resource.getAttributes().setFictitious(fictitious);
    }
}
