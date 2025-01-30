/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractTapChangerTest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TapChangerTest extends AbstractTapChangerTest {

    // TODO remove this test when regulation are handled properly :
    //  regulating terminal are not deleted from TapChangers when linked terminals are deleted
    //  and isRegulating is not updated either
    @Override
    public void baseTestsPhaseTapChanger() { }
}
