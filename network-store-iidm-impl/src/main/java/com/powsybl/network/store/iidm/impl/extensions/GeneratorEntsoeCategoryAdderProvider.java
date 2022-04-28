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
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;

/**
 * @author Borsenberger Jacques <borsenberger.jacques at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorEntsoeCategoryAdderProvider
    implements ExtensionAdderProvider<Generator, GeneratorEntsoeCategory, GeneratorEntsoeCategoryAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<? super GeneratorEntsoeCategoryAdderImpl> getAdderClass() {
        return GeneratorEntsoeCategoryAdderImpl.class;
    }

    @Override
    public GeneratorEntsoeCategoryAdderImpl newAdder(Generator extendable) {
        return new GeneratorEntsoeCategoryAdderImpl(extendable);
    }
}
