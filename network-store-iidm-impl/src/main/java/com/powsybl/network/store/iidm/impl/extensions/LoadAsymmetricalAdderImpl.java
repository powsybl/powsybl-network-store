/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import com.powsybl.network.store.model.LoadAsymmetricalAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LoadAsymmetricalAdderImpl extends AbstractIidmExtensionAdder<Load, LoadAsymmetrical> implements LoadAsymmetricalAdder {
    private LoadConnectionType connectionType = LoadConnectionType.Y;
    private double deltaPa = 0;
    private double deltaQa = 0;
    private double deltaPb = 0;
    private double deltaQb = 0;
    private double deltaPc = 0;
    private double deltaQc = 0;

    public LoadAsymmetricalAdderImpl(Load load) {
        super(load);
    }

    @Override
    protected LoadAsymmetrical createExtension(Load load) {
        LoadAsymmetricalAttributes attributes = LoadAsymmetricalAttributes.builder()
            .connectionType(connectionType)
            .deltaQa(deltaQa)
            .deltaQb(deltaQb)
            .deltaQc(deltaQc)
            .deltaPa(deltaPa)
            .deltaPb(deltaPb)
            .deltaPc(deltaPc)
            .build();
        ((LoadImpl) load).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(LoadAsymmetrical.NAME, attributes));
        return new LoadAsymmetricalImpl(load);
    }

    @Override
    public LoadAsymmetricalAdder withConnectionType(LoadConnectionType loadConnectionType) {
        this.connectionType = loadConnectionType;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaPa(double deltaPa) {
        this.deltaPa = deltaPa;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaQa(double deltaQa) {
        this.deltaQa = deltaQa;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaPb(double deltaPb) {
        this.deltaPb = deltaPb;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaQb(double deltaQb) {
        this.deltaQb = deltaQb;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaPc(double deltaPc) {
        this.deltaPc = deltaPc;
        return this;
    }

    @Override
    public LoadAsymmetricalAdder withDeltaQc(double deltaQc) {
        this.deltaQc = deltaQc;
        return this;
    }

    @Override
    public Class<? super LoadAsymmetrical> getExtensionClass() {
        return LoadAsymmetrical.class;
    }
}
