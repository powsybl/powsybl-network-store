/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadingLimitsAdderExt<S, OWNER extends LimitsOwner<S>, L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>>
        extends LoadingLimitsAdder<L, A> {

    OWNER getOwner();

    void addTemporaryLimit(TemporaryLimitAttributes temporaryLimitAttribute);

    Map<Integer, TemporaryLimitAttributes> getTemporaryLimits();
}
