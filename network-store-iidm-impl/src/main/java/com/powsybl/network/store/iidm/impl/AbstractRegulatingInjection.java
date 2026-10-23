/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com).
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file.
 * SPDX-License-Identifier: MPL-2.0
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
import com.powsybl.network.store.model.NetworkVoltageRegulationAttributes;
import com.powsybl.network.store.model.Resource;
import lombok.Getter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
abstract class AbstractRegulatingInjection<
        I extends Injection<I> & VoltageRegulationHolder<I>,
        D extends InjectionAttributes>
        extends AbstractInjectionImpl<I, D>
        implements Injection<I>, VoltageRegulationHolder<I> {

    @Getter
    protected final InjectionRegulatingPoint<I, D> regulatingPoint;

    private VoltageRegulationImpl voltageRegulation;

    protected AbstractRegulatingInjection(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new InjectionRegulatingPoint<>(index, this, AbstractRegulatingEquipmentAttributes.class::cast);
        if (getRegulatingEquipmentAttributes().getVoltageRegulation() != null) {
            voltageRegulation = createVoltageRegulation();
        }
    }

    protected abstract Class<? extends VoltageRegulationHolder<?>> getVoltageRegulationHolderClass();

    private AbstractRegulatingEquipmentAttributes getRegulatingEquipmentAttributes() {
        return (AbstractRegulatingEquipmentAttributes) getResource().getAttributes();
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(getVoltageRegulationHolderClass(), this, this, index,
                this::createOrUpdateVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        NetworkVoltageRegulationAttributes attributes = getRegulatingEquipmentAttributes().getVoltageRegulation();
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
        onVoltageRegulationRemoval();
        forEachVariant(variantId -> {
            NetworkVoltageRegulationAttributes oldValue = getRegulatingEquipmentAttributes().getVoltageRegulation();
            if (oldValue != null) {
                updateResource(res -> ((AbstractRegulatingEquipmentAttributes) res.getAttributes()).setVoltageRegulation(null),
                        "voltageRegulation", oldValue, null);
            }
            regulatingPoint.synchronizeRegulatingTerminal(null);
        });
    }

    protected void onVoltageRegulationRemoval() {
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
            voltageRegulation = null;
        }
    }

    @Override
    public Terminal getRegulatingTerminal() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null && regulation.getTerminal() != null ? regulation.getTerminal() : getTerminal();
    }

    @Override
    public boolean isRegulating() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null && regulation.isRegulating();
    }

    // should be setRegulatingTerminal but there is already a method with the same name in the regulating equipments
    protected void setRegTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            double targetValue = isWithMode(RegulationMode.VOLTAGE) ? getRegulatingTargetV() : getRegulatingTargetQ();
            regulation.setTerminal(regulatingTerminal, targetValue);
        } else {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTerminal(regulatingTerminal)
                    .withRegulating(false)
                    .build();
        }
    }

    public I setRegulating(boolean regulating) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null) {
            regulation.setRegulating(regulating);
        } else if (regulating) {
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withRegulating(true)
                    .build();
        }
        return getInjection();
    }

    private VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
        NetworkVoltageRegulationAttributes newValue = NetworkVoltageRegulationAttributes.builder()
                .targetValue(attributes.targetValue())
                .targetDeadband(attributes.targetDeadband())
                .slope(attributes.slope())
                .mode(attributes.mode())
                .regulating(attributes.isRegulating())
                .terminal(TerminalRefUtils.getTerminalRefAttributes(attributes.terminal()))
                .build();
        NetworkVoltageRegulationAttributes oldValue = getRegulatingEquipmentAttributes().getVoltageRegulation();
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
        }
        updateResource(res -> ((AbstractRegulatingEquipmentAttributes) res.getAttributes()).setVoltageRegulation(newValue),
                "voltageRegulation", oldValue, newValue);
        voltageRegulation = createVoltageRegulation();
        return voltageRegulation;
    }

    private VoltageRegulationImpl createVoltageRegulation() {
        return new VoltageRegulationImpl(this, this, getVoltageRegulationHolderClass(), index,
                () -> getRegulatingEquipmentAttributes().getVoltageRegulation(),
                (modifier, attribute, oldValue, newValue) -> updateResource(res -> {
                    NetworkVoltageRegulationAttributes attributes =
                            ((AbstractRegulatingEquipmentAttributes) res.getAttributes()).getVoltageRegulation();
                    modifier.accept(attributes);
                }, attribute, oldValue, newValue),
                regulatingPoint);
    }
}
