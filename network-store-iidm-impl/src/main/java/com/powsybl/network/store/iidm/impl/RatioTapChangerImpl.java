/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
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

    // the voltage regulation object is cached so that two calls to getVoltageRegulation()
    // return the same instance, as in the powsybl-core in-memory implementation
    private VoltageRegulationImpl voltageRegulation;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "ratio tap changer", attributes -> ((RatioTapChangerAttributes) attributes).getVoltageRegulation(),
            "ratio" + parent.getTapChangerAttribute() + ".VoltageRegulation.Terminal");
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
    }

    @Override
    protected RatioTapChangerAttributes getAttributes() {
        return getAttributes(getResource());
    }

    protected RatioTapChangerAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRatioTapChangerAttributes();
    }

    private VoltageRegulationAttributes getVoltageRegulationAttributes() {
        RatioTapChangerAttributes attributes = getAttributes();
        return attributes == null ? null : attributes.getVoltageRegulation();
    }

    private VoltageRegulation.VoltageRegulationAttributes toCoreAttributes(java.util.function.UnaryOperator<VoltageRegulation.VoltageRegulationAttributes> modifier) {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null ? modifier.apply(voltageRegulation.getAttributes()) : null;
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(RatioTapChanger.class, parent, this, index.getNetwork(), this::createOrUpdateVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        if (voltageRegulation != null) {
            // return the cached instance (also in an invalid variant context, where reading
            // the attributes would throw); it reads the current variant attributes lazily
            return voltageRegulation;
        }
        if (getVoltageRegulationAttributes() == null) {
            return null;
        }
        voltageRegulation = new VoltageRegulationImpl(index, this, RatioTapChanger.class, parent, regulatingPoint,
            getTapChangerAttribute() + ".");
        return voltageRegulation;
    }

    @Override
    public void removeVoltageRegulation() {
        // the voltage regulation is removed on all existing variants
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
            voltageRegulation = null;
        }
        VariantManager variantManager = index.getNetwork().getVariantManager();
        String workingVariantId = variantManager.getWorkingVariantId();
        for (String variantId : variantManager.getVariantIds()) {
            variantManager.setWorkingVariant(variantId);
            if (getVoltageRegulationAttributes() != null) {
                getTransformer().updateResource(res -> getAttributes(res).setVoltageRegulation(null),
                    getTapChangerAttribute() + ".voltageRegulation", null, null);
            }
        }
        variantManager.setWorkingVariant(workingVariantId);
    }

    @Override
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public RatioTapChanger setLocalTargetV(double localTargetV) {
        // local target voltage is not supported for ratio tap changers
        return this;
    }

    private VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes coreAttributes) {
        if (getVoltageRegulationAttributes() == null) {
            if (coreAttributes.terminal() != null && index.getNetwork().getVariantManager().getVariantIds().size() > 1) {
                throw new PowsyblException(parent.getMessageHeader() + "Cannot set terminal when there are multiple variants");
            }
            VoltageRegulationAttributes attributes = VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(getTransformer().getId())
                .regulatingResourceType(getTransformer().getResource().getType())
                .regulatingTapChangerType(getRegulatingTapChangerType())
                .localTerminal(null)
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(coreAttributes.terminal()))
                .regulatedResourceType(coreAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) coreAttributes.terminal().getConnectable()).getResource().getType()
                    : getTransformer().getResource().getType())
                .regulationMode(coreAttributes.mode() != null ? coreAttributes.mode().name() : null)
                .regulating(coreAttributes.isRegulating())
                .targetValue(coreAttributes.targetValue())
                .targetDeadband(coreAttributes.targetDeadband())
                .slope(coreAttributes.slope())
                .build();
            getTransformer().updateResource(res -> getAttributes(res).setVoltageRegulation(attributes),
                getTapChangerAttribute() + ".voltageRegulation", null, attributes);
            if (coreAttributes.terminal() != null) {
                ((TerminalImpl<?>) coreAttributes.terminal()).setAsRegulatingPoint(regulatingPoint);
                ((VoltageRegulationImpl) getVoltageRegulation()).registerOnTerminal();
            }
        } else {
            VoltageRegulationImpl voltageRegulation = (VoltageRegulationImpl) getVoltageRegulation();
            voltageRegulation.setAttributesOnCurrentVariant(coreAttributes);
        }
        return getVoltageRegulation();
    }

    private RegulatingTapChangerType getRegulatingTapChangerType() {
        if (parent instanceof ThreeWindingsTransformerImpl.LegImpl leg) {
            return switch (leg.getSide()) {
                case ONE -> RegulatingTapChangerType.RATIO_TAP_CHANGER_SIDE_ONE;
                case TWO -> RegulatingTapChangerType.RATIO_TAP_CHANGER_SIDE_TWO;
                case THREE -> RegulatingTapChangerType.RATIO_TAP_CHANGER_SIDE_THREE;
            };
        }
        return RegulatingTapChangerType.RATIO_TAP_CHANGER;
    }

    @Override
    public double getRegulationValue() {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null ? voltageRegulation.getTargetValue() : Double.NaN;
    }

    @Override
    public RatioTapChanger setRegulationValue(double regulationValue) {
        double oldValue = Double.NaN;
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTargetValue();
            voltageRegulation.setTargetValue(regulationValue);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .withTargetValue(regulationValue)
                .build();
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulationValue", variantId, oldValue, regulationValue);
        return this;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, toCoreAttributes(a -> a),
            loadTapChangingCapabilities,
            parent.getNetwork(), parent.getNetwork().getMinValidationLevel(), parent.getNetwork().getReportNodeContext().getReportNode());
        return super.setLoadTapChangingCapabilities(loadTapChangingCapabilities);
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        NetworkImpl network = index.getNetwork();
        VoltageRegulation.VoltageRegulationAttributes attributes = toCoreAttributes(a -> a.withRegulating(regulating));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
            hasLoadTapChangingCapabilities(),
            network, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
            network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            boolean oldValue = voltageRegulation.isRegulating();
            voltageRegulation.setRegulating(regulating);
            String variantId = network.getVariantManager().getWorkingVariantId();
            index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        } else if (regulating) {
            // This will throw an exception because the target value is not set
            // (called to have the same message as everywhere else)
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .build();
        }
        return this;
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        NetworkImpl network = index.getNetwork();
        VoltageRegulation.VoltageRegulationAttributes attributes = toCoreAttributes(a -> a.withTerminalAndTargetValue(regulationTerminal, getRegulationValue()));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
            hasLoadTapChangingCapabilities(),
            network, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        Terminal oldValue = null;
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTerminal();
            voltageRegulation.setTerminal(regulationTerminal, getRegulationValue());
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .withTerminal(regulationTerminal)
                .build();
        }
        String variantId = network.getVariantManager().getWorkingVariantId();
        index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulationTerminal", variantId, oldValue, regulationTerminal);
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        return this.getRegulatingTerminal();
    }

    @Override
    public double getTargetDeadband() {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null ? voltageRegulation.getTargetDeadband() : Double.NaN;
    }

    @Override
    public RatioTapChangerImpl setTargetDeadband(double targetDeadband) {
        double oldValue = Double.NaN;
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTargetDeadband();
            voltageRegulation.setTargetDeadband(targetDeadband);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(false)
                .withTargetDeadband(targetDeadband)
                .build();
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
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
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
        } else {
            regulatingPoint.remove();
        }
        parent.setRatioTapChanger(null);
    }

    @Override
    public boolean isRegulating() {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null && voltageRegulation.isRegulating();
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
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null ? voltageRegulation.getMode() : null;
    }

    @Override
    public RatioTapChanger setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = null;
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getMode();
            voltageRegulation.setMode(regulationMode);
        } else {
            newVoltageRegulation()
                .withMode(regulationMode)
                .withRegulating(false)
                .build();
        }
        index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulationMode", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulationMode);
        return this;
    }

    @Override
    public double getTargetV() {
        return isWithMode(RegulationMode.VOLTAGE) ? getVoltageRegulation().getTargetValue() : Double.NaN;
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        double oldValue = Double.NaN;
        VoltageRegulation.VoltageRegulationAttributes attributes = toCoreAttributes(a -> a.withTargetValue(targetV).withMode(RegulationMode.VOLTAGE));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
            hasLoadTapChangingCapabilities(),
            parent.getNetwork(), parent.getNetwork().getMinValidationLevel(), parent.getNetwork().getReportNodeContext().getReportNode());
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        if (voltageRegulation != null) {
            if (!Double.isNaN(targetV) && !isWithMode(RegulationMode.VOLTAGE)) {
                RegulationMode oldMode = voltageRegulation.getMode();
                voltageRegulation.setMode(RegulationMode.VOLTAGE);
                index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulationMode", variantId, oldMode, RegulationMode.VOLTAGE);
            }
            if (isWithMode(RegulationMode.VOLTAGE)) {
                oldValue = voltageRegulation.getTargetValue();
                voltageRegulation.setTargetValue(targetV);
            }
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withRegulating(false)
                .build();
        }
        index.notifyUpdate(getTransformer(), getTapChangerAttribute() + ".regulationValue", variantId, oldValue, targetV);
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
