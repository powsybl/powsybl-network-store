/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.List;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
public interface FlowsLimitsAttributes {

    List<OperationalLimitGroupAttributes> getOperationalLimitsGroups();

    String getSelectedOperationalLimitsGroupId();

    default OperationalLimitGroupAttributes getOperationalLimitsGroup(String id) {
        return getOperationalLimitsGroups() != null ? getOperationalLimitsGroups().stream()
                .filter(group -> group.getId().equals(id))
                .findFirst()
                .orElse(null) : null;
    }

    default OperationalLimitGroupAttributes getSelectedOperationalLimitsGroup() {
        return getOperationalLimitsGroup(getSelectedOperationalLimitsGroupId());
    }

    void setSelectedOperationalLimitsGroupId(String id);

}
