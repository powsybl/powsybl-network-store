/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "extensionName"
)
@JsonTypeIdResolver(ExtensionAttributesIdResolver.class)
public interface ExtensionAttributes {
    // This property is used to not persist some extensions that are only used at import/export.
    // This workaround can be removed when private importers do not add private extensions to build open source classes.
    @JsonIgnore
    default boolean isPersisted() {
        return true;
    }
}
