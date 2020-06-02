/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractAttributes {

    @ToString.Exclude
    @JsonIgnore
    private Resource resource;

    public AbstractAttributes(AbstractAttributes other) {
        this.resource = other.resource;
    }

    @SuppressWarnings("unused")
    public void setDirty() {
        if (resource != null) {
            // notify the store client that the resource has changed
            resource.getResourceUpdater().updateResource(resource.getNetworkUuid(), resource);
        }
    }
}
