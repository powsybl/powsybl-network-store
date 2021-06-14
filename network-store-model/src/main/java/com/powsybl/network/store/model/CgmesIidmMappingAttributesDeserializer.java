/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesIidmMappingAttributesDeserializer extends StdDeserializer<CgmesIidmMappingAttributes> {
    public CgmesIidmMappingAttributesDeserializer() {
        this(null);
    }

    public CgmesIidmMappingAttributesDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CgmesIidmMappingAttributes deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        JsonNode mainNode = jsonParser.getCodec().readTree(jsonParser);

        Set<String> unmapped = new HashSet<>();
        for (JsonNode node : mainNode.get("unmapped")) {
            unmapped.add(node.asText());
        }

        Map<String, Set<String>> busTopologicalNodeMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> elements = mainNode.get("busTopologicalNodeMap").fields();
        Set<String> topologicalNodes;
        while (elements.hasNext()) {
            topologicalNodes = new HashSet<>();
            Map.Entry<String, JsonNode> element = elements.next();
            for (final JsonNode node : element.getValue()) {
                topologicalNodes.add(node.asText());
            }
            busTopologicalNodeMap.put(element.getKey(), topologicalNodes);
        }

        Map<TerminalRefAttributes, String> equipmentSideTopologicalNodeMap = new HashMap<>();
        JsonNode terminalRefNode;
        for (JsonNode node : mainNode.get("equipmentSideTopologicalNodeMap")) {
            terminalRefNode = node.get("terminalRefAttributes");
            equipmentSideTopologicalNodeMap.put(
                    new TerminalRefAttributes(terminalRefNode.get("connectableId").asText(), terminalRefNode.get("side").asText()),
                    node.get("topologicalNodeId").asText()
            );
        }

        return new CgmesIidmMappingAttributes(equipmentSideTopologicalNodeMap, busTopologicalNodeMap, unmapped);
    }
}
