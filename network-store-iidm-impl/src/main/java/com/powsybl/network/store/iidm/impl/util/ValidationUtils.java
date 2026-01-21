/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.util;

import com.powsybl.commons.PowsyblException;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ValidationUtils {

    static final int MAX_ID_LENGTH = 255;

    private ValidationUtils() {
    }

    public static void checkMaxIdLength(String type, String id) {
        if (id != null && id.length() > MAX_ID_LENGTH) {
            String idHead = id.substring(0, 25);
            String idTail = id.substring(id.length() - 25);
            throw new PowsyblException(type
                    + " id is too long: '"
                    + idHead + "..." + idTail + "' (len=" + id.length() + ", max=255)");
        }
    }
}
