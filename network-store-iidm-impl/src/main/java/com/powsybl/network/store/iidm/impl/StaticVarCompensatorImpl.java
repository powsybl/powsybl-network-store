/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.network.store.iidm.impl.extensions.StandbyAutomatonImpl;
import com.powsybl.network.store.iidm.impl.extensions.VoltagePerReactivePowerControlImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class StaticVarCompensatorImpl extends AbstractInjectionImpl<StaticVarCompensator, StaticVarCompensatorAttributes> implements StaticVarCompensator {

    public StaticVarCompensatorImpl(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        super(index, resource);
    }

    static StaticVarCompensatorImpl create(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        return new StaticVarCompensatorImpl(index, resource);
    }

    @Override
    protected StaticVarCompensator getInjection() {
        return this;
    }

    @Override
    public double getBmin() {
        return getResource().getAttributes().getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        ValidationUtil.checkBmin(this, bMin);
        double oldValue = getResource().getAttributes().getBmin();
        if (bMin != oldValue) {
            updateResource(res -> res.getAttributes().setBmin(bMin));
            index.notifyUpdate(this, "bMin", oldValue, bMin);
        }
        return this;
    }

    @Override
    public double getBmax() {
        return getResource().getAttributes().getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        ValidationUtil.checkBmax(this, bMax);
        double oldValue = getResource().getAttributes().getBmax();
        if (bMax != oldValue) {
            updateResource(res -> res.getAttributes().setBmax(bMax));
            index.notifyUpdate(this, "bMax", oldValue, bMax);
        }
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return getResource().getAttributes().getVoltageSetPoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(double voltageSetPoint) {
        ValidationUtil.checkSvcRegulator(this, voltageSetPoint, getReactivePowerSetpoint(), getRegulationMode(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getVoltageSetPoint();
        if (Double.compare(voltageSetPoint, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setVoltageSetPoint(voltageSetPoint));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageSetpoint", variantId, oldValue, voltageSetPoint);
        }
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return getResource().getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetPoint) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), reactivePowerSetPoint, getRegulationMode(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getReactivePowerSetPoint();
        if (Double.compare(reactivePowerSetPoint, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setReactivePowerSetPoint(reactivePowerSetPoint));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "reactivePowerSetpoint", variantId, oldValue, reactivePowerSetPoint);
        }
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.valueOf(getResource().getAttributes().getRegulatingPoint().getRegulationMode());
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), getReactivePowerSetpoint(), regulationMode, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        RegulationMode oldValue = getRegulationMode();
        if (regulationMode != oldValue) {
            regulatingPoint.setRegulationMode(String.valueOf(regulationMode));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "regulationMode", variantId, oldValue, regulationMode);
        }
        return this;
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        setRegTerminal(regulatingTerminal);
        return this;
    }

    private <E extends Extension<StaticVarCompensator>> E createVoltagePerReactiveControlExtension() {
        E extension = null;
        var resource = getResource();
        VoltagePerReactivePowerControlAttributes attributes = resource.getAttributes().getVoltagePerReactiveControl();
        if (attributes != null) {
            extension = (E) new VoltagePerReactivePowerControlImpl((StaticVarCompensatorImpl) getInjection());
        }
        return extension;
    }

    private <E extends Extension<StaticVarCompensator>> E createStandbyAutomatonExtension() {
        E extension = null;
        var resource = getResource();
        StandbyAutomatonAttributes attributes = resource.getAttributes().getStandbyAutomaton();
        if (attributes != null) {
            extension = (E) new StandbyAutomatonImpl(getInjection());
        }
        return extension;
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> E getExtension(Class<? super E> type) {
        if (type == VoltagePerReactivePowerControl.class) {
            return createVoltagePerReactiveControlExtension();
        } else if (type == StandbyAutomaton.class) {
            return createStandbyAutomatonExtension();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> E getExtensionByName(String name) {
        if (name.equals("voltagePerReactivePowerControl")) {
            return createVoltagePerReactiveControlExtension();
        } else if (name.equals(StandbyAutomaton.NAME)) {
            return createStandbyAutomatonExtension();
        }
        return super.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createVoltagePerReactiveControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createStandbyAutomatonExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
        }
        regulatingPoint.remove();
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeStaticVarCompensator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}
