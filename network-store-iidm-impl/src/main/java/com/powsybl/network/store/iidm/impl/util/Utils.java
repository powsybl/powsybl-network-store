/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.util;

import com.powsybl.network.store.model.BranchAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class Utils {
    private Utils() {

    }

    public static <T extends BranchAttributes> boolean removeConnectionPositionForBranches(Resource<T> resource) {
        boolean isRemoved = false;
        if (resource.getAttributes().getPosition1() != null) {
            resource.getAttributes().setPosition1(null);
            isRemoved = true;
        }
        if (resource.getAttributes().getPosition2() != null) {
            resource.getAttributes().setPosition2(null);
            isRemoved = true;
        }
        return isRemoved;
    }
}
