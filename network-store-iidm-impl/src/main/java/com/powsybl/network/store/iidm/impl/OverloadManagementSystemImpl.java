/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.List;

import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import java.util.Collections;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class OverloadManagementSystemImpl extends AbstractIdentifiableImpl<OverloadManagementSystem, IdentifiableAttributes> implements OverloadManagementSystem {

    public OverloadManagementSystemImpl(NetworkObjectIndex index, Resource<IdentifiableAttributes> resource) {
        super(index, resource);
    }

    static OverloadManagementSystemImpl create(NetworkObjectIndex index, Resource<IdentifiableAttributes> resource) {
        return new OverloadManagementSystemImpl(index, resource);
    }

    @Override
    public void remove() {
        // FIXME: implement this method
        // do nothing
    }

    @Override
    public boolean isEnabled() {
        // FIXME: implement this method
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // FIXME: implement this method
        // do nothing
    }

    @Override
    public Substation getSubstation() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public String getMonitoredElementId() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public ThreeSides getMonitoredSide() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public void addTripping(Tripping tripping) {
        // FIXME: implement this method
        // do nothing
    }

    @Override
    public List<Tripping> getTrippings() {
        // FIXME: implement this method
        return Collections.emptyList();
    }

}
