/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.ReactiveLimitsKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public interface ReactiveCapabilityCurveHolder {

    ReactiveLimitsAttributes getReactiveLimits();

    void setReactiveLimits(ReactiveLimitsAttributes limits);

    @JsonIgnore
    default List<ReactiveCapabilityCurvePointAttributes> getAllReactiveCapabilityCurvePoints() {

        ReactiveLimitsAttributes reactiveLimits = getReactiveLimits();
        if (reactiveLimits != null
                && reactiveLimits.getKind() == ReactiveLimitsKind.CURVE
                && ((ReactiveCapabilityCurveAttributes) reactiveLimits).getPoints() != null) {

            return new ArrayList<>(((ReactiveCapabilityCurveAttributes) reactiveLimits).getPoints().values());
        }
        return Collections.emptyList();
    }
}
