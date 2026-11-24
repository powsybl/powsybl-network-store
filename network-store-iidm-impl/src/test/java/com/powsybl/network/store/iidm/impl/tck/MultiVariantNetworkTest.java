/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractMultiVariantNetworkTest;
import org.junit.jupiter.api.Disabled;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

@Disabled("Multi-thread access to variants is not supported in the network store")
class MultiVariantNetworkTest extends AbstractMultiVariantNetworkTest {
}
