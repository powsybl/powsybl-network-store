/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface IdentifiableAttributes<T extends IdentifiableAttributes<T>> {

    String getName();

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    default void setResource(Resource resource) {
    }

    Boolean getFictitious();

    default boolean isFictitious() {
        return Boolean.TRUE.equals(getFictitious());
    }

    void setFictitious(Boolean fictitious);

    default void initUpdatedAttributes(T updatedAttributes) {
    }
}
