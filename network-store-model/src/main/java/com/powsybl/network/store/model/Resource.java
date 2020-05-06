/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ApiModel(value = "Resource", description = "Resource compliant with Json API spec")
@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = ResourceDeserializer.class)
public class Resource<T extends IdentifiableAttributes> {

    @ApiModelProperty(value = "Resource type", required = true)
    private ResourceType type;

    @ApiModelProperty(value = "Resource ID", required = true)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Resource attributes")
    private T attributes;

    @JsonIgnore
    private UUID networkUuid;

    @JsonIgnore
    private UpdateResource storeClient;

    public static class Builder<T extends IdentifiableAttributes> {

        private final ResourceType type;

        private String id;

        private T attributes;

        private UUID networkUuid;

        private UpdateResource storeClient;

        public Builder(ResourceType type, UUID networkUuid, UpdateResource storeClient) {
            this.type = Objects.requireNonNull(type);
            this.networkUuid = networkUuid;
            this.storeClient = storeClient;
        }

        public Builder<T> id(String id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder<T> attributes(T attributes) {
            this.attributes = Objects.requireNonNull(attributes);
            return this;
        }

        public Resource<T> build() {
            if (id == null) {
                throw new IllegalStateException("ID is not set");
            }
            if (attributes == null) {
                throw new IllegalStateException("attributes is not set");
            }

            if (networkUuid == null && storeClient == null) {
                return new Resource<>(type, id, attributes, networkUuid, storeClient);
            } else {
                Resource<T> resource = new Resource<>(type, id, null, networkUuid, storeClient);
                T attributesSpyer = AttributesSpyer.create(attributes, type);
                resource.setAttributes(attributesSpyer);
                attributesSpyer.setResource(resource);
                return resource;
            }
        }
    }

    public static Builder<NetworkAttributes> networkBuilder() {
        return new Builder<>(ResourceType.NETWORK, null, null);
    }

    public static Builder<SubstationAttributes> substationBuilder() {
        return new Builder<>(ResourceType.SUBSTATION, null, null);
    }

    public static Builder<VoltageLevelAttributes> voltageLevelBuilder() {
        return new Builder<>(ResourceType.VOLTAGE_LEVEL, null, null);
    }

    public static Builder<LoadAttributes> loadBuilder() {
        return loadBuilder(null, null);
    }

    public static Builder<LoadAttributes> loadBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.LOAD, networkUuid, storeClient);
    }

    public static Builder<GeneratorAttributes> generatorBuilder() {
        return generatorBuilder(null, null);
    }

    public static Builder<GeneratorAttributes> generatorBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.GENERATOR, networkUuid, storeClient);
    }

    public static Builder<ShuntCompensatorAttributes> shuntCompensatorBuilder() {
        return shuntCompensatorBuilder(null, null);
    }

    public static Builder<ShuntCompensatorAttributes> shuntCompensatorBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.SHUNT_COMPENSATOR, networkUuid, storeClient);
    }

    public static Builder<VscConverterStationAttributes> vscConverterStationBuilder() {
        return vscConverterStationBuilder(null, null);
    }

    public static Builder<VscConverterStationAttributes> vscConverterStationBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.VSC_CONVERTER_STATION, networkUuid, storeClient);
    }

    public static Builder<LccConverterStationAttributes> lccConverterStationBuilder() {
        return lccConverterStationBuilder(null, null);
    }

    public static Builder<LccConverterStationAttributes> lccConverterStationBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.LCC_CONVERTER_STATION, networkUuid, storeClient);
    }

    public static Builder<StaticVarCompensatorAttributes> staticVarCompensatorBuilder() {
        return staticVarCompensatorBuilder(null, null);
    }

    public static Builder<StaticVarCompensatorAttributes> staticVarCompensatorBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.STATIC_VAR_COMPENSATOR, networkUuid, storeClient);
    }

    public static Builder<SwitchAttributes> switchBuilder() {
        return switchBuilder(null, null);
    }

    public static Builder<SwitchAttributes> switchBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.SWITCH, networkUuid, storeClient);
    }

    public static Builder<BusbarSectionAttributes> busbarSectionBuilder() {
        return new Builder<>(ResourceType.BUSBAR_SECTION, null, null);
    }

    public static Builder<TwoWindingsTransformerAttributes> twoWindingsTransformerBuilder() {
        return twoWindingsTransformerBuilder(null, null);
    }

    public static Builder<TwoWindingsTransformerAttributes> twoWindingsTransformerBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.TWO_WINDINGS_TRANSFORMER, networkUuid, storeClient);
    }

    public static Builder<ThreeWindingsTransformerAttributes> threeWindingsTransformerBuilder() {
        return threeWindingsTransformerBuilder(null, null);
    }

    public static Builder<ThreeWindingsTransformerAttributes> threeWindingsTransformerBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.THREE_WINDINGS_TRANSFORMER, networkUuid, storeClient);
    }

    public static Builder<LineAttributes> lineBuilder() {
        return lineBuilder(null, null);
    }

    public static Builder<LineAttributes> lineBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.LINE, networkUuid, storeClient);
    }

    public static Builder<HvdcLineAttributes> hvdcLineBuilder() {
        return hvdcLineBuilder(null, null);
    }

    public static Builder<HvdcLineAttributes> hvdcLineBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.HVDC_LINE, networkUuid, storeClient);
    }

    public static Builder<DanglingLineAttributes> danglingLineBuilder() {
        return danglingLineBuilder(null, null);
    }

    public static Builder<DanglingLineAttributes> danglingLineBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.DANGLING_LINE, networkUuid, storeClient);
    }

    public static Builder<ConfiguredBusAttributes> configuredBusBuilder() {
        return configuredBusBuilder(null, null);
    }

    public static Builder<ConfiguredBusAttributes> configuredBusBuilder(UUID networkUuid, UpdateResource storeClient) {
        return new Builder<>(ResourceType.CONFIGURED_BUS, networkUuid, storeClient);
    }
}
