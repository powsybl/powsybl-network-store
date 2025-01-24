/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractNodeBreakerInternalConnectionsTest;
import org.junit.jupiter.api.Disabled;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerInternalConnectionsTest extends AbstractNodeBreakerInternalConnectionsTest {

    @Disabled
    public void testTraversalInternalConnections() {
        // FIXME: Expected result in powsybl-tck is wrong, actual result obtained here is right. Remove this when fixed in powsybl-core.
    }
}
