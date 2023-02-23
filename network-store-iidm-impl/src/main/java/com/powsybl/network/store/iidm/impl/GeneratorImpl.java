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
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.network.store.iidm.impl.extensions.ActivePowerControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.CoordinatedReactiveControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.GeneratorEntsoeCategoryImpl;
import com.powsybl.network.store.iidm.impl.extensions.GeneratorShortCircuitImpl;
import com.powsybl.network.store.iidm.impl.extensions.GeneratorStartupImpl;
import com.powsybl.network.store.iidm.impl.extensions.RemoteReactivePowerControlImpl;
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
    public EnergySource getEnergySource() {
        return checkResource().getAttributes().getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        ValidationUtil.checkEnergySource(this, energySource);
        EnergySource oldValue = checkResource().getAttributes().getEnergySource();
        if (energySource != oldValue) {
            updateResource(res -> res.getAttributes().setEnergySource(energySource));
            index.notifyUpdate(this, "energySource", oldValue.toString(), energySource.toString());
        }
        return this;
    }

    @Override
    public double getMaxP() {
        return checkResource().getAttributes().getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP);
        double oldValue = checkResource().getAttributes().getMaxP();
        if (maxP != oldValue) {
            updateResource(res -> res.getAttributes().setMaxP(maxP));
            index.notifyUpdate(this, "maxP", oldValue, maxP);
        }
        return this;
    }

    @Override
    public double getMinP() {
        return checkResource().getAttributes().getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        var resource = checkResource();
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP());
        double oldValue = resource.getAttributes().getMinP();
        if (minP != oldValue) {
            updateResource(res -> res.getAttributes().setMinP(minP));
            index.notifyUpdate(this, "minP", oldValue, minP);
        }
        return this;
    }

    @Override
    public void remove() {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeGenerator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return checkResource().getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getTargetV(), getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        boolean oldValue = checkResource().getAttributes().isVoltageRegulatorOn();
        if (voltageRegulatorOn != oldValue) {
            updateResource(res -> res.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        }
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        var resource = checkResource();
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes oldValue = checkResource().getAttributes().getRegulatingTerminal();
        updateResource(res -> res.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        index.notifyUpdate(this, "regulatingTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return checkResource().getAttributes().getTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV, getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = checkResource().getAttributes().getTargetV();
        if (Double.compare(targetV, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setTargetV(targetV));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetV", variantId, oldValue, targetV);
        }
        return this;
    }

    @Override
    public double getTargetP() {
        return checkResource().getAttributes().getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        ValidationUtil.checkActivePowerSetpoint(this, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = checkResource().getAttributes().getTargetP();
        if (targetP != oldValue) {
            updateResource(res -> res.getAttributes().setTargetP(targetP));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetP", variantId, oldValue, targetP);
        }
        return this;
    }

    @Override
    public double getTargetQ() {
        return checkResource().getAttributes().getTargetQ();
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        var resource = checkResource();
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), getTargetV(), targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = resource.getAttributes().getTargetQ();
        if (Double.compare(targetQ, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setTargetQ(targetQ));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetQ", variantId, oldValue, targetQ);
        }
        return this;
    }

    @Override
    public double getRatedS() {
        return checkResource().getAttributes().getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = checkResource().getAttributes().getRatedS();
        if (Double.compare(ratedS, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setRatedS(ratedS));
            index.notifyUpdate(this, "ratedS", oldValue, ratedS);
        }
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        updateResource(res -> res.getAttributes().setReactiveLimits(reactiveLimits));
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        var resource = checkResource();
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
        return new ReactiveCapabilityCurveAdderImpl<>(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl<>(this);
    }

    @Override
    public <E extends Extension<Generator>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        var resource = checkResource();
        if (type == CoordinatedReactiveControl.class) {
            CoordinatedReactiveControl coordinatedReactiveControl = (CoordinatedReactiveControl) extension;
            resource.getAttributes().setCoordinatedReactiveControl(CoordinatedReactiveControlAttributes.builder()
                    .qPercent(coordinatedReactiveControl.getQPercent())
                    .build());
            updateResource();
        } else if (type == RemoteReactivePowerControl.class) {
            RemoteReactivePowerControl remoteReactivePowerControl = (RemoteReactivePowerControl) extension;
            resource.getAttributes().setRemoteReactivePowerControl(RemoteReactivePowerControlAttributes.builder()
                    .targetQ(remoteReactivePowerControl.getTargetQ())
                    .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(remoteReactivePowerControl.getRegulatingTerminal()))
                    .enabled(remoteReactivePowerControl.isEnabled())
                    .build());
            updateResource();
        } else if (type == GeneratorShortCircuit.class) {
            GeneratorShortCircuit generatorShortCircuit = (GeneratorShortCircuit) extension;
            resource.getAttributes().setGeneratorShortCircuitAttributes(GeneratorShortCircuitAttributes.builder()
                    .directSubtransX(generatorShortCircuit.getDirectSubtransX())
                    .directTransX(generatorShortCircuit.getDirectTransX())
                    .stepUpTransformerX(generatorShortCircuit.getStepUpTransformerX())
                    .build());
            updateResource();
        }
    }

    private <E extends Extension<Generator>> E createActivePowerControlExtension() {
        E extension = null;
        var resource = checkResource();
        ActivePowerControlAttributes attributes = resource.getAttributes().getActivePowerControl();
        if (attributes != null) {
            extension = (E) new ActivePowerControlImpl<>(getInjection());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createCoordinatedReactiveControlExtension() {
        E extension = null;
        var resource = checkResource();
        CoordinatedReactiveControlAttributes attributes = resource.getAttributes().getCoordinatedReactiveControl();
        if (attributes != null) {
            extension = (E) new CoordinatedReactiveControlImpl((GeneratorImpl) getInjection(), attributes.getQPercent());
        }
        return extension;
    }

    public Terminal getRemoteReactivePowerControlRegulatingTerminal() {
        var resource = checkResource();
        RemoteReactivePowerControlAttributes attributes = resource.getAttributes().getRemoteReactivePowerControl();
        if (attributes != null) {
            TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
            return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        } else {
            return null;
        }
    }

    private <E extends Extension<Generator>> E createRemoteReactivePowerControlExtension() {
        E extension = null;
        var resource = checkResource();
        RemoteReactivePowerControlAttributes attributes = resource.getAttributes().getRemoteReactivePowerControl();
        if (attributes != null) {
            extension = (E) new RemoteReactivePowerControlImpl((GeneratorImpl) getInjection(), attributes.getTargetQ(), attributes.getRegulatingTerminal(), attributes.isEnabled());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createGeneratorStartupExtension() {
        E extension = null;
        var resource = checkResource();
        GeneratorStartupAttributes attributes = resource.getAttributes().getGeneratorStartupAttributes();
        if (attributes != null) {
            extension = (E) new GeneratorStartupImpl((GeneratorImpl) getInjection(),
                    attributes.getPlannedActivePowerSetpoint(), attributes.getStartupCost(),
                    attributes.getMarginalCost(), attributes.getPlannedOutageRate(), attributes.getForcedOutageRate());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createGeneratorShortCircuitExtension() {
        E extension = null;
        var resource = checkResource();
        GeneratorShortCircuitAttributes attributes = resource.getAttributes().getGeneratorShortCircuitAttributes();
        if (attributes != null) {
            extension = (E) new GeneratorShortCircuitImpl((GeneratorImpl) getInjection(),
                    attributes.getDirectSubtransX(), attributes.getDirectTransX(), attributes.getStepUpTransformerX());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createEntsoeCategoryExtension() {
        E extension = null;
        var resource = checkResource();
        GeneratorEntsoeCategoryAttributes attributes = resource.getAttributes().getEntsoeCategoryAttributes();
        if (attributes != null) {
            extension = (E) new GeneratorEntsoeCategoryImpl((GeneratorImpl) getInjection(), attributes.getCode());
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
        } else if (type == GeneratorEntsoeCategory.class) {
            extension = createEntsoeCategoryExtension();
        } else if (type == RemoteReactivePowerControl.class) {
            extension = createRemoteReactivePowerControlExtension();
        } else if (type == GeneratorStartup.class) {
            extension = createGeneratorStartupExtension();
        } else if (type == GeneratorShortCircuit.class) {
            extension = createGeneratorShortCircuitExtension();
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
        } else if (name.equals("entsoeCategory")) {
            extension = createEntsoeCategoryExtension();
        } else if (name.equals("remoteReactivePowerControl")) {
            extension = createRemoteReactivePowerControlExtension();
        } else if (name.equals("startup")) {
            extension = createGeneratorStartupExtension();
        } else if (name.equals("generatorShortCircuit")) {
            extension = createGeneratorShortCircuitExtension();
        }
        return extension;
    }

    private <E extends Extension<Generator>> void addIfNotNull(Collection<E> list, E extension) {
        if (extension != null) {
            list.add(extension);
        }
    }

    @Override
    public <E extends Extension<Generator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        addIfNotNull(extensions, createActivePowerControlExtension());
        addIfNotNull(extensions, createCoordinatedReactiveControlExtension());
        addIfNotNull(extensions, createEntsoeCategoryExtension());
        addIfNotNull(extensions, createRemoteReactivePowerControlExtension());
        addIfNotNull(extensions, createGeneratorStartupExtension());
        addIfNotNull(extensions, createGeneratorShortCircuitExtension());
        return extensions;
    }

    public GeneratorImpl initGeneratorStartupAttributes(double plannedActivePowerSetpoint, double startupCost, double marginalCost, double plannedOutageRate, double forcedOutageRate) {
        checkResource().getAttributes().setGeneratorStartupAttributes(new GeneratorStartupAttributes(plannedActivePowerSetpoint, startupCost, marginalCost, plannedOutageRate, forcedOutageRate));
        updateResource();
        return this;
    }

    public GeneratorImpl initGeneratorShortCircuitAttributes(double directSubtransX, double directTransX, double stepUpTransformerX) {
        checkResource().getAttributes().setGeneratorShortCircuitAttributes(new GeneratorShortCircuitAttributes(directSubtransX, directTransX, stepUpTransformerX));
        return this;
    }
}
