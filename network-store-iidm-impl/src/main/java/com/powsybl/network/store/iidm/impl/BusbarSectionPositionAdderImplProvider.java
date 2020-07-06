/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class BusbarSectionPositionAdderImplProvider
        implements ExtensionAdderProvider<BusbarSection, BusbarSectionPosition, BusbarSectionPositionAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<BusbarSectionPositionAdderImpl> getAdderClass() {
        return BusbarSectionPositionAdderImpl.class;
    }

    @Override
    public BusbarSectionPositionAdderImpl newAdder(BusbarSection busbarSection) {
        return new BusbarSectionPositionAdderImpl(busbarSection);
    }

}
