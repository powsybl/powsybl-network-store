/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.network.store.iidm.impl.LoadImpl;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LoadDetailAdderImpl extends AbstractExtensionAdder<Load, LoadDetail> implements LoadDetailAdder {

    private float fixedActivePower;

    private float fixedReactivePower;

    private float variableActivePower;

    private float variableReactivePower;

    public LoadDetailAdderImpl(Load load) {
        super(load);
    }

    @Override
    protected LoadDetail createExtension(Load load) {
        return new LoadDetailImpl((LoadImpl) load, fixedActivePower, fixedReactivePower, variableActivePower, variableReactivePower);
    }

    @Override
    public LoadDetailAdder withFixedActivePower(float fixedActivePower) {
        this.fixedActivePower = fixedActivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withFixedReactivePower(float fixedReactivePower) {
        this.fixedReactivePower = fixedReactivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withVariableActivePower(float variableActivePower) {
        this.variableActivePower = variableActivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withVariableReactivePower(float variableReactivePower) {
        this.variableReactivePower = variableReactivePower;
        return this;
    }
}
