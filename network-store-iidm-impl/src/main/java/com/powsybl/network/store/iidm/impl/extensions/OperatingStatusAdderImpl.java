/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.OperatingStatusHolder;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class OperatingStatusAdderImpl<I extends Identifiable<I>>
        extends AbstractExtensionAdder<C, OperatingStatus<C>> implements OperatingStatusAdder<C> {

    private OperatingStatus.Status status = OperatingStatus.Status.IN_OPERATION;

    protected OperatingStatusAdderImpl(C extendable) {
        super(extendable);
    }

    @Override
    protected OperatingStatus<C> createExtension(C extendable) {
        ((AbstractIdentifiableImpl<?, ?>) extendable).updateResource(res -> {
            if (!(res.getAttributes() instanceof OperatingStatusHolder)) {
                throw new IllegalStateException("Not an operating status holder");
            }
            ((OperatingStatusHolder) res.getAttributes()).setOperatingStatus(status.name());
        });
        return new OperatingStatusImpl<>(extendable);
    }

    @Override
    public OperatingStatusAdder<C> withStatus(OperatingStatus.Status status) {
        this.status = Objects.requireNonNull(status);
        return this;
    }
}
