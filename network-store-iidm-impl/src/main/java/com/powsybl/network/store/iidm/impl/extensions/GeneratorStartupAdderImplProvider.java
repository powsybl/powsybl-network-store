/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorStartupAdderImplProvider
        implements ExtensionAdderProvider<Generator, GeneratorStartup, GeneratorStartupAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return GeneratorStartup.NAME;
    }

    @Override
    public Class<GeneratorStartupAdderImpl> getAdderClass() {
        return GeneratorStartupAdderImpl.class;
    }

    @Override
    public GeneratorStartupAdderImpl newAdder(Generator extendable) {
        return new GeneratorStartupAdderImpl(extendable);
    }
}
