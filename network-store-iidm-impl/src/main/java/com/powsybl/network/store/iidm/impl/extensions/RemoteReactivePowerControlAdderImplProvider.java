/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;

/**
 * @author Jon Harper <jon.harper at rte-france.com.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class RemoteReactivePowerControlAdderImplProvider implements ExtensionAdderProvider<Generator, RemoteReactivePowerControl, RemoteReactivePowerAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<RemoteReactivePowerAdderImpl> getAdderClass() {
        return RemoteReactivePowerAdderImpl.class;
    }

    @Override
    public RemoteReactivePowerAdderImpl newAdder(Generator extendable) {
        return new RemoteReactivePowerAdderImpl(extendable);
    }
}
