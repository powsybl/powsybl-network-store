/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.sld.iidm.extensions.BranchStatus;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class BranchStatusImpl<C extends Connectable<C>> extends AbstractExtension<C> implements BranchStatus<C> {

    private Status status;

    public BranchStatusImpl(C branch) {
        super(branch);
    }

    public BranchStatusImpl(C branch, Status branchStatus) {
        super(branch);
        this.status = branchStatus;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public BranchStatus setStatus(Status branchStatus) {
        this.status = branchStatus;
        return this;
    }
}
