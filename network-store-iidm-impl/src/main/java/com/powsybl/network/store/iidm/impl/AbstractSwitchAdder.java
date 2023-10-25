/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractSwitchAdder<T extends AbstractSwitchAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Resource<VoltageLevelAttributes> voltageLevelResource;

    private boolean open = false;

    AbstractSwitchAdder(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index, String parentNetwork) {
        super(index, parentNetwork);
        this.voltageLevelResource = voltageLevelResource;
    }

    protected Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return voltageLevelResource;
    }

    protected boolean isOpen() {
        return open;
    }

    public T setOpen(boolean open) {
        this.open = open;
        return (T) this;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.SWITCH.getDescription();
    }

    protected void invalidateCalculatedBuses() {
        index.getVoltageLevel(voltageLevelResource.getId())
                .orElseThrow(AssertionError::new)
                .invalidateCalculatedBuses();
    }
}
