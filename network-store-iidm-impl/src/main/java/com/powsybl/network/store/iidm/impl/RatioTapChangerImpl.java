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
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes>
    implements RatioTapChanger, Validable, VoltageRegulationHolder<RatioTapChanger> {

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    private VoltageRegulationImpl voltageRegulation;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "ratio tap changer");
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        if (getAttributes().getVoltageRegulation() != null) {
            getVoltageRegulation();
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
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public RatioTapChanger setLocalTargetV(double targetV) {
        return this;
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(RatioTapChanger.class, this, index.getNetwork(),
            () -> Double.NaN, () -> Double.NaN, this::createOrUpdateVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        if (getAttributes().getVoltageRegulation() == null) {
            return null;
        }
        if (voltageRegulation == null) {
            voltageRegulation = new VoltageRegulationImpl(getTransformer(), index, regulatingPoint, RatioTapChanger.class, this,
                () -> getAttributes().getVoltageRegulation(),
                value -> getAttributes().setVoltageRegulation(value));
        }
        return voltageRegulation;
    }

    @Override
    public void removeVoltageRegulation() {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            ((VoltageRegulationImpl) regulation).remove();
            String transformerId = getTransformer().getId();
            for (var variant : index.getStoreClient().getVariantsInfos(index.getNetworkUuid(), true)) {
                Resource<? extends IdentifiableAttributes> resource = switch (getTransformer().getResource().getType()) {
                    case TWO_WINDINGS_TRANSFORMER -> index.getStoreClient()
                        .getTwoWindingsTransformer(index.getNetworkUuid(), variant.getNum(), transformerId).orElse(null);
                    case THREE_WINDINGS_TRANSFORMER -> index.getStoreClient()
                        .getThreeWindingsTransformer(index.getNetworkUuid(), variant.getNum(), transformerId).orElse(null);
                    default -> null;
                };
                if (resource != null) {
                    RatioTapChangerAttributes attributes = getAttributes(resource);
                    if (attributes != null && attributes.getVoltageRegulation() != null) {
                        attributes.setVoltageRegulation(null);
                        index.updateResource(resource, AttributeFilter.PRIMARY_AS_NULL);
                    }
                }
            }
            voltageRegulation = null;
        }
    }

    private VoltageRegulation.VoltageRegulationAttributes getNativeRegulationAttributes() {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            return regulation.getAttributes();
        }
        return new VoltageRegulation.VoltageRegulationAttributes(getRegulationValue(), getTargetDeadband(), Double.NaN,
            getRegulationMode(), isRegulating(), getRegulationTerminal());
    }

    private VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation == null) {
            getAttributes().setVoltageRegulation(NetworkVoltageRegulationAttributesMapper.map(attributes));
            voltageRegulation = new VoltageRegulationImpl(getTransformer(), index, regulatingPoint, RatioTapChanger.class, this,
                () -> getAttributes().getVoltageRegulation(),
                value -> getAttributes().setVoltageRegulation(value));
            if (attributes.terminal() != null) {
                regulatingPoint.setRegulatingTerminal(attributes.terminal());
            }
            return voltageRegulation;
        }
        ((VoltageRegulationImpl) regulation).setAttributes(attributes);
        return regulation;
    }

    private void validateRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes, hasLoadTapChangingCapabilities(), parent.getNetwork(),
            parent.getNetwork().getMinValidationLevel(), parent.getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public double getRegulationValue() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null ? regulation.getTargetValue() : getAttributes().getRegulationValue();
    }

    @Override
    public RatioTapChanger setRegulationValue(double regulationValue) {
        validateRegulation(getNativeRegulationAttributes().withTargetValue(regulationValue));
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            regulation.setTargetValue(regulationValue);
            return this;
        }
        double oldValue = getAttributes().getRegulationValue();
        if (Double.compare(regulationValue, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulationValue(regulationValue),
                getTapChangerAttribute() + ".regulationValue", oldValue, regulationValue);
        }
        return this;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        validateRegulation(getNativeRegulationAttributes());
        return super.setLoadTapChangingCapabilities(loadTapChangingCapabilities);
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        validateRegulation(getNativeRegulationAttributes().withRegulating(regulating));

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, parent.getNetwork().getMinValidationLevel(), parent.getNetwork().getReportNodeContext().getReportNode(
                 ));

        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null && regulation.isRegulating() != regulating) {
            regulation.setRegulating(regulating);
        }

        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        VoltageRegulation current = getVoltageRegulation();
        double targetValue = current != null ? current.getTargetValue() : getRegulationValue();
        validateRegulation(getNativeRegulationAttributes().withTerminalAndTargetValue(regulationTerminal, targetValue));
        if (current != null) {
            current.setTerminal(regulationTerminal, targetValue);
            return this;
        }
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public Terminal getRegulationTerminal() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null && regulation.getTerminal() != null
            ? regulation.getTerminal() : super.getRegulationTerminal();
    }

    @Override
    public boolean isRegulating() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null ? regulation.isRegulating() : super.isRegulating();
    }

    @Override
    public double getTargetDeadband() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null ? regulation.getTargetDeadband() : super.getTargetDeadband();
    }

    @Override
    public RatioTapChangerImpl setTargetDeadband(double targetDeadband) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            if (targetDeadband < 0) {
                throw new ValidationException(parent, "Unexpected value for target deadband of RatioTapChanger: " + targetDeadband + " < 0");
            }
            regulation.setTargetDeadband(targetDeadband);
            return this;
        }
        return super.setTargetDeadband(targetDeadband);
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
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation instanceof VoltageRegulationImpl nativeRegulation) {
            nativeRegulation.remove();
        }
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
        if (getVoltageRegulation() != null && getVoltageRegulation().getMode() != null) {
            return getVoltageRegulation().getMode();
        }
        String regulationMode = getAttributes().getRegulatingPoint().getRegulationMode();
        return regulationMode != null ? RegulationMode.valueOf(regulationMode) : null;
    }

    @Override
    public RatioTapChanger setRegulationMode(RegulationMode regulationMode) {
        VoltageRegulation current = getVoltageRegulation();
        validateRegulation(getNativeRegulationAttributes().withMode(regulationMode));
        if (current != null) {
            current.setMode(regulationMode);
            return this;
        }
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
        return getRegulationValue();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            if (!Double.isNaN(targetV) && regulation.getMode() != RegulationMode.VOLTAGE) {
                regulation.setMode(RegulationMode.VOLTAGE);
            }
            regulation.setTargetValue(targetV);
            return this;
        }
        if (!Double.isNaN(targetV)) {
            regulatingPoint.setRegulationMode(getTapChangerAttribute() + ".regulationMode",
                String.valueOf(RegulationMode.VOLTAGE));
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
