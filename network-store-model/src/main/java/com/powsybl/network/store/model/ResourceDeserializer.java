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

    private static Class<? extends Attributes> getTypeClass(ResourceType type, AttributeFilter filter) {
        Objects.requireNonNull(type);
        if (filter == null) {
            return switch (type) {
                case NETWORK -> NetworkAttributes.class;
                case SUBSTATION -> SubstationAttributes.class;
                case VOLTAGE_LEVEL -> VoltageLevelAttributes.class;
                case LOAD -> LoadAttributes.class;
                case GENERATOR -> GeneratorAttributes.class;
                case BATTERY -> BatteryAttributes.class;
                case VSC_CONVERTER_STATION -> VscConverterStationAttributes.class;
                case LCC_CONVERTER_STATION -> LccConverterStationAttributes.class;
                case SHUNT_COMPENSATOR -> ShuntCompensatorAttributes.class;
                case STATIC_VAR_COMPENSATOR -> StaticVarCompensatorAttributes.class;
                case BUSBAR_SECTION -> BusbarSectionAttributes.class;
                case SWITCH -> SwitchAttributes.class;
                case TWO_WINDINGS_TRANSFORMER -> TwoWindingsTransformerAttributes.class;
                case THREE_WINDINGS_TRANSFORMER -> ThreeWindingsTransformerAttributes.class;
                case LINE -> LineAttributes.class;
                case HVDC_LINE -> HvdcLineAttributes.class;
                case DANGLING_LINE -> DanglingLineAttributes.class;
                case GROUND -> InjectionAttributes.class;
                case CONFIGURED_BUS -> ConfiguredBusAttributes.class;
                case TIE_LINE -> TieLineAttributes.class;
            };
        } else {
            if (filter == AttributeFilter.SV) {
                return switch (type) {
                    case NETWORK -> NetworkAttributes.class;
                    case SUBSTATION -> SubstationAttributes.class;
                    case VOLTAGE_LEVEL -> VoltageLevelSvAttributes.class;
                    case LOAD, GENERATOR, BATTERY, VSC_CONVERTER_STATION, LCC_CONVERTER_STATION, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, DANGLING_LINE ->
                        InjectionSvAttributes.class;
                    case BUSBAR_SECTION -> BusbarSectionAttributes.class;
                    case SWITCH -> SwitchAttributes.class;
                    case TWO_WINDINGS_TRANSFORMER, LINE -> BranchSvAttributes.class;
                    case THREE_WINDINGS_TRANSFORMER -> ThreeWindingsTransformerSvAttributes.class;
                    case HVDC_LINE -> HvdcLineAttributes.class;
                    case CONFIGURED_BUS -> ConfiguredBusAttributes.class;
                    default -> throw new IllegalStateException("Unknown resource type: " + type);
                };
            } else {
                throw new IllegalStateException("Unknown attribute filter: " + filter);
            }
        }
    }

    @Override
    public Resource deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ResourceType type = null;
        String id = null;
        int variantNum = -1;
        Attributes attributes = null;
        AttributeFilter filter = null;

        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();
                switch (fieldName) {
                    case "type" -> type = ResourceType.valueOf(parser.nextTextValue());
                    case "id" -> id = parser.nextTextValue();
                    case "variantNum" -> variantNum = parser.nextIntValue(-1);
                    case "filter" -> filter = AttributeFilter.valueOf(parser.nextTextValue());
                    case "attributes" -> {
                        parser.nextValue();
                        attributes = parser.readValueAs(getTypeClass(type, filter));
                    }
                    default -> {
                        // Do nothing
                    }
                }
            } else if (token == JsonToken.END_OBJECT) {
                break;
            }
        }

        return Resource.create(type, id, variantNum, filter, attributes);
    }
}
