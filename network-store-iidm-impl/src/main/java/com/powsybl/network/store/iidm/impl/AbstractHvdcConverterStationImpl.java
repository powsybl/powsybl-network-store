/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractHvdcConverterStationImpl<I extends HvdcConverterStation<I>, D extends InjectionAttributes> extends AbstractInjectionImpl<I, D> {

    protected AbstractHvdcConverterStationImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
    }

    public ConnectableType getType() {
        return ConnectableType.HVDC_CONVERTER_STATION;
    }

    public HvdcLine getHvdcLine() {
        // TODO: to optimize later on, this won't work with a lot of HVDC lines
        return index.getHvdcLines()
                .stream()
                .filter(hvdcLine -> hvdcLine.getConverterStation1().getId().equals(getId())
                        || hvdcLine.getConverterStation2().getId().equals(getId()))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
