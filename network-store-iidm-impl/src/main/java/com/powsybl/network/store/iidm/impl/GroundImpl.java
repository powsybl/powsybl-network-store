/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Ground;
import com.powsybl.network.store.model.GroundAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class GroundImpl extends AbstractInjectionImpl<Ground, GroundAttributes> implements Ground {

    public GroundImpl(NetworkObjectIndex index, Resource<GroundAttributes> resource) {
        super(index, resource);
    }

    static GroundImpl create(NetworkObjectIndex index, Resource<GroundAttributes> resource) {
        return new GroundImpl(index, resource);
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeGround(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public void setFictitious(boolean fictitious) {
        if (fictitious) {
            throw new PowsyblException("The ground cannot be fictitious.");
        } else {
            getResource().getAttributes().setFictitious(false);
        }
    }

    @Override
    protected Ground getInjection() {
        return this;
    }

}
