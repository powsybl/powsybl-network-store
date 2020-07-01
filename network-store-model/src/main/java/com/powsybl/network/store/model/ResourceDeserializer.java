/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourceDeserializer extends StdDeserializer<Resource> {

    public ResourceDeserializer() {
        super(Resource.class);
    }

    private static Class<? extends IdentifiableAttributes> getTypeClass(ResourceType type) {
        Objects.requireNonNull(type);
        switch (type) {
            case NETWORK:
                return NetworkAttributes.class;
            case SUBSTATION:
                return SubstationAttributes.class;
            case VOLTAGE_LEVEL:
                return VoltageLevelAttributes.class;
            case LOAD:
                return LoadAttributes.class;
            case GENERATOR:
                return GeneratorAttributes.class;
            case VSC_CONVERTER_STATION:
                return VscConverterStationAttributes.class;
            case LCC_CONVERTER_STATION:
                return LccConverterStationAttributes.class;
            case SHUNT_COMPENSATOR:
                return ShuntCompensatorAttributes.class;
            case STATIC_VAR_COMPENSATOR:
                return StaticVarCompensatorAttributes.class;
            case BUSBAR_SECTION:
                return BusbarSectionAttributes.class;
            case SWITCH:
                return SwitchAttributes.class;
            case TWO_WINDINGS_TRANSFORMER:
                return TwoWindingsTransformerAttributes.class;
            case THREE_WINDINGS_TRANSFORMER:
                return ThreeWindingsTransformerAttributes.class;
            case LINE:
                return LineAttributes.class;
            case HVDC_LINE:
                return HvdcLineAttributes.class;
            case DANGLING_LINE:
                return DanglingLineAttributes.class;
            case CONFIGURED_BUS:
                return ConfiguredBusAttributes.class;
            default:
                throw new IllegalStateException("Unknown resource type: " + type);
        }
    }

    @Override
    public Resource deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ResourceType type = null;
        String id = null;
        IdentifiableAttributes attributes = null;

        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();
                switch (fieldName) {
                    case "type":
                        type = ResourceType.valueOf(parser.nextTextValue());
                        break;
                    case "id":
                        id = parser.nextTextValue();
                        break;
                    case "attributes":
                        parser.nextValue();
                        attributes = parser.readValueAs(getTypeClass(type));
                        break;
                    default:
                        break;
                }
            } else if (token == JsonToken.END_OBJECT) {
                break;
            }
        }

        return new Resource<>(type, id, attributes, false, null);
    }
}
