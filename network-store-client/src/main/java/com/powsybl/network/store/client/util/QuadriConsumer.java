package com.powsybl.network.store.client.util;

public interface QuadriConsumer<K, V, S, Q> {
    void accept(K k, V v, S s, Q q);
}
