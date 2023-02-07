/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.network.store.client.RestClient;
import lombok.experimental.Delegate;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractForwardingRestClient implements RestClient {

    @Delegate
    private RestClient delegate;

    public AbstractForwardingRestClient(RestClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }
}
