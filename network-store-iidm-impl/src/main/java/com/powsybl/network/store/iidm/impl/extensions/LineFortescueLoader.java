/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.LineFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class LineFortescueLoader implements ExtensionLoader<Line, LineFortescue, LineFortescueAttributes> {
    @Override
    public Extension<Line> load(Line line) {
        return new LineFortescueImpl(line);
    }

    @Override
    public String getName() {
        return LineFortescue.NAME;
    }

    @Override
    public Class<LineFortescue> getType() {
        return LineFortescue.class;
    }

    @Override
    public Class<LineFortescueAttributes> getAttributesType() {
        return LineFortescueAttributes.class;
    }
}
