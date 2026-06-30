/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.cgmes.extensions.CgmesTapChangerAdder;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.CgmesTapChangerAttributes;
import com.powsybl.network.store.model.CgmesTapChangersAttributes;
import com.powsybl.network.store.model.ExtensionAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class CgmesTapChangersTest {
    @Test
    void addTapChangerShouldUpdateResource() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        TwoWindingsTransformerImpl twt = (TwoWindingsTransformerImpl) network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerImpl twtSpy = Mockito.spy(twt);
        CgmesTapChangersAdder<TwoWindingsTransformer> cgmesTapChangersAdder = twtSpy.newExtension(CgmesTapChangersAdder.class);
        CgmesTapChangers<TwoWindingsTransformer> cgmesTapChangers = cgmesTapChangersAdder.add();

        String expectedId = UUID.randomUUID().toString();
        String expectedType = "type";
        String expectedControlId = "controlId";
        int expectedStep = 1;
        boolean expectedHiddenStatus = false;
        CgmesTapChangerAdder ctcAdder = cgmesTapChangers.newTapChanger()
                .setId(expectedId)
                .setType(expectedType)
                .setControlId(expectedControlId)
                .setStep(expectedStep)
                .setHiddenStatus(expectedHiddenStatus);
        Mockito.clearInvocations(twtSpy);
        ctcAdder.add();

        Map<String, ExtensionAttributes> extensionAttributes = twtSpy.getResource()
                .getAttributes()
                .getExtensionAttributes();
        CgmesTapChangersAttributes cgmesTapChangersAttributes = (CgmesTapChangersAttributes) extensionAttributes.get(CgmesTapChangers.NAME);
        ArgumentCaptor<CgmesTapChangerAttributes> argumentCaptor = ArgumentCaptor.forClass(CgmesTapChangerAttributes.class);
        Mockito.verify(twtSpy).updateResourceExtension(Mockito.eq(cgmesTapChangers), Mockito.any(), Mockito.eq("tapChangers.tapChanger"), Mockito.eq(null), argumentCaptor.capture());
        CgmesTapChangerAttributes cgmesTapChangerAttributesFromResourceUpdate = argumentCaptor.getValue();
        assertEquals(1, extensionAttributes.size());
        assertEquals(1, cgmesTapChangersAttributes.getCgmesTapChangers().size());
        assertCgmesTapChangerAttributes(cgmesTapChangersAttributes.getCgmesTapChangers().getFirst(), expectedId, expectedType, expectedControlId, expectedStep, expectedHiddenStatus);
        assertCgmesTapChangerAttributes(cgmesTapChangerAttributesFromResourceUpdate, expectedId, expectedType, expectedControlId, expectedStep, expectedHiddenStatus);
    }

    private void assertCgmesTapChangerAttributes(CgmesTapChangerAttributes cgmesTapChangerAttributes,
                                                 String expectedId,
                                                 String expectedType,
                                                 String expectedControlId,
                                                 int expectedStep,
                                                 boolean expectedHiddenStatus) {
        assertEquals(expectedId, cgmesTapChangerAttributes.getId());
        assertEquals(expectedType, cgmesTapChangerAttributes.getType());
        assertEquals(expectedControlId, cgmesTapChangerAttributes.getControlId());
        assertEquals(expectedStep, cgmesTapChangerAttributes.getStep());
        assertEquals(expectedHiddenStatus, cgmesTapChangerAttributes.isHidden());
    }
}
