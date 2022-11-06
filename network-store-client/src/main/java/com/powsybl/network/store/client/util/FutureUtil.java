/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client.util;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class FutureUtil {

    private FutureUtil() {
    }

    public static void waitAll(List<Future<?>> futures) {
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
