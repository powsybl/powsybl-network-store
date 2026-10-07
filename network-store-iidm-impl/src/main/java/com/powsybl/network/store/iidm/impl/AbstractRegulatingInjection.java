/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.*;
import lombok.Getter;

/**
 * Base class for injection implementations that are {@link VoltageRegulationHolder}s
 * (generators, batteries, shunt compensators, static var compensators, vsc converter stations).
 *
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractRegulatingInjection<I extends Injection<I> & VoltageRegulationHolder<I>, D extends InjectionAttributes & VoltageRegulationHolderAttributes>
        extends AbstractInjectionImpl<I, D> implements VoltageRegulationHolder<I> {

    @Getter
    protected final InjectionRegulatingPoint<I, D> regulatingPoint;

    // the voltage regulation object is cached so that two calls to getVoltageRegulation()
    // return the same instance, as in the powsybl-core in-memory implementation
    private VoltageRegulationImpl voltageRegulation;

    protected AbstractRegulatingInjection(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new InjectionRegulatingPoint<>(index, this, attributes -> ((VoltageRegulationHolderAttributes) attributes).getVoltageRegulation());
    }

    /**
     * The iidm interface of the holder (e.g. {@code Generator.class}), used for validation.
     */
    protected abstract Class<? extends VoltageRegulationHolder<?>> getHolderClass();

    protected VoltageRegulationAttributes getVoltageRegulationAttributes() {
        return getResource().getAttributes().getVoltageRegulation();
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
        voltageRegulation = new VoltageRegulationImpl(index, this, getHolderClass(), this, regulatingPoint, "");
        return voltageRegulation;
    }

    /**
     * Clean the references of the voltage regulation (on the regulating terminal). To be called
     * when the regulating equipment is removed.
     */
    protected void removeVoltageRegulationRefs() {
        if (getVoltageRegulationAttributes() != null) {
            ((VoltageRegulationImpl) getVoltageRegulation()).onRemove();
        }
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(getHolderClass(), this, this, index.getNetwork(), this::createOrUpdateVoltageRegulation);
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
                VoltageRegulationAttributes oldAttributes = getVoltageRegulationAttributes();
                updateResource(res -> res.getAttributes().setVoltageRegulation(null),
                    "voltageRegulation", oldAttributes, null);
            }
        }
        variantManager.setWorkingVariant(workingVariantId);
    }

    VoltageRegulation createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes coreAttributes) {
        VoltageRegulationAttributes existing = getVoltageRegulationAttributes();
        if (existing == null) {
            if (coreAttributes.terminal() != null && index.getNetwork().getVariantManager().getVariantIds().size() > 1) {
                throw new PowsyblException(getMessageHeader() + "Cannot set terminal when there are multiple variants");
            }
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
            updateResource(res -> res.getAttributes().setVoltageRegulation(attributes),
                "voltageRegulation", null, attributes);
            if (coreAttributes.terminal() != null) {
                ((TerminalImpl<?>) coreAttributes.terminal()).setAsRegulatingPoint(regulatingPoint);
                ((VoltageRegulationImpl) getVoltageRegulation()).registerOnTerminal();
            }
        } else {
            // update the existing regulation on the current variant
            VoltageRegulationImpl voltageRegulation = (VoltageRegulationImpl) getVoltageRegulation();
            voltageRegulation.setAttributesOnCurrentVariant(coreAttributes);
        }
        return getVoltageRegulation();
    }
}
