/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.*;
import lombok.Getter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractRegulatingInjection<I extends Injection<I> & VoltageRegulationHolder<I>, D extends InjectionAttributes & VoltageRegulationHolderAttributes>
        extends AbstractInjectionImpl<I, D> implements VoltageRegulationHolder<I> {

    @Getter
    protected final InjectionRegulatingPoint<I, D> regulatingPoint;

    protected AbstractRegulatingInjection(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new InjectionRegulatingPoint<>(index, this, attributes -> ((VoltageRegulationHolderAttributes) attributes).getVoltageRegulation());
    }

    protected abstract Class<? extends VoltageRegulationHolder<?>> getHolderClass();

    protected VoltageRegulationAttributes getVoltageRegulationAttributes() {
        return getResource().getAttributes().getVoltageRegulation();
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        if (getVoltageRegulationAttributes() == null) {
            return null;
        }
        return new VoltageRegulationImpl(index, this, getHolderClass(), this, regulatingPoint);
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(getHolderClass(), this, this, index.getNetwork(), this::createOrUpdateVoltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        VoltageRegulation oldVoltageRegulation = getVoltageRegulation();
        if (oldVoltageRegulation != null) {
            ((VoltageRegulationImpl) oldVoltageRegulation).onRemove();
        }
        if (getVoltageRegulationAttributes() != null) {
            updateResource(res -> res.getAttributes().setVoltageRegulation(null),
                    "VoltageRegulation.removed", oldVoltageRegulation, null);
        }
    }

    VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes coreAttributes) {
        VoltageRegulationAttributes existing = getVoltageRegulationAttributes();
        if (existing == null) {
            VoltageRegulationAttributes attributes = VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(getId())
                .regulatingResourceType(getResource().getType())
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(getId(), null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(coreAttributes.terminal()))
                .regulatedResourceType(coreAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) coreAttributes.terminal().getConnectable()).getResource().getType()
                    : getResource().getType())
                .regulationMode(coreAttributes.mode() != null ? coreAttributes.mode().name() : null)
                .regulating(coreAttributes.isRegulating())
                .targetValue(coreAttributes.targetValue())
                .targetDeadband(coreAttributes.targetDeadband())
                .slope(coreAttributes.slope())
                .build();
            VoltageRegulation voltageRegulation = new VoltageRegulationImpl(index, this, getHolderClass(), this, regulatingPoint);
            updateResource(res -> res.getAttributes().setVoltageRegulation(attributes),
                "VoltageRegulation.created", null, voltageRegulation);
            if (coreAttributes.terminal() != null) {
                ((TerminalImpl<?>) coreAttributes.terminal()).setAsRegulatingPoint(regulatingPoint);
                ((VoltageRegulationImpl) getVoltageRegulation()).registerOnTerminal();
            }
        } else {
            // update the existing regulation on the current variant
            VoltageRegulationImpl voltageRegulation = new VoltageRegulationImpl(index, this, getHolderClass(), this, regulatingPoint);
            voltageRegulation.setAttributesOnCurrentVariant(coreAttributes);
        }
        return getVoltageRegulation();
    }
}
