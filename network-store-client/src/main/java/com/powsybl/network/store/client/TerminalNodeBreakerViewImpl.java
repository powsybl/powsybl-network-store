/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalNodeBreakerViewImpl<T extends IdentifiableAttributes, U extends InjectionAttributes> implements Terminal.NodeBreakerView {

    private final Resource<T> resource;

    private final Function<T, U> attributesAdapter;

    TerminalNodeBreakerViewImpl(Resource<T> resource, Function<T, U> attributesAdapter) {
        this.resource = resource;
        this.attributesAdapter = attributesAdapter;
    }

    @Override
    public int getNode() {
        return attributesAdapter.apply(resource.getAttributes()).getNode();
    }
}
