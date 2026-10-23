/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.AbstractRegulatingEquipmentAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.LegacyVoltageRegulationAttributesMapper;
import com.powsybl.network.store.model.NetworkVoltageRegulationAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.VoltageRegulationReactiveTargetAttributes;
import com.powsybl.network.store.model.VoltageRegulationTargetAttributes;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Setter
@Getter
public abstract class AbstractRegulatingInjection<I extends Injection<I>,
    D extends InjectionAttributes & VoltageRegulationTargetAttributes>
    extends AbstractInjectionImpl<I, D> implements Injection<I> {

    protected final InjectionRegulatingPoint<I, D> regulatingPoint;

    private VoltageRegulationImpl voltageRegulation;

    private Resource<D> legacyVoltageRegulationResource;

    protected AbstractRegulatingInjection(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new InjectionRegulatingPoint<>(index, this, AbstractRegulatingEquipmentAttributes.class::cast);
    }

    // should be setRegulatingTerminal but there is already a method with the same name in the regulating equipments
    protected void setRegTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            double targetValue = regulatingTerminal == null ? Double.NaN : regulation.isWithTerminal() ? regulation.getTargetValue()
                : regulation.getMode() == RegulationMode.REACTIVE_POWER ? getLocalTargetQ() : getLocalTargetV();
            regulation.setTerminal(regulatingTerminal, targetValue);
        } else {
            regulatingPoint.setRegulatingTerminal(regulatingTerminal);
        }
    }

    public Terminal getRegulatingTerminal() {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null && regulation.getTerminal() != null) {
            return regulation.getTerminal();
        }
        Terminal regulatingTerminal = regulatingPoint.getRegulatingTerminal();
        return regulatingTerminal != null ? regulatingTerminal : getTerminal();
    }

    public boolean isRegulating() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null && regulation.isRegulating();
    }

    public I setRegulating(boolean regulating) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation == null) {
            newVoltageRegulation().withRegulating(regulating).build();
        } else {
            regulation.setRegulating(regulating);
        }
        return getInjection();
    }

    void onRegulatingTerminalRemoved(Terminal removedTerminal) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation == null || regulation.getTerminal() != removedTerminal) {
            return;
        }
        Terminal localTerminal = getTerminal();
        boolean sameBus = localTerminal != null && localTerminal != removedTerminal
            && localTerminal.getBusBreakerView().getConnectableBus() != null
            && removedTerminal.getBusBreakerView().getConnectableBus() != null
            && Objects.equals(localTerminal.getBusBreakerView().getConnectableBus().getId(),
                removedTerminal.getBusBreakerView().getConnectableBus().getId());
        boolean isSvc = getVoltageRegulationHolderClass() == com.powsybl.iidm.network.StaticVarCompensator.class;
        if (sameBus && (!isSvc || regulation.getMode() == RegulationMode.VOLTAGE)) {
            ((VoltageRegulationImpl) regulation).relocateTerminal(localTerminal);
        } else {
            ((VoltageRegulationImpl) regulation).deactivateAfterTerminalRemoval(isSvc);
        }
    }

    public double getLocalTargetV() {
        return targetAttributes().getLocalTargetV();
    }

    public I setLocalTargetV(double targetV) {
        double oldValue = getLocalTargetV();
        if (Double.compare(oldValue, targetV) != 0) {
            updateResource(resource -> targetAttributes().setLocalTargetV(targetV),
                "localTargetV", oldValue, targetV);
        }
        return getInjection();
    }

    public double getLocalTargetQ() {
        if (getResource().getAttributes() instanceof VoltageRegulationReactiveTargetAttributes attributes) {
            return attributes.getLocalTargetQ();
        }
        return Double.NaN;
    }

    public I setLocalTargetQ(double targetQ) {
        if (!(getResource().getAttributes() instanceof VoltageRegulationReactiveTargetAttributes)) {
            return getInjection();
        }
        double oldValue = getLocalTargetQ();
        if (Double.compare(oldValue, targetQ) != 0) {
            updateResource(resource -> reactiveTargetAttributes().setLocalTargetQ(targetQ),
                "localTargetQ", oldValue, targetQ);
        }
        return getInjection();
    }

    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(getVoltageRegulationHolderClass(), this, getNetwork(),
            this::getLocalTargetV, this::getLocalTargetQ,
            attributes -> createOrUpdateVoltageRegulation(attributes));
    }

    protected abstract Class<? extends VoltageRegulationHolder<?>> getVoltageRegulationHolderClass();

    public VoltageRegulation getVoltageRegulation() {
        Resource<D> resource = getResource();
        if (legacyVoltageRegulationResource != resource) {
            if (regulatingAttributes().getVoltageRegulation() == null) {
                index.loadExtensionAttributes(resource.getType(), resource.getId(), "voltageRegulation");
            }
            LegacyVoltageRegulationAttributesMapper.migrate(resource.getAttributes());
            legacyVoltageRegulationResource = resource;
        }
        NetworkVoltageRegulationAttributes attributes = regulatingAttributes().getVoltageRegulation();
        if (attributes == null) {
            return null;
        }
        if (voltageRegulation == null) {
            voltageRegulation = new VoltageRegulationImpl(this, index, regulatingPoint, getVoltageRegulationHolderClass(),
                (VoltageRegulationHolder<?>) getInjection(),
                () -> regulatingAttributes().getVoltageRegulation(),
                value -> regulatingAttributes().setVoltageRegulation(value));
            regulatingPoint.getAttributes().setRegulating(attributes.isRegulating());
            regulatingPoint.getAttributes().setRegulationMode(attributes.getMode() == null ? null : attributes.getMode().toString());
            Terminal terminal = voltageRegulation.getTerminal();
            if (terminal != null) {
                regulatingPoint.setRegulatingTerminal(terminal);
                voltageRegulation.updateTerminalReference(terminal);
            }
        }
        return voltageRegulation;
    }

    public void removeVoltageRegulation() {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            ((VoltageRegulationImpl) regulation).remove();
            String id = getId();
            ResourceType resourceType = getResource().getType();
            updateResource(resource -> regulatingAttributes().setVoltageRegulation(null),
                "VoltageRegulation", regulation.getAttributes(), null);
            voltageRegulation = null;
            for (var variant : index.getStoreClient().getVariantsInfos(index.getNetworkUuid(), true)) {
                index.getRegulatingResource(resourceType, variant.getNum(), id).ifPresent(resource -> {
                    if (resource.getVariantNum() != index.getWorkingVariantNum()) {
                        resource.getAttributes().setVoltageRegulation(null);
                        index.updateRegulatingResource(resource);
                    }
                });
            }
        }
    }

    private VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
        VoltageRegulation current = getVoltageRegulation();
        if (getNetwork().getVariantManager().getVariantIds().size() > 1 && attributes.terminal() != null
            && (current == null || !Objects.equals(TerminalRefUtils.getTerminalRefAttributes(current.getTerminal()),
                TerminalRefUtils.getTerminalRefAttributes(attributes.terminal())))) {
            String action = current != null && current.getTerminal() != null ? "change" : "set";
            throw new com.powsybl.commons.PowsyblException(getMessageHeader() + "Cannot " + action + " terminal when there are multiple variants");
        }
        if (current == null) {
            NetworkVoltageRegulationAttributes storedAttributes = NetworkVoltageRegulationAttributes.builder()
                .targetValue(attributes.targetValue())
                .targetDeadband(attributes.targetDeadband())
                .slope(attributes.slope())
                .mode(attributes.mode())
                .regulating(attributes.isRegulating())
                .terminal(TerminalRefUtils.getTerminalRefAttributes(attributes.terminal()))
                .build();
            updateResource(resource -> regulatingAttributes().setVoltageRegulation(storedAttributes),
                "VoltageRegulation", null, storedAttributes);
            voltageRegulation = new VoltageRegulationImpl(this, index, regulatingPoint, getVoltageRegulationHolderClass(),
                (VoltageRegulationHolder<?>) getInjection(),
                () -> regulatingAttributes().getVoltageRegulation(),
                value -> regulatingAttributes().setVoltageRegulation(value));
            Terminal terminal = voltageRegulation.getTerminal();
            if (terminal != null) {
                regulatingPoint.setRegulatingTerminal(terminal);
            }
            regulatingPoint.getAttributes().setRegulating(attributes.isRegulating());
            regulatingPoint.getAttributes().setRegulationMode(attributes.mode() == null ? null : attributes.mode().toString());
            return voltageRegulation;
        }
        ((VoltageRegulationImpl) current).setAttributes(attributes);
        return current;
    }

    private AbstractRegulatingEquipmentAttributes regulatingAttributes() {
        return AbstractRegulatingEquipmentAttributes.class.cast(getResource().getAttributes());
    }

    private VoltageRegulationTargetAttributes targetAttributes() {
        return VoltageRegulationTargetAttributes.class.cast(getResource().getAttributes());
    }

    private VoltageRegulationReactiveTargetAttributes reactiveTargetAttributes() {
        return getResource().getAttributes() instanceof VoltageRegulationReactiveTargetAttributes attributes ? attributes : null;
    }
}
