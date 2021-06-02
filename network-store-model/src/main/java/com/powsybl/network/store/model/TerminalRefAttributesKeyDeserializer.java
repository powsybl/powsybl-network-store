package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.powsybl.commons.PowsyblException;

public class TerminalRefAttributesKeyDeserializer extends KeyDeserializer {
    @Override
    public TerminalRefAttributes deserializeKey(final String key,
                                  final DeserializationContext ctxt) {
        String[] values = key.split("[\\(, \\)=]");

        if (values.length != 6) {
            throw new PowsyblException("TerminalRefAttributes deserialization error: " + key + " is not a valid representation.");
        }
        if (!values[0].equals("TerminalRefAttributes")) {
            throw new PowsyblException("TerminalRefAttributes deserialization error: " + values[0] + " is not a valid object.");
        }
        return new TerminalRefAttributes(values[2], values[5]);
    }
}
