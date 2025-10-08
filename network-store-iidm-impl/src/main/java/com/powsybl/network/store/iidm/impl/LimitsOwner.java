/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Validable;
import com.powsybl.network.store.model.*;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface LimitsOwner<SIDE> extends Validable {

    void setCurrentLimits(SIDE side, LimitsAttributes currentLimits, String operationalLimitsGroupId);

    void setActivePowerLimits(SIDE side, LimitsAttributes activePowerLimitsAttributes, String operationalLimitsGroupId);

    void setApparentPowerLimits(SIDE side, LimitsAttributes apparentPowerLimitsAttributes, String operationalLimitsGroupId);

    AbstractIdentifiableImpl getIdentifiable();

    static <D extends IdentifiableAttributes> void updateOperationalLimitsResource(Resource<D> resource, Identifiable<?> identifiable, Network network, Consumer<Resource<D>> modifier, String attribute, OperationalLimitsGroupAttributes oldValue, OperationalLimitsGroupAttributes newValue, NetworkObjectIndex index) {
        modifier.accept(resource);
        index.updateResource(resource, null);
        String variantId = network.getVariantManager().getWorkingVariantId();
        LimitsAttributes oldCurrentValue = Optional.ofNullable(oldValue).map(OperationalLimitsGroupAttributes::getCurrentLimits).orElse(null);
        LimitsAttributes newCurrentValue = Optional.ofNullable(newValue).map(OperationalLimitsGroupAttributes::getCurrentLimits).orElse(null);
        index.notifyUpdate(identifiable, attribute + "_" + LimitType.CURRENT, variantId, oldCurrentValue, newCurrentValue);

        LimitsAttributes oldActivePowerLimits = Optional.ofNullable(oldValue).map(OperationalLimitsGroupAttributes::getActivePowerLimits).orElse(null);
        LimitsAttributes newActivePowerLimits = Optional.ofNullable(newValue).map(OperationalLimitsGroupAttributes::getActivePowerLimits).orElse(null);
        index.notifyUpdate(identifiable, attribute + "_" + LimitType.ACTIVE_POWER, variantId, oldActivePowerLimits, newActivePowerLimits);

        LimitsAttributes oldApparentPowerLimits = Optional.ofNullable(oldValue).map(OperationalLimitsGroupAttributes::getApparentPowerLimits).orElse(null);
        LimitsAttributes newApparentPowerLimits = Optional.ofNullable(newValue).map(OperationalLimitsGroupAttributes::getApparentPowerLimits).orElse(null);
        index.notifyUpdate(identifiable, attribute + "_" + LimitType.APPARENT_POWER, variantId, oldApparentPowerLimits, newApparentPowerLimits);
    }
}
