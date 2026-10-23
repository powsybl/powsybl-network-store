/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes> implements RatioTapChanger, Validable {

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    private VoltageRegulationImpl voltageRegulation;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "ratio tap changer");
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        if (getAttributes().getVoltageRegulation() != null) {
            voltageRegulation = createVoltageRegulation();
        }
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
        return getVoltageRegulation() == null ? Double.NaN : getVoltageRegulation().getTargetValue();
    }

    @Override
    public RatioTapChanger setRegulationValue(double regulationValue) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(regulationValue)
                    .withRegulating(false)
                    .build();
        } else {
            getVoltageRegulation().setTargetValue(regulationValue);
        }
        return this;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        if (getVoltageRegulation() != null) {
            ValidationUtil.checkRatioTapChangerRegulation(parent,
                    getAttributes().getVoltageRegulation().toAttributes(getVoltageRegulation().getTerminal()),
                    loadTapChangingCapabilities, parent.getNetwork(),
                    parent.getNetwork().getMinValidationLevel(),
                    parent.getNetwork().getReportNodeContext().getReportNode());
        }
        return super.setLoadTapChangingCapabilities(loadTapChangingCapabilities);
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        if (getVoltageRegulation() == null) {
            if (regulating) {
                newVoltageRegulation()
                        .withMode(RegulationMode.VOLTAGE)
                        .withRegulating(true)
                        .build();
            }
        } else {
            getVoltageRegulation().setRegulating(regulating);
        }
        return this;
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTerminal(regulationTerminal)
                    .withRegulating(false)
                    .build();
        } else {
            getVoltageRegulation().setTerminal(regulationTerminal, getVoltageRegulation().getTargetValue());
        }
        return this;
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
    public RatioTapChangerStep getSolvedCurrentStep() {
        Integer solvedPosition = getAttributes().getSolvedTapPosition();
        if (solvedPosition == null) {
            return null;
        }
        return getStep(solvedPosition);
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
        removeVoltageRegulation();
        regulatingPoint.remove();
        parent.setRatioTapChanger(null);
    }

    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public MessageHeader getMessageHeader() {
        return new DefaultMessageHeader("ratioTapChanger", parent.getTransformer().getId());
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getVoltageRegulation() == null ? null : getVoltageRegulation().getMode();
    }

    @Override
    public RatioTapChanger setRegulationMode(RegulationMode regulationMode) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation()
                    .withMode(regulationMode)
                    .withRegulating(false)
                    .build();
        } else {
            getVoltageRegulation().setMode(regulationMode);
        }
        return this;
    }

    @Override
    public double getTargetV() {
        return isWithMode(RegulationMode.VOLTAGE) ? getVoltageRegulation().getTargetValue() : Double.NaN;
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(targetV)
                    .withRegulating(false)
                    .build();
        } else {
            if (!Double.isNaN(targetV) && !isWithMode(RegulationMode.VOLTAGE)) {
                getVoltageRegulation().setMode(RegulationMode.VOLTAGE);
            }
            if (isWithMode(RegulationMode.VOLTAGE)) {
                getVoltageRegulation().setTargetValue(targetV);
            }
        }
        return this;
    }

    @Override
    public boolean isRegulating() {
        return getVoltageRegulation() != null && getVoltageRegulation().isRegulating();
    }

    @Override
    public double getTargetDeadband() {
        return getVoltageRegulation() == null ? Double.NaN : getVoltageRegulation().getTargetDeadband();
    }

    @Override
    public RatioTapChangerImpl setTargetDeadband(double targetDeadband) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetDeadband(targetDeadband)
                    .withRegulating(false)
                    .build();
        } else {
            getVoltageRegulation().setTargetDeadband(targetDeadband);
        }
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        return getRegulatingTerminal();
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(RatioTapChanger.class, parent, this, index,
                this::createOrUpdateVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        NetworkVoltageRegulationAttributes attributes = getAttributes().getVoltageRegulation();
        if (attributes == null) {
            return null;
        }
        if (voltageRegulation == null) {
            voltageRegulation = createVoltageRegulation();
        }
        return voltageRegulation;
    }

    @Override
    public void removeVoltageRegulation() {
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
            voltageRegulation = null;
        }
        getTransformer().forEachVariant(variantId -> {
            NetworkVoltageRegulationAttributes oldValue = getAttributes().getVoltageRegulation();
            if (oldValue != null) {
                getTransformer().updateResource(res -> getAttributes(res).setVoltageRegulation(null),
                        getTapChangerAttribute() + ".voltageRegulation", oldValue, null);
            }
            regulatingPoint.synchronizeRegulatingTerminal(null);
        });
    }

    @Override
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public RatioTapChanger setLocalTargetV(double targetV) {
        return this;
    }

    private VoltageRegulationImpl createVoltageRegulation() {
        return new VoltageRegulationImpl(parent, this, RatioTapChanger.class, index,
                () -> getAttributes().getVoltageRegulation(),
                (modifier, attribute, oldValue, newValue) -> getTransformer().updateResource(
                        res -> modifier.accept(getAttributes(res).getVoltageRegulation()),
                        getTapChangerAttribute() + "." + attribute, oldValue, newValue),
                regulatingPoint);
    }

    private VoltageRegulationImpl createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
        NetworkVoltageRegulationAttributes newValue = NetworkVoltageRegulationAttributes.builder()
                .targetValue(attributes.targetValue())
                .targetDeadband(attributes.targetDeadband())
                .slope(attributes.slope())
                .mode(attributes.mode())
                .regulating(attributes.isRegulating())
                .terminal(TerminalRefUtils.getTerminalRefAttributes(attributes.terminal()))
                .build();
        NetworkVoltageRegulationAttributes oldValue = getAttributes().getVoltageRegulation();
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
        }
        getTransformer().updateResource(res -> getAttributes(res).setVoltageRegulation(newValue),
                getTapChangerAttribute() + ".voltageRegulation", oldValue, newValue);
        voltageRegulation = createVoltageRegulation();
        return voltageRegulation;
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
