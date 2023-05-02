/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.BranchStatusHolder;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusImpl<C extends Connectable<C>> extends AbstractExtension<C> implements BranchStatus<C> {

    public BranchStatusImpl(C connectable) {
        super(connectable);
    }

    @Override
    public Status getStatus() {
        var resource = ((AbstractIdentifiableImpl<?, ?>) getExtendable()).getResource();
        return Status.valueOf(((BranchStatusHolder) resource.getAttributes()).getBranchStatus());
    }

    @Override
    public BranchStatus<C> setStatus(Status status) {
        ((AbstractIdentifiableImpl<?, ?>) getExtendable()).updateResource(res -> ((BranchStatusHolder) res.getAttributes()).setBranchStatus(status.name()));
        return this;
    }
}
