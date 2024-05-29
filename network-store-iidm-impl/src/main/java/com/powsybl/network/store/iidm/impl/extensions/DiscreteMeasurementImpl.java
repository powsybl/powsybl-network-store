/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.DiscreteMeasurementAttributes;
import com.powsybl.network.store.model.DiscreteMeasurementsAttributes;

import java.util.Set;

import static com.powsybl.iidm.network.extensions.util.DiscreteMeasurementValidationUtil.checkValue;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class DiscreteMeasurementImpl implements DiscreteMeasurement {

    private final DiscreteMeasurementAttributes discreteMeasurementAttributes;

    AbstractIdentifiableImpl<?, ?> abstractIdentifiable;

    public DiscreteMeasurementImpl(AbstractIdentifiableImpl abstractIdentifiable, DiscreteMeasurementAttributes discreteMeasurementAttributes) {
        this.discreteMeasurementAttributes = discreteMeasurementAttributes;
        this.abstractIdentifiable = abstractIdentifiable;
    }

    @Override
    public String getId() {
        return discreteMeasurementAttributes.getId();
    }

    @Override
    public Type getType() {
        return discreteMeasurementAttributes.getType();
    }

    @Override
    public TapChanger getTapChanger() {
        return discreteMeasurementAttributes.getTapChanger();
    }

    @Override
    public Set<String> getPropertyNames() {
        return discreteMeasurementAttributes.getProperties().keySet();
    }

    @Override
    public String getProperty(String name) {
        return discreteMeasurementAttributes.getProperties().get(name);
    }

    @Override
    public DiscreteMeasurement putProperty(String name, String value) {
        discreteMeasurementAttributes.getProperties().put(name, value);
        return this;
    }

    @Override
    public DiscreteMeasurement removeProperty(String name) {
        discreteMeasurementAttributes.getProperties().remove(name);
        return this;
    }

    @Override
    public ValueType getValueType() {
        return discreteMeasurementAttributes.getValueType();
    }

    @Override
    public String getValueAsString() {
        if (discreteMeasurementAttributes.getValueType() == ValueType.STRING) {
            return (String) discreteMeasurementAttributes.getValue();
        }
        throw new PowsyblException("Value type is not STRING but is: " + discreteMeasurementAttributes.getValueType().name());
    }

    @Override
    public int getValueAsInt() {
        if (discreteMeasurementAttributes.getValueType() == ValueType.INT) {
            return (int) discreteMeasurementAttributes.getValue();
        }
        throw new PowsyblException("Value type is not INT but is: " + discreteMeasurementAttributes.getType().name());
    }

    @Override
    public boolean getValueAsBoolean() {
        if (discreteMeasurementAttributes.getValueType() == ValueType.BOOLEAN) {
            return (boolean) discreteMeasurementAttributes.getValue();
        }
        throw new PowsyblException("Value type is not BOOLEAN but is: " + discreteMeasurementAttributes.getType().name());
    }

    @Override
    public DiscreteMeasurement setValue(String value) {
        checkValue(value, discreteMeasurementAttributes.isValid());
        discreteMeasurementAttributes.setValueType(ValueType.STRING);
        discreteMeasurementAttributes.setValue(value);
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(int value) {
        discreteMeasurementAttributes.setValueType(ValueType.INT);
        discreteMeasurementAttributes.setValue(value);
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(boolean value) {
        discreteMeasurementAttributes.setValueType(ValueType.BOOLEAN);
        discreteMeasurementAttributes.setValue(value);
        return this;
    }

    @Override
    public boolean isValid() {
        return discreteMeasurementAttributes.isValid();
    }

    @Override
    public DiscreteMeasurement setValid(boolean valid) {
        discreteMeasurementAttributes.setValid(valid);
        return this;
    }

    @Override
    public void remove() {
        ((DiscreteMeasurementsAttributes) abstractIdentifiable.getResource().getAttributes().getExtensionAttributes().get(DiscreteMeasurements.NAME))
                .getDiscreteMeasurementAttributes().remove(this.discreteMeasurementAttributes);
    }
}
