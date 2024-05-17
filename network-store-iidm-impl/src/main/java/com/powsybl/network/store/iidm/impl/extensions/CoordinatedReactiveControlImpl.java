/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CoordinatedReactiveControlImpl extends AbstractExtension<Generator> implements CoordinatedReactiveControl {

    public CoordinatedReactiveControlImpl(GeneratorImpl generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    @Override
    public double getQPercent() {
        return getGenerator().getResource().getAttributes().getCoordinatedReactiveControl().getQPercent();
    }

    @Override
    public void setQPercent(double qPercent) {
        checkQPercent(qPercent);
        getGenerator().updateResource(res -> res.getAttributes().getCoordinatedReactiveControl().setQPercent(qPercent));
    }

    private void checkQPercent(double qPercent) {
        if (Double.isNaN(qPercent)) {
            throw new PowsyblException(String.format("Undefined value (%s) for qPercent for generator %s",
                    qPercent, getGenerator().getId()));
        }
    }
}
