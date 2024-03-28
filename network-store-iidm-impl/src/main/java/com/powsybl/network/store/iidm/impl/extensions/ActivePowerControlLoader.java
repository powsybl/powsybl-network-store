/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class ActivePowerControlLoader<I extends Injection<I>> implements ExtensionLoader<I, ActivePowerControl<I>, ActivePowerControlAttributes> {
    @Override
    public Extension<I> load(I injection) {
        return new ActivePowerControlImpl<>(injection);
    }

    @Override
    public String getName() {
        return ActivePowerControl.NAME;
    }

    @Override
    public Class<ActivePowerControl> getType() {
        return ActivePowerControl.class;
    }

    @Override
    public Class<ActivePowerControlAttributes> getAttributesType() {
        return ActivePowerControlAttributes.class;
    }
}
