/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.DiscreteMeasurementsAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class DiscreteMeasurementsAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, DiscreteMeasurements<I>> implements DiscreteMeasurementsAdder<I> {

    DiscreteMeasurementsAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected DiscreteMeasurements<I> createExtension(I extendable) {
        DiscreteMeasurementsAttributes discreteMeasurementsAttributes = DiscreteMeasurementsAttributes.builder()
                .build();
        ((AbstractIdentifiableImpl<?, ?>) extendable).updateResource(res -> res.getAttributes().getExtensionAttributes().put(DiscreteMeasurements.NAME, discreteMeasurementsAttributes));
        return new DiscreteMeasurementsImpl<>(extendable);
    }
}
