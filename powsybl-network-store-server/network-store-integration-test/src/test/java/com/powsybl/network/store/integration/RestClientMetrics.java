/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RestClientMetrics {

    int oneGetterCallCount = 0;

    int allGetterCallCount = 0;

    Set<String> updatedUrls = new HashSet<>();

    void reset() {
        oneGetterCallCount = 0;
        allGetterCallCount = 0;
        updatedUrls.clear();
    }
}
