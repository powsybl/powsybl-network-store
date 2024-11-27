/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@EqualsAndHashCode
@Getter
@Setter
public class RegulatingEquipmentIdentifier {
    private String equipmentId;
    private ResourceType resourceType;

    public RegulatingEquipmentIdentifier(String equipmentId, ResourceType resourceType) {
        this.equipmentId = equipmentId;
        this.resourceType = resourceType;
    }

    public RegulatingEquipmentIdentifier() {

    }
}
