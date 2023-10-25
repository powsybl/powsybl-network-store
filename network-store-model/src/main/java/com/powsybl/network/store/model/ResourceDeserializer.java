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
                case BATTERY:
                    return BatteryAttributes.class;
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
                case TIE_LINE:
                    return TieLineAttributes.class;
                case SUBNETWORK:
                    return SubnetworkAttributes.class;
                default:
                    throw new IllegalStateException("Unknown resource type: " + type);
            }
        } else {
            if (filter == AttributeFilter.SV) {
                switch (type) {
                    case NETWORK:
                        return NetworkAttributes.class;
                    case SUBSTATION:
                        return SubstationAttributes.class;
                    case VOLTAGE_LEVEL:
                        return VoltageLevelSvAttributes.class;
                    case LOAD:
                    case GENERATOR:
                    case BATTERY:
                    case VSC_CONVERTER_STATION:
                    case LCC_CONVERTER_STATION:
                    case SHUNT_COMPENSATOR:
                    case STATIC_VAR_COMPENSATOR:
                    case DANGLING_LINE:
                        return InjectionSvAttributes.class;
                    case BUSBAR_SECTION:
                        return BusbarSectionAttributes.class;
                    case SWITCH:
                        return SwitchAttributes.class;
                    case TWO_WINDINGS_TRANSFORMER:
                    case LINE:
                        return BranchSvAttributes.class;
                    case THREE_WINDINGS_TRANSFORMER:
                        return ThreeWindingsTransformerSvAttributes.class;
                    case HVDC_LINE:
                        return HvdcLineAttributes.class;
                    case CONFIGURED_BUS:
                        return ConfiguredBusAttributes.class;
                    default:
                        throw new IllegalStateException("Unknown resource type: " + type);
                }
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
        String parentNetwork = null;

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
                    case "variantNum":
                        variantNum = parser.nextIntValue(-1);
                        break;
                    case "filter":
                        filter = AttributeFilter.valueOf(parser.nextTextValue());
                        break;
                    case "attributes":
                        parser.nextValue();
                        attributes = parser.readValueAs(getTypeClass(type, filter));
                        break;
                    case "parentNetwork":
                        parentNetwork = parser.nextTextValue();
                    default:
                        break;
                }
            } else if (token == JsonToken.END_OBJECT) {
                break;
            }
        }

        return Resource.create(type, id, parentNetwork, variantNum, filter, attributes);
    }
}
