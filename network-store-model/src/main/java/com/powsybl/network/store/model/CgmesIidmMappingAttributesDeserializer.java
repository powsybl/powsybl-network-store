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

        JsonNode unmappedNode = mainNode.get("unmapped");
        Set<String> unmapped = new HashSet<>();
        for (final JsonNode node : unmappedNode) {
            unmapped.add(node.asText());
        }

        Map<String, Set<String>> busTopologicalNodeMap = new HashMap<>();
        JsonNode busTopologicalNodeMapNode = mainNode.get("busTopologicalNodeMap");
        Iterator<Map.Entry<String, JsonNode>> fields = busTopologicalNodeMapNode.fields();
        Set<String> topologicalNodes;
        while (fields.hasNext()) {
            topologicalNodes = new HashSet<>();
            Map.Entry<String, JsonNode> field = fields.next();
            String   bus  = field.getKey();
            JsonNode topologicalNodesArray = field.getValue();
            for (final JsonNode element : topologicalNodesArray) {
                topologicalNodes.add(element.asText());
            }
            busTopologicalNodeMap.put(bus, topologicalNodes);
        }

        Map<TerminalRefAttributes, String> equipmentSideTopologicalNodeMap = new HashMap<>();
        JsonNode equipmentSideTopologicalNodeMapNode = mainNode.get("equipmentSideTopologicalNodeMap");
        TerminalRefAttributes terminalRefAttributes;
        JsonNode terminalRefNode;
        for (final JsonNode element : equipmentSideTopologicalNodeMapNode) {
            terminalRefNode = element.get("terminalRefAttributes");
            terminalRefAttributes = new TerminalRefAttributes(terminalRefNode.get("connectableId").asText(), terminalRefNode.get("side").asText());
            equipmentSideTopologicalNodeMap.put(terminalRefAttributes, element.get("topologicalNodeId").asText());
        }

        return new CgmesIidmMappingAttributes(equipmentSideTopologicalNodeMap, busTopologicalNodeMap, unmapped);
    }
}
