/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.server;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class CassandraConstants {
    public static final String TERMINAL_REF = "terminalRef";
    public static final String REGULATING_TERMINAL = "regulatingTerminal";
    public static final String MIN_MAX_REACTIVE_LIMITS = "minMaxReactiveLimits";
    public static final String REACTIVE_CAPABILITY_CURVE = "reactiveCapabilityCurve";
    public static final String TARGET_V = "targetV";

    private CassandraConstants() {
    }
}
