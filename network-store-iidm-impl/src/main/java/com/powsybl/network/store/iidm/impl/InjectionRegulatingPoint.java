/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class InjectionRegulatingPoint<I extends Injection<I>, D extends InjectionAttributes> extends AbstractRegulatingPoint {
    private final AbstractRegulatingInjection<I, D> injection;

    public InjectionRegulatingPoint(NetworkObjectIndex index, AbstractRegulatingInjection<I, D> injection, Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        super(index, attributesGetter);
        this.injection = injection;
    }

    private Resource<D> getResource() {
        return injection.getResource();
    }

    @Override
    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(getResource().getAttributes()).getRegulatingPoint();
    }

    @Override
    protected void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal, ReportNode reportNode) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() &&
            !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getRegulatingResourceType()) {
                // for svc we set the regulation mode to Off if the regulation was not on the same bus than the svc. If the svc is on the same bus were the equipment was remove we keep the regulation
                case STATIC_VAR_COMPENSATOR -> {
                    resetRegulationModeWithoutNotification(String.valueOf(StaticVarCompensator.RegulationMode.OFF));
                    reportNode.newReportNode()
                        .withMessageTemplate("resetSVCRegulationMode", "Regulation mode of static var compensator ${identifiableId} has been reset to OFF due to deletion of its regulating terminal")
                        .withUntypedValue("identifiableId", getRegulatingEquipmentId())
                        .withSeverity(TypedValue.INFO_SEVERITY)
                        .add();
                }
                case GENERATOR, SHUNT_COMPENSATOR, VSC_CONVERTER_STATION -> {
                }
                default -> throw new PowsyblException("No regulation for this kind of equipment");
            }
            // the target can be inappropriated if it was a remote regulation
            resetRegulatingWithoutNotification(false);
        }
        // if the regulating equipment was already regulating on his bus but on another element
        // we reallocate the regulating point and we keep the regulation on
    }

    @Override
    protected AbstractRegulatingInjection<I, D> getIdentifiable() {
        return injection;
    }
}
