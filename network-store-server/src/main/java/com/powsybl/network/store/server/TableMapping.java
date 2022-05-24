/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.ResourceType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Table mapping.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TableMapping {

    private final ResourceType resourceType;

    private final Supplier<IdentifiableAttributes> attributesSupplier;

    private final Map<String, Mapping> columnMapping = new LinkedHashMap<>();

    public TableMapping(ResourceType resourceType, Supplier<IdentifiableAttributes> attributesSupplier) {
        this.resourceType = Objects.requireNonNull(resourceType);
        this.attributesSupplier = Objects.requireNonNull(attributesSupplier);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Supplier<IdentifiableAttributes> getAttributesSupplier() {
        return attributesSupplier;
    }

    public Map<String, Mapping> getColumnMapping() {
        return columnMapping;
    }

    public void addColumnMapping(String name, Mapping mapping) {
        columnMapping.put(name, mapping);
    }
}
