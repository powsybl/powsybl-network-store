/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.ResourceType;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public interface RegulatingPoint {
    String getRegulatingEquipmentId();
    ResourceType getRegulatingEquipmentType();
}
