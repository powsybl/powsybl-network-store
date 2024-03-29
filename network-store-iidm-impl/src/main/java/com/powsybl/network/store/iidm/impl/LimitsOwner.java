/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.network.store.model.LimitsAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface LimitsOwner<SIDE> extends Validable {

    void setCurrentLimits(SIDE side, LimitsAttributes currentLimits, String operationalLimitsGroupId);

    void setActivePowerLimits(SIDE side, LimitsAttributes activePowerLimitsAttributes, String operationalLimitsGroupId);

    void setApparentPowerLimits(SIDE side, LimitsAttributes apparentPowerLimitsAttributes, String operationalLimitsGroupId);

    AbstractIdentifiableImpl getIdentifiable();
}
