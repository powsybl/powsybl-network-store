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
    private ResourceUpdater resourceUpdater;

    public static class Builder<T extends IdentifiableAttributes> {

        private final ResourceType type;

        private String id;

        private T attributes;

        private ResourceUpdater resourceUpdater;

        public Builder(ResourceType type, ResourceUpdater resourceUpdater) {
            this.type = Objects.requireNonNull(type);
            this.resourceUpdater = resourceUpdater;
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

            Resource<T> resource = new Resource<>(type, id, attributes, null);
            if (resourceUpdater != null) {
                AttributesSpyer.spy(resource, resourceUpdater);
            }
            return resource;
        }
    }

    public static Builder<NetworkAttributes> networkBuilder() {
        return networkBuilder(null);
    }

    public static Builder<NetworkAttributes> networkBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.NETWORK, resourceUpdater);
    }

    public static Builder<SubstationAttributes> substationBuilder() {
        return new Builder<>(ResourceType.SUBSTATION, null);
    }

    public static Builder<VoltageLevelAttributes> voltageLevelBuilder() {
        return voltageLevelBuilder(null);
    }

    public static Builder<VoltageLevelAttributes> voltageLevelBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.VOLTAGE_LEVEL, resourceUpdater);
    }

    public static Builder<LoadAttributes> loadBuilder() {
        return loadBuilder(null);
    }

    public static Builder<LoadAttributes> loadBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.LOAD, resourceUpdater);
    }

    public static Builder<GeneratorAttributes> generatorBuilder() {
        return generatorBuilder(null);
    }

    public static Builder<GeneratorAttributes> generatorBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.GENERATOR, resourceUpdater);
    }

    public static Builder<ShuntCompensatorAttributes> shuntCompensatorBuilder() {
        return shuntCompensatorBuilder(null);
    }

    public static Builder<ShuntCompensatorAttributes> shuntCompensatorBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.SHUNT_COMPENSATOR, resourceUpdater);
    }

    public static Builder<VscConverterStationAttributes> vscConverterStationBuilder() {
        return vscConverterStationBuilder(null);
    }

    public static Builder<VscConverterStationAttributes> vscConverterStationBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.VSC_CONVERTER_STATION, resourceUpdater);
    }

    public static Builder<LccConverterStationAttributes> lccConverterStationBuilder() {
        return lccConverterStationBuilder(null);
    }

    public static Builder<LccConverterStationAttributes> lccConverterStationBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.LCC_CONVERTER_STATION, resourceUpdater);
    }

    public static Builder<StaticVarCompensatorAttributes> staticVarCompensatorBuilder() {
        return staticVarCompensatorBuilder(null);
    }

    public static Builder<StaticVarCompensatorAttributes> staticVarCompensatorBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.STATIC_VAR_COMPENSATOR, resourceUpdater);
    }

    public static Builder<SwitchAttributes> switchBuilder() {
        return switchBuilder(null);
    }

    public static Builder<SwitchAttributes> switchBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.SWITCH, resourceUpdater);
    }

    public static Builder<BusbarSectionAttributes> busbarSectionBuilder() {
        return new Builder<>(ResourceType.BUSBAR_SECTION, null);
    }

    public static Builder<TwoWindingsTransformerAttributes> twoWindingsTransformerBuilder() {
        return twoWindingsTransformerBuilder(null);
    }

    public static Builder<TwoWindingsTransformerAttributes> twoWindingsTransformerBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.TWO_WINDINGS_TRANSFORMER, resourceUpdater);
    }

    public static Builder<ThreeWindingsTransformerAttributes> threeWindingsTransformerBuilder() {
        return threeWindingsTransformerBuilder(null);
    }

    public static Builder<ThreeWindingsTransformerAttributes> threeWindingsTransformerBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.THREE_WINDINGS_TRANSFORMER, resourceUpdater);
    }

    public static Builder<LineAttributes> lineBuilder() {
        return lineBuilder(null);
    }

    public static Builder<LineAttributes> lineBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.LINE, resourceUpdater);
    }

    public static Builder<HvdcLineAttributes> hvdcLineBuilder() {
        return hvdcLineBuilder(null);
    }

    public static Builder<HvdcLineAttributes> hvdcLineBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.HVDC_LINE, resourceUpdater);
    }

    public static Builder<DanglingLineAttributes> danglingLineBuilder() {
        return danglingLineBuilder(null);
    }

    public static Builder<DanglingLineAttributes> danglingLineBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.DANGLING_LINE, resourceUpdater);
    }

    public static Builder<ConfiguredBusAttributes> configuredBusBuilder() {
        return configuredBusBuilder(null);
    }

    public static Builder<ConfiguredBusAttributes> configuredBusBuilder(ResourceUpdater resourceUpdater) {
        return new Builder<>(ResourceType.CONFIGURED_BUS, resourceUpdater);
    }
}
