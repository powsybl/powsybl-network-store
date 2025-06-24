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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes> implements RatioTapChanger, Validable {

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "ratio tap changer");
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
    }

    @Override
    protected RatioTapChangerAttributes getAttributes() {
        return getAttributes(getResource());
    }

    protected RatioTapChangerAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRatioTapChangerAttributes();
    }

    @Override
    public double getRegulationValue() {
        return getAttributes().getRegulationValue();
    }

    @Override
    public RatioTapChanger setRegulationValue(double regulationValue) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), getRegulationMode(), regulationValue, parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getAttributes().getRegulationValue();
        if (Double.compare(regulationValue, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulationValue(regulationValue),
                getTapChangerAttribute() + ".regulationValue", oldValue, regulationValue);
        }
        return this;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities, getRegulationTerminal(), getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        return super.setLoadTapChangingCapabilities(loadTapChangingCapabilities);
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, hasLoadTapChangingCapabilities(), getRegulationTerminal(), getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());

        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), regulationTerminal, getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public int getStepCount() {
        return getAttributes().getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        int tapPositionIndex = getTapPositionIndex(tapPosition);
        return new RatioTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public RatioTapChangerStepsReplacer stepsReplacer() {
        return new RatioTapChangerStepsReplacerImpl(this);
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        var attributes = getAttributes();
        int tapPositionIndex = attributes.getTapPosition() - attributes.getLowTapPosition();
        return new RatioTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public Optional<RatioTapChangerStep> getNeutralStep() {
        Integer relativeNeutralPosition = getRelativeNeutralPosition();
        return relativeNeutralPosition != null ? Optional.of(new RatioTapChangerStepImpl(this, relativeNeutralPosition)) : Optional.empty();
    }

    @Override
    protected Integer getRelativeNeutralPosition() {
        var steps = getAttributes().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            TapChangerStepAttributes stepAttributes = steps.get(i);
            if (stepAttributes.getRho() == 1) {
                return i;
            }
        }
        return null;
    }

    @Override
    public void remove() {
        regulatingPoint.remove();
        parent.setRatioTapChanger(null);
    }

    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public String getMessageHeader() {
        return "ratioTapChanger '" + parent.getTransformer().getId() + "': ";
    }

    @Override
    public RegulationMode getRegulationMode() {
        String regulationMode = getAttributes().getRegulatingPoint().getRegulationMode();
        return regulationMode != null ? RegulationMode.valueOf(regulationMode) : null;
    }

    @Override
    public RatioTapChanger setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), regulationMode, getTargetV(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        RegulationMode oldValue = getRegulationMode();
        if (regulationMode != oldValue) {
            regulatingPoint.setRegulationMode(getTapChangerAttribute() + ".regulationMode", String.valueOf(regulationMode));
        }
        return this;
    }

    @Override
    public double getTargetV() {
        if (getRegulationMode() != RegulationMode.VOLTAGE) {
            return Double.NaN;
        }
        return getAttributes().getRegulationValue();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        if (!Double.isNaN(targetV)) {
            regulatingPoint.setRegulationMode(getTapChangerAttribute() + ".regulationMode",
                String.valueOf(RatioTapChanger.RegulationMode.VOLTAGE));
        }
        setRegulationValue(targetV);
        return this;
    }

    public static void validateStep(TapChangerStepAttributes step, TapChangerParent parent) {
        AbstractTapChanger.validateStep(step, parent);
    }

    // equals and hashCode are overridden to ensure correct behavior of the RatioTapChanger
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
        RatioTapChangerImpl that = (RatioTapChangerImpl) o;
        if (!Objects.equals(that.getTransformer().getClass(), getTransformer().getClass())) {
            return false;
        }
        // check ratio tap changer are on same leg
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
