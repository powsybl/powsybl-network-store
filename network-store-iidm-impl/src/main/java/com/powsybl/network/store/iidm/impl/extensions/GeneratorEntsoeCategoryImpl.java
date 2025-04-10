/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Borsenberger Jacques <borsenberger.jacques at rte-france.com>
 */
public class GeneratorEntsoeCategoryImpl extends AbstractExtension<Generator> implements GeneratorEntsoeCategory {

    public GeneratorEntsoeCategoryImpl(GeneratorImpl generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    @Override
    public int getCode() {
        return getGenerator().getResource().getAttributes().getEntsoeCategoryAttributes().getCode();
    }

    @Override
    public GeneratorEntsoeCategory setCode(int code) {
        int oldValue = getCode();
        if (oldValue != code) {
            getGenerator().updateResourceExtension(this, res -> res.getAttributes().getEntsoeCategoryAttributes().setCode(code), "code", oldValue, code);
        }
        return this;
    }
}
