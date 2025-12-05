/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadConnectionType;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import com.powsybl.network.store.model.LoadAsymmetricalAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LoadAsymmetricalImpl extends AbstractExtension<Load> implements LoadAsymmetrical {

    public LoadAsymmetricalImpl(Load load) {
        super(load);
    }

    private LoadImpl getLoad() {
        return (LoadImpl) getExtendable();
    }

    private LoadAsymmetricalAttributes getLoadAsymmetricalAttributes() {
        return (LoadAsymmetricalAttributes) getLoad().getResource().getAttributes().getExtensionAttributes().get(LoadAsymmetrical.NAME);
    }

    @Override
    public LoadConnectionType getConnectionType() {
        return getLoadAsymmetricalAttributes().getConnectionType();
    }

    @Override
    public LoadAsymmetrical setConnectionType(LoadConnectionType loadConnectionType) {
        LoadConnectionType oldValue = getConnectionType();
        if (oldValue != loadConnectionType) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setConnectionType(loadConnectionType), "connectionType", oldValue, loadConnectionType);
        }
        return this;
    }

    @Override
    public double getDeltaPa() {
        return getLoadAsymmetricalAttributes().getDeltaPa();
    }

    @Override
    public LoadAsymmetrical setDeltaPa(double deltaPa) {
        double oldValue = getDeltaPa();
        if (oldValue != deltaPa) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaPa(deltaPa), "deltaPa", oldValue, deltaPa);
        }
        return this;
    }

    @Override
    public double getDeltaPb() {
        return getLoadAsymmetricalAttributes().getDeltaPb();
    }

    @Override
    public LoadAsymmetrical setDeltaPb(double deltaPb) {
        double oldValue = getDeltaPb();
        if (oldValue != deltaPb) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaPb(deltaPb), "deltaPb", oldValue, deltaPb);
        }
        return this;
    }

    @Override
    public double getDeltaPc() {
        return getLoadAsymmetricalAttributes().getDeltaPc();
    }

    @Override
    public LoadAsymmetrical setDeltaPc(double deltaPc) {
        double oldValue = getDeltaPc();
        if (oldValue != deltaPc) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaPc(deltaPc), "deltaPc", oldValue, deltaPc);
        }
        return this;
    }

    @Override
    public double getDeltaQa() {
        return getLoadAsymmetricalAttributes().getDeltaQa();
    }

    @Override
    public LoadAsymmetrical setDeltaQa(double deltaQa) {
        double oldValue = getDeltaQa();
        if (oldValue != deltaQa) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaQa(deltaQa), "deltaQa", oldValue, deltaQa);
        }
        return this;
    }

    @Override
    public double getDeltaQb() {
        return getLoadAsymmetricalAttributes().getDeltaQb();
    }

    @Override
    public LoadAsymmetrical setDeltaQb(double deltaQb) {
        double oldValue = getDeltaQb();
        if (oldValue != deltaQb) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaQb(deltaQb), "deltaQb", oldValue, deltaQb);
        }
        return this;
    }

    @Override
    public double getDeltaQc() {
        return getLoadAsymmetricalAttributes().getDeltaQc();
    }

    @Override
    public LoadAsymmetrical setDeltaQc(double deltaQc) {
        double oldValue = getDeltaQc();
        if (oldValue != deltaQc) {
            getLoad().updateResourceExtension(this, res ->
                getLoadAsymmetricalAttributes().setDeltaQc(deltaQc), "deltaQc", oldValue, deltaQc);
        }
        return this;
    }
}
