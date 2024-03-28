package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Tie line attributes")
public class TieLineAttributes extends AbstractIdentifiableAttributes {

    @Schema(description = "Dangling line side 1 ID")
    private String danglingLine1Id;

    @Schema(description = "Dangling line side 2 ID")
    private String danglingLine2Id;
}
