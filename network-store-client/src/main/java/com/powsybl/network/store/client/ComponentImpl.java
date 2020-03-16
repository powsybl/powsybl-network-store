/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.ComponentConstants;

import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentImpl implements Component {

    @Override
    public int getNum() {
        return ComponentConstants.MAIN_NUM; // TODO
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<Bus> getBuses() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Stream<Bus> getBusStream() {
        throw new UnsupportedOperationException("TODO");
    }
}
