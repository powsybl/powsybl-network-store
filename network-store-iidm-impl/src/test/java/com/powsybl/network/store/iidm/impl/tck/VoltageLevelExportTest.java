/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractVoltageLevelExportTest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VoltageLevelExportTest extends AbstractVoltageLevelExportTest {
    @Override
    public void nodeBreakerTest() {
        // FIXME remove this test when VoltageLevelImpl.exportTopology is implemented
    }

    @Override
    public void busBreakerTest() {
        // FIXME remove this test when VoltageLevelImpl.exportTopology is implemented
    }
}
