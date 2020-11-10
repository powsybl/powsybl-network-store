/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControlImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class GeneratorImpl extends AbstractInjectionImpl<Generator, GeneratorAttributes> implements Generator, ReactiveLimitsOwner {

    public GeneratorImpl(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        super(index, resource);
    }

    static GeneratorImpl create(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        return new GeneratorImpl(index, resource);
    }

    @Override
    protected Generator getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.GENERATOR;
    }

    @Override
    public EnergySource getEnergySource() {
        return resource.getAttributes().getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        resource.getAttributes().setEnergySource(energySource);
        return this;
    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        resource.getAttributes().setMaxP(maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return resource.getAttributes().getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        resource.getAttributes().setMinP(minP);
        return this;
    }

    @Override
    public void remove() {
        index.removeGenerator(resource.getId());
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return resource.getAttributes().getTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        resource.getAttributes().setTargetV(targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return resource.getAttributes().getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        resource.getAttributes().setTargetP(targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return resource.getAttributes().getTargetQ();
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        resource.getAttributes().setTargetQ(targetQ);
        return this;
    }

    @Override
    public double getRatedS() {
        return resource.getAttributes().getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
        resource.getAttributes().setRatedS(ratedS);
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        resource.getAttributes().setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        ReactiveLimitsAttributes reactiveLimitsAttributes = resource.getAttributes().getReactiveLimits();
        if (reactiveLimitsAttributes.getKind() == ReactiveLimitsKind.CURVE) {
            return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimitsAttributes);
        } else {
            return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimitsAttributes);
        }
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        ReactiveLimits reactiveLimits = getReactiveLimits();
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new PowsyblException("incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    @Override
    public <E extends Extension<Generator>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ActivePowerControl.class) {
            ActivePowerControl activePowerControl = (ActivePowerControl) extension;
            resource.getAttributes().setActivePowerControl(ActivePowerControlAttributes.builder()
                    .participate(activePowerControl.isParticipate())
                    .droop(activePowerControl.getDroop())
                    .build());
        } else if (type == CoordinatedReactiveControl.class) {
            CoordinatedReactiveControl coordinatedReactiveControl = (CoordinatedReactiveControl) extension;
            resource.getAttributes().setCoordinatedReactiveControl(CoordinatedReactiveControlAttributes.builder()
                    .qPercent(coordinatedReactiveControl.getQPercent())
                    .build());
        }
    }

    private <E extends Extension<Generator>> E createActivePowerControlExtension() {
        E extension = null;
        ActivePowerControlAttributes attributes = resource.getAttributes().getActivePowerControl();
        if (attributes != null) {
            extension = (E) new ActivePowerControlImpl<>(getInjection(), attributes.isParticipate(), attributes.getDroop());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createCoordinatedReactiveControlExtension() {
        E extension = null;
        CoordinatedReactiveControlAttributes attributes = resource.getAttributes().getCoordinatedReactiveControl();
        if (attributes != null) {
            extension = (E) new CoordinatedReactiveControlImpl(getInjection(), attributes.getQPercent());
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ActivePowerControl.class) {
            extension = createActivePowerControlExtension();
        } else if (type == CoordinatedReactiveControl.class) {
            extension = createCoordinatedReactiveControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("activePowerControl")) {
            extension = createActivePowerControlExtension();
        } else if (name.equals("coordinatedReactiveControl")) {
            extension = createCoordinatedReactiveControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createActivePowerControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCoordinatedReactiveControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }
}
