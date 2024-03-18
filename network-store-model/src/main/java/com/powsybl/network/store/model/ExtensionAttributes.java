package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) // Is this OK to use this?
public interface ExtensionAttributes {

    String toJson();
}
