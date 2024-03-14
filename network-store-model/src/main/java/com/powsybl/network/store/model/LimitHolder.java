/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public interface LimitHolder {

    LimitsAttributes getCurrentLimits(int side, String operationalLimitsGroupId);

    LimitsAttributes getApparentPowerLimits(int side, String operationalLimitsGroupId);

    LimitsAttributes getActivePowerLimits(int side, String operationalLimitsGroupId);

    Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups(int side);

    void setCurrentLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    void setApparentPowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    void setActivePowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    @JsonIgnore
    List<Integer> getSideList();

    String EXCEPTION_UNKNOWN_SIDE = "Unknown side";

}
