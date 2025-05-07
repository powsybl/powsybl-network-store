package com.powsybl.network.store.model;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class OperationalLimitsGroupIdentifierDeserializer extends KeyDeserializer {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public OperationalLimitsGroupIdentifier deserializeKey(String s, DeserializationContext deserializationContext) throws IOException {
        return mapper.readValue(s, OperationalLimitsGroupIdentifier.class);
    }
}
