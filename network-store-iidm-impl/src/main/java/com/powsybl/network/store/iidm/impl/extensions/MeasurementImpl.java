/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.MeasurementAttributes;
import com.powsybl.network.store.model.MeasurementsAttributes;

import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class MeasurementImpl implements Measurement {

    private MeasurementAttributes measurementAttributes;

    AbstractIdentifiableImpl<?, ?> abstractIdentifiable;

    public MeasurementImpl(AbstractIdentifiableImpl abstractIdentifiable, MeasurementAttributes measurementAttributes) {
        this.measurementAttributes = measurementAttributes;
        this.abstractIdentifiable = abstractIdentifiable;
    }

    @Override
    public String getId() {
        return measurementAttributes.getId();
    }

    @Override
    public Type getType() {
        return measurementAttributes.getType();
    }

    @Override
    public Set<String> getPropertyNames() {
        return measurementAttributes.getProperties().keySet();
    }

    @Override
    public String getProperty(String name) {
        return measurementAttributes.getProperties().get(name);
    }

    @Override
    public Measurement putProperty(String name, String property) {
        this.measurementAttributes.getProperties().put(name, property);
        return this;
    }

    @Override
    public Measurement removeProperty(String id) {
        this.measurementAttributes.getProperties().remove(id);
        return this;
    }

    @Override
    public Measurement setValue(double value) {
        this.measurementAttributes.setValue(value);
        return this;
    }

    @Override
    public double getValue() {
        return this.measurementAttributes.getValue();
    }

    @Override
    public Measurement setStandardDeviation(double standardDeviation) {
        this.measurementAttributes.setStandardDeviation(standardDeviation);
        return this;
    }

    @Override
    public double getStandardDeviation() {
        return this.measurementAttributes.getStandardDeviation();
    }

    @Override
    public boolean isValid() {
        return this.measurementAttributes.isValid();
    }

    @Override
    public Measurement setValid(boolean b) {
        this.measurementAttributes.setValid(b);
        return this;
    }

    @Override
    public ThreeSides getSide() {
        return this.measurementAttributes.getSide() != null ?
                ThreeSides.valueOf(this.measurementAttributes.getSide()) : null;
    }

    @Override
    public void remove() {
        ((MeasurementsAttributes) abstractIdentifiable.getResource().getAttributes().getExtensionAttributes().get(Measurements.NAME))
                .getMeasurementAttributes().remove(this.measurementAttributes);
    }
}
