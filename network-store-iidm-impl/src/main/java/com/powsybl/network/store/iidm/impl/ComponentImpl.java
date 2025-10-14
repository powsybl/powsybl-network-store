/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.DcBus;
import com.powsybl.iidm.network.Network;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComponentImpl implements Component {

    private final CalculatedBus calculatedBus;

    private final ComponentType componentType;

    ComponentImpl(CalculatedBus calculatedBus, ComponentType componentType) {
        this.calculatedBus = calculatedBus;
        this.componentType = componentType;
    }

    @Override
    public int getNum() {
        return componentType == ComponentType.CONNECTED ? calculatedBus.getConnectedComponentNum()
                : calculatedBus.getSynchronousComponentNum();
    }

    @Override
    public int getSize() {
        return (int) getBusStream().count();
    }

    @Override
    public Iterable<Bus> getBuses() {
        return getBusStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Bus> getBusStream() {
        Network network = calculatedBus.getVoltageLevel().getNetwork();
        Predicate<CalculatedBus> pred =
                componentType == ComponentType.CONNECTED ?
                    b -> b.getConnectedComponentNum() == calculatedBus.getConnectedComponentNum() :
                    b -> b.getSynchronousComponentNum() == calculatedBus.getSynchronousComponentNum();
        return network.getVoltageLevelStream()
                .flatMap(vl -> vl.getBusView().getBusStream())
                .filter(b -> pred.test((CalculatedBus) b));
    }

    @Override
    public Stream<DcBus> getDcBusStream() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterable<DcBus> getDcBuses() {
        throw new UnsupportedOperationException("TODO");
    }
}
