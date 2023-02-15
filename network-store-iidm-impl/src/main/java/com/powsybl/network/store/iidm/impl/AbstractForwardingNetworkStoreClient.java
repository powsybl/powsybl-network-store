/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import lombok.experimental.Delegate;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    @Delegate
    protected final NetworkStoreClient delegate;

    protected AbstractForwardingNetworkStoreClient(NetworkStoreClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }
}
