/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.MeasurementsAttributes;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class MeasurementsImpl<C extends Connectable<C>> extends AbstractExtension<C> implements Measurements<C> {

    public MeasurementsImpl(C connectable) {
        super(connectable);
    }

    private AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return (AbstractIdentifiableImpl<?, ?>) getExtendable();
    }

    MeasurementsAttributes getMeasurementsAttributes() {
        return (MeasurementsAttributes) getIdentifiable().getResource().getAttributes().getExtensionAttributes().get(Measurements.NAME);
    }

    @Override
    public Collection<Measurement> getMeasurements() {
        return getMeasurementsAttributes().getMeasurementAttributes().stream()
                .map(measurementAttributes -> new MeasurementImpl(this, getIdentifiable(), measurementAttributes))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Measurement> getMeasurements(Measurement.Type type) {
        return getMeasurementsAttributes().getMeasurementAttributes().stream()
                .filter(measurementAttributes -> measurementAttributes.getType().equals(type))
                .map(measurementAttributes -> new MeasurementImpl(this, getIdentifiable(), measurementAttributes))
                .collect(Collectors.toList());
    }

    @Override
    public Measurement getMeasurement(String id) {
        return getMeasurementsAttributes().getMeasurementAttributes().stream()
                .filter(measurementAttributes -> id.equals(measurementAttributes.getId()))
                .map(measurementAttributes -> new MeasurementImpl(this, getIdentifiable(), measurementAttributes))
                .findFirst().orElse(null);
    }

    @Override
    public MeasurementAdder newMeasurement() {
        return new MeasurementAdderImpl(this);
    }
}
