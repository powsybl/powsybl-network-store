/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.powsybl.iidm.network.Validable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Schema(description = "Resource compliant with Json API spec")
@ToString
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = ResourceDeserializer.class)
public class Resource<T extends IdentifiableAttributes> implements Validable {

    public static final int INITIAL_VARIANT_NUM = 0;

    @Schema(description = "Resource type", required = true)
    private ResourceType type;

    @Schema(description = "Resource ID", required = true)
    private String id;

    @Schema(description = "Variant number", required = true)
    private int variantNum;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Resource attributes")
    private T attributes;

    @JsonIgnore
    public String getMessageHeader() {
        return type.getDescription() + " '" + id + "': ";
    }

    public static <T extends IdentifiableAttributes> Resource<T> create(ResourceType type, String id, int variantNum, T attributes) {
        Objects.requireNonNull(attributes);
        Resource<T> resource = new Resource<>(type, id, variantNum, attributes);
        attributes.setResource(resource);
        return resource;
    }

    public static class Builder<T extends IdentifiableAttributes> {

        private final ResourceType type;

        private String id;

        private int variantNum = INITIAL_VARIANT_NUM;

        private T attributes;

        public Builder(ResourceType type) {
            this.type = Objects.requireNonNull(type);
        }

        public Builder<T> id(String id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder<T> variantNum(int variantNum) {
            this.variantNum = variantNum;
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
            if (variantNum < 0) {
                throw new IllegalStateException("Variant number is not set");
            }
            if (attributes == null) {
                throw new IllegalStateException("attributes is not set");
            }
            Resource<T> resource = new Resource<>(type, id, variantNum, attributes);
            attributes.setResource(resource);
            return resource;
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

    public static Builder<BatteryAttributes> batteryBuilder() {
        return new Builder<>(ResourceType.BATTERY);
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

    public static Builder<ThreeWindingsTransformerAttributes> threeWindingsTransformerBuilder() {
        return new Builder<>(ResourceType.THREE_WINDINGS_TRANSFORMER);
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

    public static Builder<ConfiguredBusAttributes> configuredBusBuilder() {
        return new Builder<>(ResourceType.CONFIGURED_BUS);
    }

    public static <T extends IdentifiableAttributes> List<Resource<T>> cloneResourcesToVariant(
        Collection<Resource<T>> resources, int newVariantNum,
        ObjectMapper objectMapper, Consumer<Resource<T>> resourcePostProcessor) {
        // use json serialization to clone the resources of source collection
        List<Resource<T>> clonedResources;
        try {
            var json = objectMapper.writeValueAsString(resources);
            clonedResources = objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        // reassign cloned resources to new variant number
        for (Resource<T> clonedResource : clonedResources) {
            clonedResource.setVariantNum(newVariantNum);
            if (resourcePostProcessor != null) {
                resourcePostProcessor.accept(clonedResource);
            }
        }
        return clonedResources;
    }
}
