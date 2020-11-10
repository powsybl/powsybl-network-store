/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl extends AbstractTapChanger<TapChangerParent, PhaseTapChangerImpl, PhaseTapChangerAttributes> implements PhaseTapChanger {

    public PhaseTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, PhaseTapChangerAttributes attributes) {
        super(parent, index, attributes);
    }

    @Override
    public RegulationMode getRegulationMode() {
        return attributes.getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = attributes.getRegulationMode();
        attributes.setRegulationMode(regulationMode);
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return attributes.getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        double oldValue = attributes.getRegulationValue();
        attributes.setRegulationValue(regulationValue);
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationValue);
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
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new PhaseTapChangerStepImpl(this, attributes.getSteps().get(tapPosition - attributes.getLowTapPosition()));
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new PhaseTapChangerStepImpl(this, attributes.getSteps().get(attributes.getTapPosition() - attributes.getLowTapPosition()));

    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    protected String getTapChangerAttribute() {
        return "phase" + parent.getTapChangerAttribute();
    }
}
