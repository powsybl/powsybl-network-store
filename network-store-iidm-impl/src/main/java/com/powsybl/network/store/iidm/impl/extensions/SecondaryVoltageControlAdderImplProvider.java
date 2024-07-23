/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class SecondaryVoltageControlAdderImplProvider implements ExtensionAdderProvider<Network, SecondaryVoltageControl, SecondaryVoltageControlAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return SecondaryVoltageControl.NAME;
    }

    @Override
    public Class<SecondaryVoltageControlAdderImpl> getAdderClass() {
        return SecondaryVoltageControlAdderImpl.class;
    }

    @Override
    public SecondaryVoltageControlAdderImpl newAdder(Network network) {
        return new SecondaryVoltageControlAdderImpl(network);
    }
}
