/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.CoordinatedReactiveControlAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CoordinatedReactiveControlAdderImpl extends AbstractExtensionAdder<Generator, CoordinatedReactiveControl> implements CoordinatedReactiveControlAdder {

    private double qPercent;

    public CoordinatedReactiveControlAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected CoordinatedReactiveControl createExtension(Generator generator) {
        CoordinatedReactiveControlAttributes attributes = CoordinatedReactiveControlAttributes.builder()
                .qPercent(qPercent)
                .build();
        ((GeneratorImpl) generator).updateResource(res -> res.getAttributes().setCoordinatedReactiveControl(attributes));
        return new CoordinatedReactiveControlImpl((GeneratorImpl) generator);
    }

    @Override
    public CoordinatedReactiveControlAdder withQPercent(double qPercent) {
        this.qPercent = qPercent;
        return this;
    }
}
