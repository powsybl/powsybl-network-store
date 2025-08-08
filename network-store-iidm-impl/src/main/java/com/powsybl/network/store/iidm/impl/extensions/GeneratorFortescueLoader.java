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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.GeneratorFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class GeneratorFortescueLoader implements ExtensionLoader<Generator, GeneratorFortescue, GeneratorFortescueAttributes> {
    @Override
    public Extension<Generator> load(Generator generator) {
        return new GeneratorFortescueImpl(generator);
    }

    @Override
    public String getName() {
        return GeneratorFortescue.NAME;
    }

    @Override
    public Class<GeneratorFortescue> getType() {
        return GeneratorFortescue.class;
    }

    @Override
    public Class<GeneratorFortescueAttributes> getAttributesType() {
        return GeneratorFortescueAttributes.class;
    }
}
