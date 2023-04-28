package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tie line attributes")
public class TieLineAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @Schema(description = "Tie line half1 id")
    private String half1Id;

    @Schema(description = "Tie line half2 id")
    private String half2Id;

    @Schema(description = "Tie line name")
    private String name;

    @Builder.Default
    @Schema(description = "fictitious")
    private boolean fictitious = false;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    private Set<String> aliasesWithoutType;

    @Schema(description = "Alias by type")
    private Map<String, String> aliasByType;
}
