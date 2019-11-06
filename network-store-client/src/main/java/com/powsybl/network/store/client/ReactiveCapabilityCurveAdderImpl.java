/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveCapabilityCurveAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ReactiveCapabilityCurveAdderImpl implements ReactiveCapabilityCurveAdder {

    @Override
    public PointAdder beginPoint() {
        return new PointAdderImpl(this);
    }

    @Override
    public ReactiveCapabilityCurve add() {
        return ReactiveCapabilityCurveImpl.create();
    }
}
