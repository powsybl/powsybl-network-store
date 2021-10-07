package com.powsybl.network.store.server;

import java.util.UUID;
import java.nio.ByteBuffer;

public class OracleAdapterService implements DatabaseAdapterService {

    @Override
    public Object adaptUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}
