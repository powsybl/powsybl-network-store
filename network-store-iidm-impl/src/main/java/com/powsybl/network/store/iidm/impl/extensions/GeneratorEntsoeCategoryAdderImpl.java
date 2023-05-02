/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorEntsoeCategoryAttributes;

/**
 * @author Borsenberger Jacques <borsenberger.jacques at rte-france.com>
 */
public class GeneratorEntsoeCategoryAdderImpl extends AbstractExtensionAdder<Generator, GeneratorEntsoeCategory>
    implements GeneratorEntsoeCategoryAdder {

    private int code;

    public GeneratorEntsoeCategoryAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected GeneratorEntsoeCategory createExtension(Generator generator) {
        var attributes = GeneratorEntsoeCategoryAttributes.builder().code(code).build();
        ((GeneratorImpl) generator).updateResource(res -> res.getAttributes().setEntsoeCategoryAttributes(attributes));
        return new GeneratorEntsoeCategoryImpl((GeneratorImpl) generator);
    }

    @Override
    public GeneratorEntsoeCategoryAdderImpl withCode(int code) {
        this.code = code;
        return this;
    }

}
