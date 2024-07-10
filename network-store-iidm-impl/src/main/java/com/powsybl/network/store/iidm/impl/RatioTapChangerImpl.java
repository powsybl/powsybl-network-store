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
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), getRegulationMode(), regulationValue, parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getAttributes().getRegulationValue();
        if (Double.compare(regulationValue, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulationValue(regulationValue));
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationValue);
        }
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return getAttributes().isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), status, getRegulationTerminal(), getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        boolean oldValue = getAttributes().isLoadTapChangingCapabilities();
        if (status != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setLoadTapChangingCapabilities(status));
            notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, status);
        }
        return this;
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, hasLoadTapChangingCapabilities(), getRegulationTerminal(), getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, true);

        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), regulationTerminal, getRegulationMode(), getRegulationValue(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public int getStepCount() {
        return getAttributes().getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        var attributes = getAttributes();
        int tapPositionIndex = tapPosition - attributes.getLowTapPosition();
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
        return getAttributes().getRegulationMode();
    }

    @Override
    public RatioTapChanger setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), regulationMode, getTargetV(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        RegulationMode oldValue = getAttributes().getRegulationMode();
        if (regulationMode != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulationMode(regulationMode));
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationMode);
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
        setRegulationValue(targetV);
        if (!Double.isNaN(targetV)) {
            setRegulationMode(RegulationMode.VOLTAGE);
        }
        return this;
    }

    public static void validateStep(TapChangerStepAttributes step, TapChangerParent parent) {
        AbstractTapChanger.validateStep(step, parent);
    }
}
