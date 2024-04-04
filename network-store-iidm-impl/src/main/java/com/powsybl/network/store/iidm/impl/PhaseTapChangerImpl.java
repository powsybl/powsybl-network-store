/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseTapChangerImpl extends AbstractTapChanger<TapChangerParent, PhaseTapChangerImpl, PhaseTapChangerAttributes> implements PhaseTapChanger, Validable {

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    public PhaseTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "phase tap changer");
        this.attributesGetter = attributesGetter;
    }

    @Override
    protected PhaseTapChangerAttributes getAttributes() {
        return getAttributes(getResource());
    }

    protected PhaseTapChangerAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getPhaseTapChangerAttributes();
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getAttributes().getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(), getRegulationTerminal(), parent.getNetwork(), true);
        RegulationMode oldValue = getAttributes().getRegulationMode();
        parent.getTransformer().updateResource(res -> getAttributes(res).setRegulationMode(regulationMode));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return getAttributes().getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), regulationValue, isRegulating(), getRegulationTerminal(), parent.getNetwork(), true);
        double oldValue = getAttributes().getRegulationValue();
        parent.getTransformer().updateResource(res -> getAttributes(res).setRegulationValue(regulationValue));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), getRegulationValue(), regulating, getRegulationTerminal(), parent.getNetwork(), true);

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getPhaseTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, true);

        return super.setRegulating(regulating);
    }

    @Override
    public PhaseTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), getRegulationValue(), isRegulating(), regulationTerminal, parent.getNetwork(), true);
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public int getStepCount() {
        return getAttributes().getSteps().size();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        var attributes = getAttributes();
        int tapPositionIndex = tapPosition - attributes.getLowTapPosition();
        return new PhaseTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public PhaseTapChangerStepsReplacer stepsReplacer() {
        return new PhaseTapChangerStepsReplacerImpl(this);
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        var attributes = getAttributes();
        int tapPositionIndex = attributes.getTapPosition() - attributes.getLowTapPosition();
        return new PhaseTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public void remove() {
        parent.setPhaseTapChanger(null);
    }

    protected String getTapChangerAttribute() {
        return "phase" + parent.getTapChangerAttribute();
    }

    @Override
    public String getMessageHeader() {
        return "phaseTapChanger '" + parent.getTransformer().getId() + "': ";
    }

    @Override
    public PhaseTapChangerStepsReplacer stepsReplacer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stepsReplacer'");
    }

    public static void validateStep(TapChangerStepAttributes step, TapChangerParent parent) {
        AbstractTapChanger.validateStep(step, parent);
        if (Double.isNaN(step.getAlpha())) {
            throw new ValidationException(parent, "step alpha is not set");
        }
    }
}
