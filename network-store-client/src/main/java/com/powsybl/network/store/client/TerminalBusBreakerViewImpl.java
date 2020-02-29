/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.function.Function;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TerminalBusBreakerViewImpl<T extends IdentifiableAttributes, U extends InjectionAttributes> implements Terminal.BusBreakerView {

    private final NetworkObjectIndex index;

    private final Resource<T> resource;

    private final Function<T, U> attributesAdapter;

    public TerminalBusBreakerViewImpl(NetworkObjectIndex index, Resource<T> resource, Function<T, U> attributesAdapter) {
        this.index = index;
        this.resource = resource;
        this.attributesAdapter = attributesAdapter;
    }

    @Override
    public Bus getBus() {
        String busId = attributesAdapter.apply(resource.getAttributes()).getBus();
        return busId != null ? index.getBus(busId).orElseThrow(AssertionError::new) : null;
    }

    @Override
    public Bus getConnectableBus() {
        return index.getBus(attributesAdapter.apply(resource.getAttributes()).getConnectableBus()).orElseThrow(AssertionError::new);
    }

    @Override
    public void setConnectableBus(String busId) {
        throw new UnsupportedOperationException("TODO");
    }
}
