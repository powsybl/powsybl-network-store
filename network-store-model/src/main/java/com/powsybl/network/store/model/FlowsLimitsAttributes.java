/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
public interface FlowsLimitsAttributes {

    Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups();

    String getSelectedOperationalLimitsGroupId();

    default OperationalLimitsGroupAttributes getOperationalLimitsGroup(String id) {
        return getOperationalLimitsGroups().get(id);
    }

    default OperationalLimitsGroupAttributes getOrCreateOperationalLimitsGroup(String id) {
        return getOperationalLimitsGroups().computeIfAbsent(id, s -> new OperationalLimitsGroupAttributes(id, null, null, null, null));
    }

    @JsonIgnore
    default OperationalLimitsGroupAttributes getSelectedOperationalLimitsGroup() {
        return getOperationalLimitsGroup(getSelectedOperationalLimitsGroupId());
    }

    void setSelectedOperationalLimitsGroupId(String id);

}
