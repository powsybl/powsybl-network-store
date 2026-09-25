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
    protected Class<? extends VoltageRegulationHolder<?>> getVoltageRegulationHolderClass() {
        return StaticVarCompensator.class;
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
    public double getVoltageSetpoint() {
        return getRegulatingTargetV();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(double voltageSetPoint) {
        if (isRegulating() && getRegulationMode() == RegulationMode.VOLTAGE && Double.isNaN(voltageSetPoint)) {
            ValidationUtil.checkSvcRegulator(this, true, voltageSetPoint, getReactivePowerSetpoint(), RegulationMode.VOLTAGE,
                getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        }
        if (isRemoteRegulating() && getVoltageRegulation() != null) {
            getVoltageRegulation().setTargetValue(voltageSetPoint);
        } else {
            setLocalTargetV(voltageSetPoint);
        }
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return getRegulatingTargetQ();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetPoint) {
        if (isRegulating() && getRegulationMode() == RegulationMode.REACTIVE_POWER && Double.isNaN(reactivePowerSetPoint)) {
            ValidationUtil.checkSvcRegulator(this, true, getVoltageSetpoint(), reactivePowerSetPoint, RegulationMode.REACTIVE_POWER,
                getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        }
        if (isRemoteRegulating() && getVoltageRegulation() != null) {
            getVoltageRegulation().setTargetValue(reactivePowerSetPoint);
        } else {
            setLocalTargetQ(reactivePowerSetPoint);
        }
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getVoltageRegulation() == null ? null : getVoltageRegulation().getMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation().withMode(regulationMode).withRegulating(false).build();
        } else {
            getVoltageRegulation().setMode(regulationMode);
        }
        return this;
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        if (regulatingTerminal == null) {
            if (getVoltageRegulation() != null) {
                getVoltageRegulation().setTerminal(null, Double.NaN);
            }
            return this;
        }
        if (getVoltageRegulation() == null) {
            newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(false)
                .withTerminal(regulatingTerminal).withTargetValue(Double.NaN).build();
        } else {
            getVoltageRegulation().setTerminal(regulatingTerminal,
                regulatingTerminal == null ? Double.NaN : isRemoteRegulating() ? getVoltageRegulation().getTargetValue() : getRegulatingTargetV());
        }
        return this;
    }

    @Override
    public StaticVarCompensator setRegulating(boolean regulating) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withRegulating(regulating).build();
        } else {
            getVoltageRegulation().setRegulating(regulating);
        }
        return this;
    }

    @Override
    public double getLocalTargetV() {
        double value = getResource().getAttributes().getLocalTargetV();
        return Double.isNaN(value) && getVoltageRegulation() == null ? getResource().getAttributes().getVoltageSetPoint() : value;
    }

    @Override
    public StaticVarCompensator setLocalTargetV(double targetV) {
        double oldValue = getLocalTargetV();
        updateResource(res -> {
            res.getAttributes().setLocalTargetV(targetV);
            res.getAttributes().setVoltageSetPoint(targetV);
        }, "localTargetV", oldValue, targetV);
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        double value = getResource().getAttributes().getLocalTargetQ();
        return Double.isNaN(value) && getVoltageRegulation() == null ? getResource().getAttributes().getReactivePowerSetPoint() : value;
    }

    @Override
    public StaticVarCompensator setLocalTargetQ(double targetQ) {
        double oldValue = getLocalTargetQ();
        updateResource(res -> {
            res.getAttributes().setLocalTargetQ(targetQ);
            res.getAttributes().setReactivePowerSetPoint(targetQ);
        }, "localTargetQ", oldValue, targetQ);
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
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation instanceof VoltageRegulationImpl nativeRegulation) {
            nativeRegulation.onRemove();
        }
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        regulatingPoint.remove();
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
