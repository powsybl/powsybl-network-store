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

    private final DiscreteMeasurements<?> discreteMeasurements;

    private final DiscreteMeasurementAttributes discreteMeasurementAttributes;

    AbstractIdentifiableImpl<?, ?> abstractIdentifiable;

    private static final String VALUE_FOR = "value for ";

    public DiscreteMeasurementImpl(DiscreteMeasurements<?> discreteMeasurements, AbstractIdentifiableImpl<?, ?> abstractIdentifiable, DiscreteMeasurementAttributes discreteMeasurementAttributes) {
        this.discreteMeasurements = discreteMeasurements;
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
        String oldValue = getProperty(name);
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> {
                int index = ((DiscreteMeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(DiscreteMeasurements.NAME)).getDiscreteMeasurementAttributes().indexOf(this.discreteMeasurementAttributes);
                if (index != -1) {
                    this.discreteMeasurementAttributes.getProperties().put(name, value);
                    ((DiscreteMeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(DiscreteMeasurements.NAME)).getDiscreteMeasurementAttributes().set(index, this.discreteMeasurementAttributes);
                }
            }, "property " + name + " for " + getInfo(), oldValue, value);
        return this;
    }

    @Override
    public DiscreteMeasurement removeProperty(String name) {
        String oldValue = getProperty(name);
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> this.discreteMeasurementAttributes.getProperties().remove(name), "property " + name + " for " + getInfo(), oldValue, null);
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

    private Object getValue() {
        return discreteMeasurementAttributes.getValue();
    }

    @Override
    public DiscreteMeasurement setValue(String value) {
        Object oldValue = getValue();
        checkValue(value, discreteMeasurementAttributes.isValid());
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> {
                this.discreteMeasurementAttributes.setValueType(ValueType.STRING);
                this.discreteMeasurementAttributes.setValue(value);
            }, VALUE_FOR + getInfo(), oldValue, value);
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(int value) {
        Object oldValue = getValue();
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> {
                this.discreteMeasurementAttributes.setValueType(ValueType.INT);
                this.discreteMeasurementAttributes.setValue(value);
            }, VALUE_FOR + getInfo(), oldValue, value);
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(boolean value) {
        Object oldValue = getValue();
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> {
                this.discreteMeasurementAttributes.setValueType(ValueType.BOOLEAN);
                this.discreteMeasurementAttributes.setValue(value);
            }, VALUE_FOR + getInfo(), oldValue, value);
        return this;
    }

    @Override
    public boolean isValid() {
        return discreteMeasurementAttributes.isValid();
    }

    @Override
    public DiscreteMeasurement setValid(boolean valid) {
        boolean oldValue = isValid();
        this.abstractIdentifiable.updateResourceExtension(discreteMeasurements,
            resource -> this.discreteMeasurementAttributes.setValid(valid), "validity for " + getInfo(), oldValue, valid);
        return this;
    }

    @Override
    public void remove() {
        this.abstractIdentifiable.updateResource(resource ->
                ((DiscreteMeasurementsAttributes) resource.getAttributes().getExtensionAttributes().get(DiscreteMeasurements.NAME)).getDiscreteMeasurementAttributes().remove(this.discreteMeasurementAttributes));
    }

    private String getInfo() {
        return "discreteMeasurement(id=" + getId() + ", type=" + getType() +
            (getTapChanger() != null ? ", tap changer=" + getTapChanger() : "") + ")";
    }
}
