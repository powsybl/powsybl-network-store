/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.network.store.client.RestClient;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestRestClient extends AbstractForwardingRestClient {

    private final RestClientMetrics metrics;

    public TestRestClient(RestClient delegate, RestClientMetrics metrics) {
        super(delegate);
        this.metrics = Objects.requireNonNull(metrics);
    }

    @Override
    public <T extends IdentifiableAttributes> Optional<Resource<T>> getOne(String target, String url, Object... uriVariables) {
        metrics.oneGetterCallCount++;
        return super.getOne(target, url, uriVariables);
    }

    @Override
    public <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        metrics.allGetterCallCount++;
        return super.getAll(target, url, uriVariables);
    }
}
