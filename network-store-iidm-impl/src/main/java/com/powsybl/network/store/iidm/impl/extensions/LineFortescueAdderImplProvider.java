/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class LineFortescueAdderImplProvider implements ExtensionAdderProvider<Line, LineFortescue, LineFortescueAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return LineFortescue.NAME;
    }

    @Override
    public Class<LineFortescueAdderImpl> getAdderClass() {
        return LineFortescueAdderImpl.class;
    }

    @Override
    public LineFortescueAdderImpl newAdder(Line extendable) {
        return new LineFortescueAdderImpl(extendable);
    }
}
