/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorEntsoeCategoryAttributes;

/**
 * @author Borsenberger Jacques <borsenberger.jacques at rte-france.com>
 */
public class GeneratorEntsoeCategoryImpl implements GeneratorEntsoeCategory {

    private GeneratorImpl generator;

    public GeneratorEntsoeCategoryImpl(GeneratorImpl generator) {
        this.generator = generator;
    }

    public GeneratorEntsoeCategoryImpl(GeneratorImpl generator, int code) {
        this(generator);
        generator.getResource().getAttributes().setEntsoeCategoryAttributes(GeneratorEntsoeCategoryAttributes.builder().code(code).build());
    }

    @Override
    public Generator getExtendable() {
        return generator;
    }

    @Override
    public void setExtendable(Generator generator) {
        this.generator = (GeneratorImpl) generator;
    }

    @Override
    public int getCode() {
        return generator.getResource().getAttributes().getEntsoeCategoryAttributes().getCode();
    }

    @Override
    public GeneratorEntsoeCategory setCode(int code) {
        generator.getResource().getAttributes().getEntsoeCategoryAttributes().setCode(code);
        return this;
    }
}
