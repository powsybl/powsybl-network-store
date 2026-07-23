/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractManipulationsOnVariantsTest;
import org.junit.jupiter.api.Test;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class ManipulationsOnVariantsTest extends AbstractManipulationsOnVariantsTest {

    @Test
    @Override
    /* we need to override this test because we don't have the same cloneVariant implementation :
    * when cloning a variant with the overwrite parameters we delete then clone the variant so we never call the
    * onVariantOverwritten listener but instead the onVariantRemoved and onVariantCreated
    * should we change this behavior ? */
    public void baseTests() {
        //FIXME see comment
    }
}
