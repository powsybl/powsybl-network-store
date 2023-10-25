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

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<String, String> properties) {

    }

    @Override
    public boolean isFictitious() {
        return false;
    }

    @Override
    public void setFictitious(boolean fictitious) {

    }

    @Override
    public Set<String> getAliasesWithoutType() {
        return null;
    }

    @Override
    public void setAliasesWithoutType(Set<String> aliasesWithoutType) {

    }

    @Override
    public Map<String, String> getAliasByType() {
        return null;
    }

    @Override
    public void setAliasByType(Map<String, String> aliasByType) {

    }
}
