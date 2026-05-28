/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.ReferencePrioritiesAttributes;
import com.powsybl.network.store.model.ReferencePriorityAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class ReferencePrioritiesTest {
    @Test
    void addReferencePriorityShouldUpdateResource() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        TwoWindingsTransformerImpl twt = (TwoWindingsTransformerImpl) network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerImpl twtSpy = Mockito.spy(twt);
        ReferencePrioritiesAdder<TwoWindingsTransformer> referencePrioritiesAdder = twtSpy.newExtension(ReferencePrioritiesAdder.class);
        ReferencePriorities<TwoWindingsTransformer> referencePriorities = referencePrioritiesAdder.add();

        Terminal expectedTerminal = twt.getTerminal1();
        int expectedPriority = 1;
        ReferencePriorityAdder rpAdder = referencePriorities.newReferencePriority()
                .setTerminal(expectedTerminal)
                .setPriority(expectedPriority);
        Mockito.clearInvocations(twtSpy);
        rpAdder.add();

        Map<String, ExtensionAttributes> extensionAttributes = twtSpy.getResource()
                .getAttributes()
                .getExtensionAttributes();

        ReferencePrioritiesAttributes referencePrioritiesAttributes = (ReferencePrioritiesAttributes) extensionAttributes.get(ReferencePriorities.NAME);
        ArgumentCaptor<ReferencePriorityAttributes> argumentCaptor = ArgumentCaptor.forClass(ReferencePriorityAttributes.class);
        Mockito.verify(twtSpy).updateResourceExtension(Mockito.eq(referencePriorities), Mockito.any(), Mockito.eq("referencePriorities.referencePriority"), Mockito.eq(null), argumentCaptor.capture());
        ReferencePriorityAttributes referencePriorityAttributesFromResourceUpdate = argumentCaptor.getValue();
        assertEquals(1, extensionAttributes.size());
        assertEquals(1, referencePrioritiesAttributes.getReferencePriorities().size());
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(expectedTerminal);
        assertCgmesTapChangerAttributes(referencePrioritiesAttributes.getReferencePriorities().getFirst(), terminalRefAttributes, expectedPriority);
        assertCgmesTapChangerAttributes(referencePriorityAttributesFromResourceUpdate, terminalRefAttributes, expectedPriority);
    }

    private void assertCgmesTapChangerAttributes(ReferencePriorityAttributes referencePriorityAttributes,
                                                 TerminalRefAttributes expectedTerminal,
                                                 int expectedPriority) {
        assertEquals(expectedTerminal, referencePriorityAttributes.getTerminal());
        assertEquals(expectedPriority, referencePriorityAttributes.getPriority());
    }
}
