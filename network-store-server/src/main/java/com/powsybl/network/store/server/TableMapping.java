/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Table mapping.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TableMapping {

    private final String table;

    private final ResourceType resourceType;

    private final Supplier<IdentifiableAttributes> attributesSupplier;

    private final Resource.Builder<? extends IdentifiableAttributes> resourceBuilder;

    private final Set<String> voltageLevelIdColumns;

    private final Map<String, Mapping> columnMapping = new LinkedHashMap<>();

    public TableMapping(String table, ResourceType resourceType, Resource.Builder<? extends IdentifiableAttributes> resourceBuilder,
                        Supplier<IdentifiableAttributes> attributesSupplier, Set<String> voltageLevelIdColumns) {
        this.table = Objects.requireNonNull(table);
        this.resourceType = Objects.requireNonNull(resourceType);
        this.resourceBuilder = Objects.requireNonNull(resourceBuilder);
        this.attributesSupplier = Objects.requireNonNull(attributesSupplier);
        this.voltageLevelIdColumns = Objects.requireNonNull(voltageLevelIdColumns);
    }

    public String getTable() {
        return table;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Resource.Builder<? extends IdentifiableAttributes> getResourceBuilder() {
        return resourceBuilder;
    }

    public Supplier<IdentifiableAttributes> getAttributesSupplier() {
        return attributesSupplier;
    }

    public Set<String> getVoltageLevelIdColumns() {
        return voltageLevelIdColumns;
    }

    public Map<String, Mapping> getColumnMapping() {
        return columnMapping;
    }

    public void addColumnMapping(String name, ColumnMapping columnMapping) {
        this.columnsMapping.put(name, columnMapping);
    }
}
