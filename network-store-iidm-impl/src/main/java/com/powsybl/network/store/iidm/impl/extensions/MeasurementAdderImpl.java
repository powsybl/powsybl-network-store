/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.network.store.iidm.impl.AbstractConnectableImpl;
import com.powsybl.network.store.model.MeasurementAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.iidm.network.extensions.util.MeasurementValidationUtil.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class MeasurementAdderImpl implements MeasurementAdder {

    private final MeasurementsImpl<? extends Connectable<?>> measurements;

    private final Map<String, String> properties = new HashMap<>();
    private String id;
    private boolean idUnicity = false;
    private Measurement.Type type;
    private double value = Double.NaN;
    private double standardDeviation = Double.NaN;
    private boolean valid = true;
    private ThreeSides side;

    MeasurementAdderImpl(MeasurementsImpl<? extends Connectable<?>> measurements) {
        this.measurements = Objects.requireNonNull(measurements);
    }

    @Override
    public MeasurementAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public MeasurementAdder putProperty(String name, String property) {
        properties.put(name, property);
        return this;
    }

    @Override
    public MeasurementAdder setType(Measurement.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public MeasurementAdder setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public MeasurementAdder setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        return this;
    }

    @Override
    public MeasurementAdder setSide(ThreeSides threeSides) {
        this.side = threeSides;
        return this;
    }

    @Override
    public MeasurementAdder setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    @Override
    public MeasurementAdder setEnsureIdUnicity(boolean idUnicity) {
        this.idUnicity = idUnicity;
        return this;
    }

    @Override
    public Measurement add() {
        id = checkId(id, idUnicity, measurements);
        if (type == null) {
            throw new PowsyblException("Measurement type can not be null");
        }
        AbstractConnectableImpl<?, ?> extendable = (AbstractConnectableImpl<?, ?>) measurements.getExtendable();
        checkValue(value, valid);
        checkSide(type, side, extendable);
        MeasurementAttributes measurementAttributes = MeasurementAttributes.builder()
                .id(id)
                .valid(valid)
                .value(value)
                .type(type)
                .properties(properties)
                .standardDeviation(standardDeviation)
                .side(side != null ? side.getNum() : null)
                .build();

        // The attribute name is not defined by powsybl-core implementation, so we chose an arbitrary value
        extendable.updateResourceExtension(
                measurements, res ->
                        measurements.getMeasurementsAttributes()
                                .getMeasurementAttributes()
                                .add(measurementAttributes), "measurements.measurement", null, measurementAttributes
        );
        return new MeasurementImpl(measurements, extendable, measurementAttributes);
    }
}
