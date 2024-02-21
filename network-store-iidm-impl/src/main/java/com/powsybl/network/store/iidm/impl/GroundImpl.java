/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.function.Predicate;

import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.Switch;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class GroundImpl extends AbstractInjectionImpl<Ground, InjectionAttributes> implements Ground {

    public GroundImpl(NetworkObjectIndex index, Resource<InjectionAttributes> resource) {
        super(index, resource);
    }

    static GroundImpl create(NetworkObjectIndex index, Resource<InjectionAttributes> resource) {
        return new GroundImpl(index, resource);
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        //index.removeGround(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public boolean connect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public boolean disconnect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disconnect'");
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disconnect'");
    }

    @Override
    protected Ground getInjection() {
        return this;
    }

}
