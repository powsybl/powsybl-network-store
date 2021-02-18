/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CoordinatedReactiveControlImpl implements CoordinatedReactiveControl {

    private GeneratorImpl generator;

    public CoordinatedReactiveControlImpl(GeneratorImpl generator) {
        this.generator = generator;
    }

    public CoordinatedReactiveControlImpl(GeneratorImpl generator, double qPercent) {
        this(generator);
        generator.getResource().getAttributes().getCoordinatedReactiveControl().setQPercent(qPercent);
    }

    @Override
    public double getQPercent() {
        return generator.getResource().getAttributes().getCoordinatedReactiveControl().getQPercent();
    }

    @Override
    public void setQPercent(double qPercent) {
        generator.getResource().getAttributes().getCoordinatedReactiveControl().setQPercent(qPercent);
    }

    @Override
    public Generator getExtendable() {
        return generator;
    }

    @Override
    public void setExtendable(Generator generator) {
        this.generator = (GeneratorImpl) generator;
    }

}
