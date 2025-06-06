/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.OperatingStatusAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class OperatingStatusImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements OperatingStatus<I> {

    public OperatingStatusImpl(I identifiable) {
        super(identifiable);
    }

    @Override
    public Status getStatus() {
        var resource = ((AbstractIdentifiableImpl<?, ?>) getExtendable()).getResource();
        return Status.valueOf(((OperatingStatusAttributes) (resource.getAttributes().getExtensionAttributes().get(OperatingStatus.NAME))).getOperatingStatus());
    }

    @Override
    public OperatingStatus<I> setStatus(Status status) {
        Status oldValue = getStatus();
        if (oldValue != status) {
            ((AbstractIdentifiableImpl<?, ?>) getExtendable()).updateResourceExtension(this, res -> ((OperatingStatusAttributes) res.getAttributes().getExtensionAttributes().get(OperatingStatus.NAME)).setOperatingStatus(status.name()), "status", oldValue, status);
        }
        return this;
    }
}
