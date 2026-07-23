/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * @author Mohamed Benrejeb <mohamed.ben-rejeb at rte-france.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class NetworkStoreServiceTest {

    @Mock
    private RestClient restClient;

    private static final String TRACE_KEY = "trace-id";

    @Test
    public void executorServicePropagatesContext() throws ExecutionException, InterruptedException {
        AtomicReference<ExecutorService> executorHolder = new AtomicReference<>();
        TriFunction<RestClient, PreloadingStrategy, ExecutorService, NetworkStoreClient> decorator = (rest, strategy, executor) -> {
            executorHolder.set(executor);
            NetworkStoreClient storeClient = mock(NetworkStoreClient.class);
            doNothing().when(storeClient).createNetworks(anyList());
            return storeClient;
        };

        ContextRegistry.getInstance().registerThreadLocalAccessor(TRACE_KEY, TraceHolderTest::get, TraceHolderTest::set, TraceHolderTest::clear);

        NetworkStoreService service = new NetworkStoreService(restClient, PreloadingStrategy.NONE, decorator);

        TraceHolderTest.set("trace-123");

        service.createNetwork("network-id", "source-format");

        ExecutorService executorService = executorHolder.get();
        assertTrue("executor should be wrapped in ContextExecutorService", executorService instanceof ContextExecutorService);

        assertEquals("trace-123", executorService.submit(TraceHolderTest::get).get());

        service.close();
    }

    private static final class TraceHolderTest {
        private static final ThreadLocal<String> TRACE_LOCAL = new ThreadLocal<>();

        private TraceHolderTest() {
        }

        static String get() {
            return TRACE_LOCAL.get();
        }

        static void set(String value) {
            TRACE_LOCAL.set(value);
        }

        static void clear() {
            TRACE_LOCAL.remove();
        }
    }
}
