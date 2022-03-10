package com.powsybl.network.store.server;

import java.util.UUID;

interface DatabaseAdapterService {
    default Object adaptUUID(UUID uuid) {
        return uuid;
    }
}
