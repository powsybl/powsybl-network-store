/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractMergeNetworkTest;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MergeNetworkTest extends AbstractMergeNetworkTest {

    @Override
    public void checkMergingDifferentFormat() {
        // FIXME
    }

    @Override
    public void testMerge() {
        // FIXME
    }

    @Override
    public void failMergeIfMultiVariants() {
        // FIXME
    }

    @Override
    public void failMergeWithSameObj() {
        // FIXME
    }

    @Override
    public void multipleDanglingLinesInMergedNetwork() {
        // FIXME
    }

    @Override
    public void test() {
        // FIXME
    }

    @Override
    public void mergeThenCloneVariantBug() {
        // FIXME
    }

    @Override
    public void multipleDanglingLinesInMergingNetwork() {
        // FIXME
    }

    @Override
    public void checkMergingSameFormat() {
        // FIXME
    }

    @Override
    public void testMergeAndDetach() {
        // FIXME
    }

    @Override
    public void testMergeAndDetachWithExtensions() {
        // FIXME
    }

    @Override
    public void failDetachWithALineBetween2Subnetworks() {
        //FIXME
    }

    @Override
    public void failDetachIfMultiVariants() {
        //FIXME
    }

    @Test
    public void testMerge3Networks() {
        // FIXME
    }

    @Test
    public void failMergeDanglingLinesWithSameId() {
        // FIXME
    }

    @Test
    public void testValidationLevelWhenMerging2Eq() {
        // FIXME
    }

    @Test
    public void testValidationLevelWhenMergingEqAndSsh() {
        // FIXME
    }

    @Test
    public void testValidationLevelWhenMerging2Ssh() {
        // FIXME
    }

    @Test
    void failMergeOnlyOneNetwork() {
        // FIXME
    }

    @Test
    void failMergeOnSubnetworks() {
        // FIXME
    }

    @Test
    void failMergeSubnetworks() {
        // FIXME
    }

    @Test
    void failMergeContainingSubnetworks() {
        // FIXME
    }

    @Test
    void testNoEmptyAdditionalSubnetworkIsCreated() {
        // FIXME
    }

    @Test
    public void testListeners() {
        // FIXME
    }

    @Test
    public void dontCreateATieLineWithAlreadyMergedDanglingLinesInMergedNetwork() {
        // FIXME
    }

    @Test
    public void dontCreateATieLineWithAlreadyMergedDanglingLinesInMergingNetwork() {
        // FIXME
    }

    @Test
    public void multipleConnectedDanglingLinesInMergedNetwork() {
        // FIXME
    }

    @Test
    public void multipleConnectedDanglingLinesWithSamePairingKey() {
       // FIXME
    }
}