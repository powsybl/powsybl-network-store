/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class OverloadManagementSystemAdderImpl extends AbstractIdentifiableAdder<OverloadManagementSystemAdderImpl> implements OverloadManagementSystemAdder {

    OverloadManagementSystemAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public OverloadManagementSystem add() {
        // FIXME: implement this method
        return null;
    }

    @Override
    protected String getTypeDescription() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public OverloadManagementSystemAdder setEnabled(boolean enabled) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public OverloadManagementSystemAdder setMonitoredElementId(String monitoredElementId) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public OverloadManagementSystemAdder setMonitoredElementSide(ThreeSides monitoredElementSide) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public SwitchTrippingAdder newSwitchTripping() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public BranchTrippingAdder newBranchTripping() {
        // FIXME: implement this method
        return null;
    }

    @Override
    public ThreeWindingsTransformerTrippingAdder newThreeWindingsTransformerTripping() {
        // FIXME: implement this method
        return null;
    }
}
