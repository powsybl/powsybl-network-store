package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CgmesIidmMappingAttributesSerializer extends StdSerializer<CgmesIidmMappingAttributes> {

    public CgmesIidmMappingAttributesSerializer() {
        this(null);
    }

    public CgmesIidmMappingAttributesSerializer(Class<CgmesIidmMappingAttributes> cgmesIidmMappingAttributesClass) {
        super(cgmesIidmMappingAttributesClass);
    }

    @Override
    public void serialize(CgmesIidmMappingAttributes cgmesIidmMappingAttributes, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeArrayFieldStart("unmapped");
        for (String element : cgmesIidmMappingAttributes.getUnmapped()) {
            jsonGenerator.writeString(element);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeObjectFieldStart("busTopologicalNodeMap");
        for (String key : cgmesIidmMappingAttributes.getBusTopologicalNodeMap().keySet()) {
            jsonGenerator.writeArrayFieldStart(key);
            for (String node : cgmesIidmMappingAttributes.getBusTopologicalNodeMap().get(key)) {
                jsonGenerator.writeString(node);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();

        jsonGenerator.writeArrayFieldStart("equipmentSideTopologicalNodeMap");
        for (TerminalRefAttributes terminalRefAttributes : cgmesIidmMappingAttributes.getEquipmentSideTopologicalNodeMap().keySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectFieldStart("terminalRefAttributes");
            jsonGenerator.writeObjectField("connectableId", terminalRefAttributes.getConnectableId());
            jsonGenerator.writeObjectField("side", terminalRefAttributes.getSide());
            jsonGenerator.writeEndObject();

            jsonGenerator.writeObjectField("topologicalNodeId", cgmesIidmMappingAttributes.getEquipmentSideTopologicalNodeMap().get(terminalRefAttributes));
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
