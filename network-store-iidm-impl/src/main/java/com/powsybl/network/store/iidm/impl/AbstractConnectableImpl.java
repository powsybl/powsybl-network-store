/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.network.store.model.IdentifiableAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractConnectableImpl<I extends Connectable<I>, D extends IdentifiableAttributes> extends AbstractIdentifiableImpl<I, D> implements Connectable<I>  {

    AbstractConnectableImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
    }

    @Override
    public boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return connect(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminals(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnect() {
        return disconnect(SwitchPredicates.IS_CLOSED_BREAKER);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return disconnect(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminals(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    abstract List<Terminal> getTerminals(ThreeSides side);
}
