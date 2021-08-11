/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface RestClient {

    <T extends IdentifiableAttributes> void create(String url, List<Resource<T>> resources, Object... uriVariables);

    <T extends IdentifiableAttributes> Optional<Resource<T>> getOne(String target, String url, Object... uriVariables);

    <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables);

    <T extends IdentifiableAttributes> void update(String url, Resource<T> resource, Object... uriVariables);

    <T extends IdentifiableAttributes> void updateAll(String url, List<Resource<T>> resources, Object... uriVariables);

    <E> E get(String uri, ParameterizedTypeReference<E> responseType, Object... uriVariables);

    void put(String uri, Object... uriVariables);

    void delete(String url, Object... uriVariables);
}
