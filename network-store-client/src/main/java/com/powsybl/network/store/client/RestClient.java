/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.Attributes;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface RestClient {

    <T extends IdentifiableAttributes> void createAll(String url, List<Resource<T>> resources, Object... uriVariables);

    <T extends IdentifiableAttributes> Optional<Resource<T>> getOne(String target, String url, Object... uriVariables);

    /**
     * Retrieves one extension attributes from the server.
     * @return {@link ExtensionAttributes} which is a subset of an identifiable resource. The extension attributes can be put in the extensionAttributes
     * map of an {@link IdentifiableAttributes} or used to load an extension.
     */
    Optional<ExtensionAttributes> getOneExtensionAttributes(String url, Object... uriVariables);

    <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables);

    <T extends Attributes> void updateAll(String url, List<Resource<T>> resources, Object... uriVariables);

    <E> E get(String url, ParameterizedTypeReference<E> responseType, Object... uriVariables);

    void put(String url, Object... uriVariables);

    void delete(String url, Object... uriVariables);

    void post(String url, Object... uriVariables);

    void deleteAll(String url, List<String> ids, Object... uriVariables);
}
