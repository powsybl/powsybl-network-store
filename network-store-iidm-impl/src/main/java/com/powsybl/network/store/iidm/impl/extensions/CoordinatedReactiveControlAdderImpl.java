/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.CoordinatedReactiveControlAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CoordinatedReactiveControlAdderImpl extends AbstractIidmExtensionAdder<Generator, CoordinatedReactiveControl> implements CoordinatedReactiveControlAdder {

    private double qPercent;

    public CoordinatedReactiveControlAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected CoordinatedReactiveControl createExtension(Generator generator) {
        checkQPercent();
        CoordinatedReactiveControlAttributes attributes = CoordinatedReactiveControlAttributes.builder()
                .qPercent(qPercent)
                .build();
        ((GeneratorImpl) generator).updateResourceWithoutNotification(res -> res.getAttributes().setCoordinatedReactiveControl(attributes));
        return new CoordinatedReactiveControlImpl((GeneratorImpl) generator);
    }

    private void checkQPercent() {
        if (Double.isNaN(qPercent)) {
            throw new PowsyblException(String.format("Undefined value (%s) for qPercent for generator %s",
                    qPercent, extendable.getId()));
        }
    }

    @Override
    public CoordinatedReactiveControlAdder withQPercent(double qPercent) {
        this.qPercent = qPercent;
        return this;
    }
}
