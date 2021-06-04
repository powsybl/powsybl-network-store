package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.*;

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
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        JsonNode unmappedNode = node.get("unmapped");
        Set<String> unmapped = new HashSet<>();
        for (final JsonNode element : unmappedNode) {
            unmapped.add(element.asText());
        }

        Map<String, Set<String>> busTopologicalNodeMap = new HashMap<>();
        JsonNode busTopologicalNodeMapNode = node.get("busTopologicalNodeMap");
        Iterator<Map.Entry<String, JsonNode>> fields = busTopologicalNodeMapNode.fields();
        Set<String> topologicalNodes = new HashSet<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String   bus  = field.getKey();
            JsonNode topologicalNodesArray = field.getValue();
            for (final JsonNode element : topologicalNodesArray) {
                topologicalNodes.add(element.asText());
            }
            busTopologicalNodeMap.put(bus, topologicalNodes);
            topologicalNodes = new HashSet<>();
        }

        Map<TerminalRefAttributes, String> equipmentSideTopologicalNodeMap = new HashMap<>();
        JsonNode equipmentSideTopologicalNodeMapNode = node.get("equipmentSideTopologicalNodeMap");
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
