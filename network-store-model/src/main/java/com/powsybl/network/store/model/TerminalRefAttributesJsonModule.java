package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class TerminalRefAttributesJsonModule extends SimpleModule {
    public TerminalRefAttributesJsonModule() {
        addKeyDeserializer(
                TerminalRefAttributes.class,
                new TerminalRefAttributesKeyDeserializer());
    }
}
