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
import com.powsybl.iidm.network.extensions.util.MeasurementValidationUtil;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.MeasurementAttributes;
import com.powsybl.network.store.model.MeasurementsAttributes;

import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class MeasurementImpl implements Measurement {

    private final Measurements<?> measurements;

    private final MeasurementAttributes measurementAttributes;

    AbstractIdentifiableImpl<?, ?> abstractIdentifiable;

    public MeasurementImpl(Measurements<?> measurements, AbstractIdentifiableImpl<?, ?> abstractIdentifiable, MeasurementAttributes measurementAttributes) {
        this.measurements = measurements;
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
        String oldValue = getProperty(name);
        this.abstractIdentifiable.updateResourceExtension(measurements,
            resource -> {
                int index = ((MeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(Measurements.NAME)).getMeasurementAttributes().indexOf(this.measurementAttributes);
                if (index != -1) {
                    this.measurementAttributes.getProperties().put(name, property);
                    ((MeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(Measurements.NAME)).getMeasurementAttributes().set(index, this.measurementAttributes);
                }
            }, "property " + name + " for " + getInfo(), oldValue, property);
        return this;
    }

    @Override
    public Measurement removeProperty(String id) {
        String oldValue = getProperty(id);
        this.abstractIdentifiable.updateResourceExtension(measurements,
            resource -> this.measurementAttributes.getProperties().remove(id), "property " + id + " for " + getInfo(), oldValue, null);
        return this;
    }

    @Override
    public Measurement setValue(double value) {
        double oldValue = getValue();
        this.abstractIdentifiable.updateResourceExtension(measurements,
            resource -> this.measurementAttributes.setValue(value), "value for " + getInfo(), oldValue, value);
        return this;
    }

    @Override
    public double getValue() {
        return this.measurementAttributes.getValue();
    }

    @Override
    public Measurement setStandardDeviation(double standardDeviation) {
        double oldValue = getStandardDeviation();
        this.abstractIdentifiable.updateResourceExtension(measurements,
            resource -> this.measurementAttributes.setStandardDeviation(standardDeviation), "standard deviation for " + getInfo(), oldValue, standardDeviation);
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
        MeasurementValidationUtil.checkValue(getValue(), b);
        boolean oldValue = isValid();
        this.abstractIdentifiable.updateResourceExtension(measurements,
            resource -> this.measurementAttributes.setValid(b), "validity for " + getInfo(), oldValue, b);
        return this;
    }

    @Override
    public ThreeSides getSide() {
        return this.measurementAttributes.getSide() != null ?
                ThreeSides.valueOf(this.measurementAttributes.getSide()) : null;
    }

    @Override
    public void remove() {
        this.abstractIdentifiable.updateResourceExtension(measurements, resource ->
                ((MeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(Measurements.NAME)).getMeasurementAttributes().remove(this.measurementAttributes),
            getInfo(), this.measurementAttributes, null);
    }

    private String getInfo() {
        return "measurement(id=" + getId() + ", type=" + getType() + ", side=" + getSide() + ")";
    }
}
