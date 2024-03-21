package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ExtensionAttributes {

    // TODO Why use a custom toJson while we could just use mapper.writeValueAsString()??
    // Replace return and spaces from Json??
    // as implemented in implementations, this is useless as it's equivalent to mapper.writeValueAsString
    String toJson();

    void writeJson(JsonGenerator jsonGenerator);
}
