/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.SecondaryVoltageControlAttributes;

/**
 * @author Achour Berrahma <achour.berrahma at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class SecondaryVoltageControlLoader implements ExtensionLoader<Network, SecondaryVoltageControl, SecondaryVoltageControlAttributes> {

    @Override
    public Extension<Network> load(Network extendable) {
        return new SecondaryVoltageControlImpl(extendable);
    }

    @Override
    public String getName() {
        return SecondaryVoltageControl.NAME;
    }

    @Override
    public Class<? super SecondaryVoltageControl> getType() {
        return SecondaryVoltageControl.class;
    }

    @Override
    public Class<SecondaryVoltageControlAttributes> getAttributesType() {
        return SecondaryVoltageControlAttributes.class;
    }
}
