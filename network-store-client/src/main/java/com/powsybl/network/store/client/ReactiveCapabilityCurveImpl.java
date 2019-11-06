/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReactiveCapabilityCurveImpl implements ReactiveCapabilityCurve {

    static ReactiveCapabilityCurveImpl create() {
        return new ReactiveCapabilityCurveImpl();
    }

    @Override
    public Collection<Point> getPoints() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getPointCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getMinP() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getMaxP() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ReactiveLimitsKind getKind() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getMinQ(double p) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getMaxQ(double p) {
        throw new UnsupportedOperationException("TODO");
    }
}
