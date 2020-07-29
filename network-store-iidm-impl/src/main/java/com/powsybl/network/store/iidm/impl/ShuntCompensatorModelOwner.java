/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Validable;
import com.powsybl.network.store.model.ShuntCompensatorModelAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface ShuntCompensatorModelOwner extends Validable {

    void setModel(ShuntCompensatorModelAttributes model);
}
