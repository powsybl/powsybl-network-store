/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.MeasurementAdder;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.extensions.MeasurementsAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractMeasurementsTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.LineImpl;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.MeasurementAttributes;
import com.powsybl.network.store.model.MeasurementsAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
class MeasurementsTest extends AbstractMeasurementsTest {

    // This test covers a bug that made newly added measurements not being persisted. This was possible because the
    // NetworkStoreClient wasn't notified of the change and didn't update the element when flushing the buffers.
    @Test
    void addMeasurementShouldUpdateResourceWithoutNotification() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        LineImpl line = (LineImpl) network.getLine("NHV1_NHV2_1");
        LineImpl lineSpy = Mockito.spy(line);
        MeasurementsAdder<?> measurementsAdder = lineSpy.newExtension(MeasurementsAdder.class);
        Measurements<?> measurements = measurementsAdder.add();

        String expectedId = UUID.randomUUID().toString();
        boolean expectedValidity = true;
        double expectedValue = 100.0;
        MeasurementAdder mAdder = measurements.newMeasurement()
                .setId(expectedId)
                .setSide(ThreeSides.ONE)
                .setType(Measurement.Type.REACTIVE_POWER)
                .setValid(expectedValidity)
                .setValue(expectedValue);
        Mockito.clearInvocations(lineSpy);
        mAdder.add();

        Map<String, ExtensionAttributes> extensionAttributes = lineSpy.getResource()
                .getAttributes()
                .getExtensionAttributes();
        MeasurementsAttributes lineMeasurementAttributes = (MeasurementsAttributes) extensionAttributes.get(Measurements.NAME);
        ArgumentCaptor<MeasurementAttributes> argumentCaptor = ArgumentCaptor.forClass(MeasurementAttributes.class);
        Mockito.verify(lineSpy).updateResourceExtension(Mockito.eq(measurements), Mockito.any(), Mockito.eq("measurements.measurement"), Mockito.eq(null), argumentCaptor.capture());
        MeasurementAttributes actualMeasurementAttributesFromResourceUpdate = argumentCaptor.getValue();
        assertEquals(1, extensionAttributes.size());
        assertEquals(1, lineMeasurementAttributes.getMeasurementAttributes().size());
        assertMeasurementAttributes(lineMeasurementAttributes.getMeasurementAttributes().getFirst(), expectedId, expectedValidity, expectedValue);
        assertMeasurementAttributes(actualMeasurementAttributesFromResourceUpdate, expectedId, expectedValidity, expectedValue);
    }

    private void assertMeasurementAttributes(MeasurementAttributes measurementAttributes, String expectedId, boolean expectedValidity, double expectedValue) {
        assertEquals(expectedId, measurementAttributes.getId());
        assertEquals(expectedValidity, measurementAttributes.isValid());
        assertEquals(expectedValue, measurementAttributes.getValue());
    }
}
