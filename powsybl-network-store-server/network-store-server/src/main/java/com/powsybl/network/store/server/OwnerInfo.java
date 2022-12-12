/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.ResourceType;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Some equipments can have sub-attributes which are represented in a different table as the equipment's.
 * When fetching the equipment from the database, we need to also get the sub-attributes from their respective
 * table sand insert them inside the retrieved equipment.
 * The sub-attributes all have in common some information about their parent equipment, which are represented
 * in this OwnerInfo class.
 * Each sub-attribute is supposed to be linked manually to the equipment with the help of this OwnerInfo class.
 *
 * @author Sylvain Bouzols <sylvain.bouzols at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class OwnerInfo {
    private String equipmentId;

    private ResourceType equipmentType;

    private UUID networkUuid;

    private int variantNum;
}
