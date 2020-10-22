/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.InjectionAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TerminalBusBreakerViewImpl<U extends InjectionAttributes> implements Terminal.BusBreakerView {

    private final NetworkObjectIndex index;

    private final U attributes;

    public TerminalBusBreakerViewImpl(NetworkObjectIndex index, U attributes) {
        this.index = index;
        this.attributes = attributes;
    }

    private void checkTopologyKind() {
        if (attributes.getNode() != null) {
            throw new PowsyblException("Not supported in a node breaker topology");
        }
    }

    @Override
    public Bus getBus() {
        checkTopologyKind();
        String busId = attributes.getBus();
        System.out.println("PROUT");
        System.out.println(busId);
        return busId != null ? index.getBus(busId).orElseThrow(() -> new AssertionError(busId + " not found")) : null;
    }

    @Override
    public Bus getConnectableBus() {
        checkTopologyKind();
        return index.getBus(attributes.getConnectableBus()).orElseThrow(AssertionError::new);
    }

    @Override
    public void setConnectableBus(String busId) {
        checkTopologyKind();
        throw new UnsupportedOperationException("TODO");
    }
}
