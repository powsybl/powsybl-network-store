/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.util;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.network.store.iidm.impl.AbstractBranchImpl;
import com.powsybl.network.store.model.BranchAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class Utils {
    private Utils() {

    }

    public static <T extends Branch<T> & Connectable<T>, U extends BranchAttributes> boolean removeConnectionPositionForBranches(AbstractBranchImpl<T, U> branch) {
        var attributes = branch.getResource().getAttributes();
        boolean hasPosition1 = attributes.getPosition1() != null;
        boolean hasPosition2 = attributes.getPosition2() != null;
        if (!hasPosition1 && !hasPosition2) {
            return false;
        }
        if (hasPosition1) {
            branch.updateResourceWithoutNotification(res -> res.getAttributes().setPosition1(null));
        }
        if (hasPosition2) {
            branch.updateResourceWithoutNotification(res -> res.getAttributes().setPosition2(null));
        }
        return true;
    }
}
