package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type"
)
@JsonTypeIdResolver(ExtensionAttributesIdResolver.class)
public interface ExtensionAttributes {
}
