/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.extensions.BranchStatusAdder;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.BranchStatusHolder;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusAdderImpl<C extends Connectable<C>>
        extends AbstractExtensionAdder<C, BranchStatus<C>> implements BranchStatusAdder<C> {

    private BranchStatus.Status status = BranchStatus.Status.IN_OPERATION;

    protected BranchStatusAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected BranchStatus<C> createExtension(C extendable) {
        ((AbstractIdentifiableImpl<?, ?>) extendable).updateResource(res -> {
            if (!(res.getAttributes() instanceof BranchStatusHolder)) {
                throw new IllegalStateException("Not a branch status holder");
            }
            ((BranchStatusHolder) res.getAttributes()).setBranchStatus(status.name());
        });
        return new BranchStatusImpl<>(extendable);
    }

    @Override
    public BranchStatusAdder<C> withStatus(BranchStatus.Status status) {
        this.status = Objects.requireNonNull(status);
        return this;
    }
}
