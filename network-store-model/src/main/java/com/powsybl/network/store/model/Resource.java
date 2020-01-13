/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

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

    public static class Builder<T extends IdentifiableAttributes> {

        private final ResourceType type;

        private String id;

        private T attributes;

        public Builder(ResourceType type) {
            this.type = Objects.requireNonNull(type);
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
            return new Resource<>(type, id, attributes);
        }
    }

    public static Builder<NetworkAttributes> networkBuilder() {
        return new Builder<>(ResourceType.NETWORK);
    }

    public static Builder<SubstationAttributes> substationBuilder() {
        return new Builder<>(ResourceType.SUBSTATION);
    }

    public static Builder<VoltageLevelAttributes> voltageLevelBuilder() {
        return new Builder<>(ResourceType.VOLTAGE_LEVEL);
    }

    public static Builder<LoadAttributes> loadBuilder() {
        return new Builder<>(ResourceType.LOAD);
    }

    public static Builder<GeneratorAttributes> generatorBuilder() {
        return new Builder<>(ResourceType.GENERATOR);
    }

    public static Builder<ShuntCompensatorAttributes> shuntCompensatorBuilder() {
        return new Builder<>(ResourceType.SHUNT_COMPENSATOR);
    }

    public static Builder<VscConverterStationAttributes> vscConverterStationBuilder() {
        return new Builder<>(ResourceType.VSC_CONVERTER_STATION);
    }

    public static Builder<LccConverterStationAttributes> lccConverterStationBuilder() {
        return new Builder<>(ResourceType.LCC_CONVERTER_STATION);
    }

    public static Builder<StaticVarCompensatorAttributes> staticVarCompensatorBuilder() {
        return new Builder<>(ResourceType.STATIC_VAR_COMPENSATOR);
    }

    public static Builder<SwitchAttributes> switchBuilder() {
        return new Builder<>(ResourceType.SWITCH);
    }

    public static Builder<BusbarSectionAttributes> busbarSectionBuilder() {
        return new Builder<>(ResourceType.BUSBAR_SECTION);
    }

    public static Builder<TwoWindingsTransformerAttributes> twoWindingsTransformerBuilder() {
        return new Builder<>(ResourceType.TWO_WINDINGS_TRANSFORMER);
    }

    public static Builder<LineAttributes> lineBuilder() {
        return new Builder<>(ResourceType.LINE);
    }

    public static Builder<HvdcLineAttributes> hvdcLineBuilder() {
        return new Builder<>(ResourceType.HVDC_LINE);
    }

    public static Builder<DanglingLineAttributes> danglingLineBuilder() {
        return new Builder<>(ResourceType.DANGLING_LINE);
    }
}
