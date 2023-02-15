/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client.util;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ExecutorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtil.class);

    private ExecutorUtil() {
    }

    /**
     * Taken from https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ExecutorService.html
     */
    public static void shutdownAndAwaitTermination(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                service.shutdownNow();
                if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.error("Executor service did not terminate");
                }
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
    }

    public static void waitAllFutures(List<Future<?>> futures) {
        for (var future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedInterruptedException(e);
            } catch (ExecutionException e) {
                throw new UncheckedExecutionException(e);
            }
        }
    }
}
