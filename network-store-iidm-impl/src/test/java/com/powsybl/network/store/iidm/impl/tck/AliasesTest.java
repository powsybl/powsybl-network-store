/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractAliasesTest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AliasesTest extends AbstractAliasesTest {
    @Override
    public void failWhenAliasTypeIsEmpty() {
        // FIXME
    }

    @Override
    public void failWhenAliasTypeIsNull() {
        // FIXME
    }

    @Override
    public void failWhenRemovingNonExistingAlias() {
        // FIXME
    }

    @Override
    public void failWhenAliasEqualToAnId() {
        // FIXME
    }

    @Override
    public void failWhenDuplicatedAlias() {
        // FIXME
    }

    @Override
    public void mergeFailWhenAliasEqualsToAnAliasOfOtherNetwork() {
        // FIXME
    }

    @Override
    public void mergeFailWhenAliasEqualsToAnIdOfOtherNetwork() {
        // FIXME
    }
}
