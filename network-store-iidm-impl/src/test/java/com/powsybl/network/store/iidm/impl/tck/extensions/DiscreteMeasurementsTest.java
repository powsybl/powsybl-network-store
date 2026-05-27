/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractDiscreteMeasurementsTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.DiscreteMeasurementAttributes;
import com.powsybl.network.store.model.DiscreteMeasurementsAttributes;
import com.powsybl.network.store.model.ExtensionAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static com.powsybl.iidm.network.extensions.DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER;
import static com.powsybl.iidm.network.extensions.DiscreteMeasurement.ValueType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DiscreteMeasurementsTest extends AbstractDiscreteMeasurementsTest {

    //FIXME delete this test when extension deletion is implemented
    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Switch sw = network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR");
        sw.newExtension(DiscreteMeasurementsAdder.class).add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setType(DiscreteMeasurement.Type.SWITCH_POSITION)
                .setId("IS_FICT")
                .setValue("CLOSED")
                .setValid(false)
                .putProperty("source", "test")
                .putProperty("other", "test3")
                .add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("IS_FICT")
                .setEnsureIdUnicity(true)
                .setType(DiscreteMeasurement.Type.OTHER)
                .setValue(false)
                .setValid(true)
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.newExtension(DiscreteMeasurementsAdder.class).add();
        twt.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_TAP_POS")
                .setType(DiscreteMeasurement.Type.TAP_POSITION)
                .setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)
                .setValue(15)
                .putProperty("source", "test2")
                .add();

        DiscreteMeasurements<Switch> swDisMeasurements = sw.getExtension(DiscreteMeasurements.class);
        assertNotNull(swDisMeasurements);
        assertEquals(2, swDisMeasurements.getDiscreteMeasurements().size());
        for (DiscreteMeasurement meas : swDisMeasurements.getDiscreteMeasurements()) {
            if ("IS_FICT".equals(meas.getId())) {
                assertEquals(DiscreteMeasurement.Type.SWITCH_POSITION, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals(STRING, meas.getValueType());
                assertEquals("CLOSED", meas.getValueAsString());
                assertFalse(meas.isValid());
                assertEquals(2, meas.getPropertyNames().size());
                assertEquals("test", meas.getProperty("source"));
                assertEquals("test3", meas.getProperty("other"));
            } else {
                assertEquals("IS_FICT#0", meas.getId());
                assertEquals(DiscreteMeasurement.Type.OTHER, meas.getType());
                assertNull(meas.getTapChanger());
                assertEquals(BOOLEAN, meas.getValueType());
                assertFalse(meas.getValueAsBoolean());
                assertTrue(meas.isValid());
                assertTrue(meas.getPropertyNames().isEmpty());
                assertNull(meas.getProperty("source"));
                meas.putProperty("source", "test4");
                assertEquals(1, meas.getPropertyNames().size());
                assertEquals("test4", meas.getProperty("source"));

                meas.setValue("CHANGED VALUE");
                assertEquals(STRING, meas.getValueType());
                assertEquals("CHANGED VALUE", meas.getValueAsString());
            }
        }

        swDisMeasurements.cleanIfEmpty();
        assertNotNull(sw.getExtension(DiscreteMeasurements.class));

        DiscreteMeasurements<TwoWindingsTransformer> twtDisMeasurements = twt.getExtension(DiscreteMeasurements.class);
        assertNotNull(twtDisMeasurements);
        assertEquals(1, twtDisMeasurements.getDiscreteMeasurements().size());
        DiscreteMeasurement ptcPos = twtDisMeasurements.getDiscreteMeasurement("DIS_MEAS_TAP_POS");
        assertNotNull(ptcPos);
        assertEquals(DiscreteMeasurement.Type.TAP_POSITION, ptcPos.getType());
        assertEquals(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER, ptcPos.getTapChanger());
        assertEquals(INT, ptcPos.getValueType());
        assertEquals(15, ptcPos.getValueAsInt());
        assertTrue(ptcPos.isValid());
        assertEquals(1, ptcPos.getPropertyNames().size());
        assertEquals("test2", ptcPos.getProperty("source"));

        ptcPos.remove();
        assertTrue(twtDisMeasurements.getDiscreteMeasurements().isEmpty());
    }

    @Test
    void addDiscreteMeasurementShouldUpdateResourceWithoutNotification() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        TwoWindingsTransformerImpl twt = (TwoWindingsTransformerImpl) network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerImpl twtSpy = Mockito.spy(twt);
        DiscreteMeasurementsAdder<?> discreteMeasurementsAdder = twtSpy.newExtension(DiscreteMeasurementsAdder.class);
        DiscreteMeasurements<?> discreteMeasurements = discreteMeasurementsAdder.add();

        String expectedId = UUID.randomUUID().toString();
        boolean expectedValidity = true;
        int expectedValue = 100;
        DiscreteMeasurement.Type expectedType = DiscreteMeasurement.Type.TAP_POSITION;
        DiscreteMeasurement.TapChanger expectedTapChanger = RATIO_TAP_CHANGER;
        DiscreteMeasurementAdder dmAdder = discreteMeasurements.newDiscreteMeasurement()
                .setId(expectedId)
                .setType(expectedType)
                .setValid(expectedValidity)
                .setValue(expectedValue)
                .setTapChanger(expectedTapChanger);
        Mockito.clearInvocations(twtSpy);
        dmAdder.add();

        Map<String, ExtensionAttributes> extensionAttributes = twtSpy.getResource()
                .getAttributes()
                .getExtensionAttributes();
        DiscreteMeasurementsAttributes twtDiscreteMeasurementsAttributes = (DiscreteMeasurementsAttributes) extensionAttributes.get(DiscreteMeasurements.NAME);
        ArgumentCaptor<DiscreteMeasurementAttributes> argumentCaptor = ArgumentCaptor.forClass(DiscreteMeasurementAttributes.class);
        Mockito.verify(twtSpy).updateResourceExtension(Mockito.eq(discreteMeasurements), Mockito.any(), Mockito.eq("discreteMeasurements.discreteMeasurement"), Mockito.eq(null), argumentCaptor.capture());
        DiscreteMeasurementAttributes actualMeasurementAttributesFromResourceUpdate = argumentCaptor.getValue();
        assertEquals(1, extensionAttributes.size());
        assertEquals(1, twtDiscreteMeasurementsAttributes.getDiscreteMeasurementAttributes().size());
        assertDiscreteMeasurementAttributes(twtDiscreteMeasurementsAttributes.getDiscreteMeasurementAttributes().getFirst(), expectedId, expectedValidity, expectedValue, expectedType, expectedTapChanger);
        assertDiscreteMeasurementAttributes(actualMeasurementAttributesFromResourceUpdate, expectedId, expectedValidity, expectedValue, expectedType, expectedTapChanger);
    }

    private void assertDiscreteMeasurementAttributes(DiscreteMeasurementAttributes discreteMeasurementAttributes,
                                                     String expectedId,
                                                     boolean expectedValidity,
                                                     int expectedValue,
                                                     DiscreteMeasurement.Type expectedType,
                                                     DiscreteMeasurement.TapChanger expectedTapChanger) {
        assertEquals(expectedId, discreteMeasurementAttributes.getId());
        assertEquals(expectedValidity, discreteMeasurementAttributes.isValid());
        assertEquals(expectedValue, discreteMeasurementAttributes.getValue());
        assertEquals(expectedType, discreteMeasurementAttributes.getType());
        assertEquals(expectedTapChanger, discreteMeasurementAttributes.getTapChanger());
    }
}
