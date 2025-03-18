/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.OperatingStatusAttributes;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class OperatingStatusAdderImpl<I extends Identifiable<I>>
        extends AbstractIidmExtensionAdder<I, OperatingStatus<I>> implements OperatingStatusAdder<I> {

    private OperatingStatus.Status status = OperatingStatus.Status.IN_OPERATION;

    protected OperatingStatusAdderImpl(I identifiable) {
        super(identifiable);
    }

    @Override
    protected OperatingStatus<I> createExtension(I identifiable) {
        ((AbstractIdentifiableImpl<?, ?>) identifiable).updateResource(res -> {
            if (!OperatingStatus.isAllowedIdentifiable(identifiable)) {
                throw new PowsyblException("Operating status extension is not allowed on identifiable type: " + identifiable.getType());
            }
            var attributes = new OperatingStatusAttributes(status.name());
            res.getAttributes().getExtensionAttributes().put(OperatingStatus.NAME, attributes);
        });
        return new OperatingStatusImpl<>(identifiable);
    }

    @Override
    public OperatingStatusAdder<I> withStatus(OperatingStatus.Status status) {
        this.status = Objects.requireNonNull(status);
        return this;
    }
}
