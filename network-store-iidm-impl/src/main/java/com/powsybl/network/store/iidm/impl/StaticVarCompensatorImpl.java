/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.iidm.impl.extensions.StandbyAutomatonImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class StaticVarCompensatorImpl extends AbstractRegulatingInjection<StaticVarCompensator, StaticVarCompensatorAttributes> implements StaticVarCompensator {

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
            updateResource(res -> res.getAttributes().setBmin(bMin),
                "bMin", oldValue, bMin);
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
            updateResource(res -> res.getAttributes().setBmax(bMax),
                "bMax", oldValue, bMax);
        }
        return this;
    }

    @Override
    protected Class<? extends VoltageRegulationHolder<?>> getHolderClass() {
        return StaticVarCompensator.class;
    }

    @Override
    public double getVoltageSetpoint() {
        return this.getRegulatingTargetV();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(double voltageSetpoint) {
        double oldValue = getLocalTargetV();
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null && hasRegulatingTerminal() && isWithMode(RegulationMode.VOLTAGE)) {
            voltageRegulation.setTargetValue(voltageSetpoint);
        } else {
            setLocalTargetV(voltageSetpoint);
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "voltageSetpoint", variantId, oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return this.getRegulatingTargetQ();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetpoint) {
        double oldValue = getLocalTargetQ();
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null && hasRegulatingTerminal() && isWithMode(RegulationMode.REACTIVE_POWER)) {
            voltageRegulation.setTargetValue(reactivePowerSetpoint);
        } else {
            setLocalTargetQ(reactivePowerSetpoint);
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
        return this;
    }

    @Override
    public double getLocalTargetV() {
        return getResource().getAttributes().getLocalTargetV();
    }

    @Override
    public StaticVarCompensator setLocalTargetV(double targetV) {
        ValidationUtil.checkLocalTargetQandV(this,
            StaticVarCompensator.class,
            targetV,
            this.getLocalTargetQ(),
            getVoltageRegulation(),
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getLocalTargetV();
        if (Double.compare(targetV, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setLocalTargetV(targetV),
                "localTargetV", oldValue, targetV);
        }
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return getResource().getAttributes().getLocalTargetQ();
    }

    @Override
    public StaticVarCompensator setLocalTargetQ(double targetQ) {
        ValidationUtil.checkLocalTargetQandV(this,
            StaticVarCompensator.class,
            this.getLocalTargetV(),
            targetQ,
            getVoltageRegulation(),
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getLocalTargetQ();
        if (Double.compare(targetQ, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setLocalTargetQ(targetQ),
                "localTargetQ", oldValue, targetQ);
        }
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        return voltageRegulation != null ? voltageRegulation.getMode() : null;
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = null;
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getMode();
            voltageRegulation.setMode(regulationMode);
        } else {
            newVoltageRegulation().withRegulating(false).withMode(regulationMode).build();
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "regulationMode", variantId, oldValue, regulationMode);
        return this;
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        Terminal oldValue = null;
        double targetValue = isWithMode(RegulationMode.VOLTAGE) ? getRegulatingTargetV() : getRegulatingTargetQ();
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTerminal();
            voltageRegulation.setTerminal(regulatingTerminal, targetValue);
        } else {
            newVoltageRegulation().withRegulating(false).withTargetValue(targetValue).withTerminal(regulatingTerminal).build();
        }
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "regulatingTerminal", variantId, oldValue, regulatingTerminal);
        return this;
    }

    @Override
    public StaticVarCompensator setRegulating(boolean regulating) {
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        VoltageRegulation voltageRegulation = getVoltageRegulation();
        if (voltageRegulation != null) {
            boolean oldValue = voltageRegulation.isRegulating();
            voltageRegulation.setRegulating(regulating);
            index.notifyUpdate(this, "regulating", variantId, oldValue, regulating);
        } else if (regulating) {
            newVoltageRegulation().withRegulating(true).withMode(RegulationMode.REACTIVE_POWER).build();
            index.notifyUpdate(this, "regulating", variantId, false, true);
        }
        return this;
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
        if (type == StandbyAutomaton.class) {
            return createStandbyAutomatonExtension();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> E getExtensionByName(String name) {
        if (name.equals(StandbyAutomaton.NAME)) {
            return createStandbyAutomatonExtension();
        }
        return super.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createStandbyAutomatonExtension();
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
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        removeVoltageRegulationRefs();
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeStaticVarCompensator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> boolean removeExtension(Class<E> type) {
        super.removeExtension(type);
        if (type.isAssignableFrom(ConnectablePosition.class)) {
            var resource = getResource();
            if (resource.getAttributes().getPosition() != null) {
                resource.getAttributes().setPosition(null);
                return true;
            }
            return false;
        }
        if (type == StandbyAutomaton.class) {
            var resource = getResource();
            if (resource.getAttributes().getStandbyAutomaton() != null) {
                resource.getAttributes().setStandbyAutomaton(null);
                return true;
            }
            return false;
        }
        return false;
    }
}
