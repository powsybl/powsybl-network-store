/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MinMaxReactiveLimitsImpl extends AbstractPropertiesHolder implements MinMaxReactiveLimits {

    private final MinMaxReactiveLimitsAttributes attributes;
    private final AbstractInjectionImpl<?, ?> owner;

    MinMaxReactiveLimitsImpl(MinMaxReactiveLimitsAttributes attributes, AbstractInjectionImpl<?, ?> injection) {
        this.attributes = attributes;
        this.owner = injection;
    }

    @Override
    public double getMinQ() {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ() {
        return attributes.getMaxQ();
    }

    @Override
    public ReactiveLimitsKind getKind() {
        return ReactiveLimitsKind.MIN_MAX;
    }

    @Override
    public double getMinQ(double p) {
        return attributes.getMinQ();
    }

    @Override
    public double getMaxQ(double p) {
        return attributes.getMaxQ();
    }

    @Override
    protected Map<String, String> getProperties() {
        return attributes.getProperties();
    }

    @Override
    protected void setProperties(Map<String, String> properties) {
        attributes.setProperties(properties);
    }

    @Override
    protected void persistProperties(Map<String, String> properties) {
        owner.updateResourceWithoutNotification(r -> setProperties(properties));
    }
}
