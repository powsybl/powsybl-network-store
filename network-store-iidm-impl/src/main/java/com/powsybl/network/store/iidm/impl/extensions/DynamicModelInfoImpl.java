/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DynamicModelInfo;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.DynamicModelInfoAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
public class DynamicModelInfoImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements DynamicModelInfo<I> {

    public DynamicModelInfoImpl(I extendable) {
        super(extendable);
    }

    private AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return (AbstractIdentifiableImpl<?, ?>) getExtendable();
    }

    private DynamicModelInfoAttributes getDynamicModelInfoAttributes() {
        return (DynamicModelInfoAttributes) getIdentifiable().getResource().getAttributes().getExtensionAttributes().get(DynamicModelInfo.NAME);
    }

    private static DynamicModelInfoAttributes getAttributes(Resource<?> resource) {
        return (DynamicModelInfoAttributes) resource.getAttributes();
    }

    @Override
    public String getModelName() {
        return getDynamicModelInfoAttributes().getModelName();
    }

    @Override
    public void setModelName(String modelName) {
        getDynamicModelInfoAttributes().setModelName(modelName);
        String oldValue = getModelName();
        if (!StringUtils.equals(oldValue, modelName)) {
            getIdentifiable().updateResourceExtension(this, res -> getAttributes(res).setModelName(modelName), "modelName", oldValue, modelName);
        }
    }
}
