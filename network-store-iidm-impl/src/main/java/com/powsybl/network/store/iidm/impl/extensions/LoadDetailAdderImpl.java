/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import com.powsybl.network.store.model.LoadDetailAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LoadDetailAdderImpl extends AbstractIidmExtensionAdder<Load, LoadDetail> implements LoadDetailAdder {

    private double fixedActivePower;

    private double fixedReactivePower;

    private double variableActivePower;

    private double variableReactivePower;

    public LoadDetailAdderImpl(Load load) {
        super(load);
    }

    @Override
    protected LoadDetail createExtension(Load load) {
        LoadDetailAttributes attributes = LoadDetailAttributes.builder()
                .fixedActivePower(fixedActivePower)
                .fixedReactivePower(fixedReactivePower)
                .variableActivePower(variableActivePower)
                .variableReactivePower(variableReactivePower)
                .build();
        ((LoadImpl) load).updateResource(res -> res.getAttributes().setLoadDetail(attributes));
        return new LoadDetailImpl((LoadImpl) load);
    }

    @Override
    public LoadDetailAdder withFixedActivePower(double fixedActivePower) {
        this.fixedActivePower = fixedActivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withFixedReactivePower(double fixedReactivePower) {
        this.fixedReactivePower = fixedReactivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withVariableActivePower(double variableActivePower) {
        this.variableActivePower = variableActivePower;
        return this;
    }

    @Override
    public LoadDetailAdder withVariableReactivePower(double variableReactivePower) {
        this.variableReactivePower = variableReactivePower;
        return this;
    }
}
