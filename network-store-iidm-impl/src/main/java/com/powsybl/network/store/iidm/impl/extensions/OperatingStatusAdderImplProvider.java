/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.OperatingStatus;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class OperatingStatusAdderImplProvider<I extends Identifiable<I>> implements
        ExtensionAdderProvider<I, OperatingStatus<I>, OperatingStatusAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<OperatingStatusAdderImpl> getAdderClass() {
        return OperatingStatusAdderImpl.class;
    }

    @Override
    public OperatingStatusAdderImpl<C> newAdder(I identifiable) {
        return new OperatingStatusAdderImpl<>(identifiable);
    }

}
