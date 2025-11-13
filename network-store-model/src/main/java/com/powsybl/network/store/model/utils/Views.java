/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model.utils;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class Views {

    private Views() {}

    public static class SvView {
    }

    public static class Basic extends SvView {
    }

    public static class WithLimits extends Basic {
    }
}
