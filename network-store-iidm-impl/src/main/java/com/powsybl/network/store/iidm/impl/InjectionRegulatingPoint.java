/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.*;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class InjectionRegulatingPoint<I extends Injection<I> & VoltageRegulationHolder<I>, D extends InjectionAttributes & VoltageRegulationHolderAttributes>
        extends AbstractRegulatingPoint {
    private final AbstractRegulatingInjection<I, D> injection;

    public InjectionRegulatingPoint(NetworkObjectIndex index, AbstractRegulatingInjection<I, D> injection, Function<Attributes, ? extends RegulatingPointAttributes> attributesGetter) {
        super(index, attributesGetter, "VoltageRegulation.Terminal");
        this.injection = injection;
    }

    private Resource<D> getResource() {
        return injection.getResource();
    }

    @Override
    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(getResource().getAttributes());
    }

    @Override
    protected boolean canRelocateOnLocalTerminal() {
        // for static var compensators, only voltage regulation can be relocated on the local terminal
        return getAttributes().getRegulatingResourceType() != ResourceType.STATIC_VAR_COMPENSATOR
            || RegulationMode.VOLTAGE.name().equals(getAttributes().getRegulationMode());
    }

    @Override
    protected void resetRegulatingAndRegulationMode(Terminal regulatingTerminal, Terminal localTerminal, ReportNode reportNode) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected()) {
            if (getAttributes().getRegulatingResourceType() == ResourceType.STATIC_VAR_COMPENSATOR) {
                // for svc we set the regulation mode to VOLTAGE if the regulation was not on the same bus than the svc.
                // If the svc is on the same bus were the equipment was removed we keep the regulation
                setRegulationMode("VoltageRegulation.RegulationMode", RegulationMode.VOLTAGE.name());
                reportNode.newReportNode()
                    .withMessageTemplate("network.store.resetSVCRegulationMode")
                    .withUntypedValue("identifiableId", getRegulatingEquipmentId())
                    .withSeverity(TypedValue.INFO_SEVERITY)
                    .add();
            }
            // the target can be inappropriate if it was a remote regulation
            // (as in the powsybl-core in-memory implementation, these resets are not notified)
            getIdentifiable().updateResourceWithoutNotification(res -> getAttributes().setRegulating(false));
            if (getAttributes() instanceof VoltageRegulationAttributes voltageRegulationAttributes) {
                getIdentifiable().updateResourceWithoutNotification(res -> voltageRegulationAttributes.setTargetValue(Double.NaN));
            }
        }
        // if the regulating equipment was already regulating on his bus but on another element
        // we reallocate the regulating point and we keep the regulation on
    }

    @Override
    protected AbstractRegulatingInjection<I, D> getIdentifiable() {
        return injection;
    }
}
