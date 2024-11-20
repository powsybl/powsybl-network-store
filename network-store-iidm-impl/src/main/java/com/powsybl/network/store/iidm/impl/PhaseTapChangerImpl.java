/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.*;
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
        return PhaseTapChanger.RegulationMode.valueOf(getAttributes().getRegulatingPoint().getRegulationMode());
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(), getRegulationTerminal(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        PhaseTapChanger.RegulationMode oldValue = getRegulationMode();
        if (regulationMode != oldValue) {
            regulatingPoint.setRegulationMode(String.valueOf(regulationMode));
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        }
        return this;
    }

    @Override
    public double getRegulationValue() {
        return getAttributes().getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), regulationValue, isRegulating(), getRegulationTerminal(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getAttributes().getRegulationValue();
        parent.getTransformer().updateResource(res -> getAttributes(res).setRegulationValue(regulationValue));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), getRegulationValue(), regulating, getRegulationTerminal(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getPhaseTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());

        return super.setRegulating(regulating);
    }

    @Override
    public PhaseTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), getRegulationValue(), isRegulating(), regulationTerminal, parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
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
    protected Integer getRelativeNeutralPosition() {
        var steps = getAttributes().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            TapChangerStepAttributes stepAttributes = steps.get(i);
            if (stepAttributes.getRho() == 1 && stepAttributes.getAlpha() == 0) {
                return i;
            }
        }
        return null;
    }

    @Override
    public Optional<PhaseTapChangerStep> getNeutralStep() {
        Integer relativeNeutralPosition = getRelativeNeutralPosition();
        return relativeNeutralPosition != null ? Optional.of(new PhaseTapChangerStepImpl(this, relativeNeutralPosition)) : Optional.empty();
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

    public static void validateStep(TapChangerStepAttributes step, TapChangerParent parent) {
        AbstractTapChanger.validateStep(step, parent);
        if (Double.isNaN(step.getAlpha())) {
            throw new ValidationException(parent, "step alpha is not set");
        }
    }

    // equals and hashCode are overridden to ensure correct behavior of the PhaseTapChanger
    // in hash table-based collections (e.g., HashSet, HashMap). Without these overrides, the default
    // implementations include this.attributesGetter, which can lead to incorrect behavior in
    // hash-based collections by affecting instance identification, retrieval and removal.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhaseTapChangerImpl that = (PhaseTapChangerImpl) o;
        if (!Objects.equals(that.getTransformer().getClass(), getTransformer().getClass())) {
            return false;
        }
        // check phase tap changer are on same leg
        if (that.getTransformer() instanceof ThreeWindingsTransformerImpl &&
            !Objects.equals(((ThreeWindingsTransformerImpl.LegImpl) parent).getSide(),
                ((ThreeWindingsTransformerImpl.LegImpl) that.getParent()).getSide())) {
            return false;
        }
        return Objects.equals(getTransformer().getId(), that.getTransformer().getId()) &&
            Objects.equals(getRegulationMode(), that.getRegulationMode()) &&
            Objects.equals(getRegulationValue(), that.getRegulationValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParent(), getTransformer().getId(), getRegulationMode(), getRegulationValue());
    }
}
