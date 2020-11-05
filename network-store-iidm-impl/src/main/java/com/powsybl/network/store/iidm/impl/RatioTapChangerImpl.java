/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.RatioTapChangerAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes> implements RatioTapChanger {

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, RatioTapChangerAttributes attributes) {
        super(parent, index, attributes);
    }

    @Override
    public double getTargetV() {
        return attributes.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        double oldValue = attributes.getTargetV();
        attributes.setTargetV(targetV);
        notifyUpdate(() -> getTapChangerAttribute() + ".targetV", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return attributes.isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        boolean oldValue = attributes.isLoadTapChangingCapabilities();
        attributes.setLoadTapChangingCapabilities(status);
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, status);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public int getStepCount() {
        return attributes.getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return new RatioTapChangerStepImpl(this, attributes.getSteps().get(tapPosition - attributes.getLowTapPosition()));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new RatioTapChangerStepImpl(this, attributes.getSteps().get(attributes.getTapPosition() - attributes.getLowTapPosition()));
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }
}
