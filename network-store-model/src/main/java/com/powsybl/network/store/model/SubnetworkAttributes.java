package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sub network attributes")
public class SubnetworkAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @Schema(description = "Subnetwork UUID", required = true)
    private UUID uuid;

    @Schema(description = "Sub network name")
    private String name;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Builder.Default
    @Schema(description = "fictitious")
    private boolean fictitious = false;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;
}
