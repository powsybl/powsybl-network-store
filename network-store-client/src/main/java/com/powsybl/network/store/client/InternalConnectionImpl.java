/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class InternalConnectionImpl implements VoltageLevel.NodeBreakerView.InternalConnection {

    private final InternalConnectionAttributes attributes;

    public InternalConnectionImpl(InternalConnectionAttributes attributes) {
        this.attributes = attributes;
    }

    static InternalConnectionImpl create(InternalConnectionAttributes attributes) {
        return new InternalConnectionImpl(attributes);
    }

    @Override
    public int getNode1() {
        return attributes.getNode1();
    }

    @Override
    public int getNode2() {
        return attributes.getNode2();
    }

}
