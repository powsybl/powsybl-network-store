/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class InternalConnectionImpl implements VoltageLevel.NodeBreakerView.InternalConnection {

    private final Resource<InternalConnectionAttributes> resource;

    public InternalConnectionImpl(Resource<InternalConnectionAttributes> resource) {
        this.resource = resource;
    }

    static InternalConnectionImpl create(Resource<InternalConnectionAttributes> resource) {
        return new InternalConnectionImpl(resource);
    }

    @Override
    public int getNode1() {
        return resource.getAttributes().getNode1();
    }

    @Override
    public int getNode2() {
        return resource.getAttributes().getNode2();
    }

}
