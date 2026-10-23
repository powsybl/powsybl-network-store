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
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.iidm.impl.extensions.CoordinatedReactiveControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.GeneratorEntsoeCategoryImpl;
import com.powsybl.network.store.iidm.impl.extensions.GeneratorShortCircuitImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class GeneratorImpl extends AbstractRegulatingInjection<Generator, GeneratorAttributes> implements Generator, ReactiveLimitsOwner {

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
    protected Class<? extends VoltageRegulationHolder<?>> getVoltageRegulationHolderClass() {
        return Generator.class;
    }

    @Override
    public EnergySource getEnergySource() {
        return getResource().getAttributes().getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        ValidationUtil.checkEnergySource(this, energySource);
        EnergySource oldValue = getResource().getAttributes().getEnergySource();
        if (energySource != oldValue) {
            updateResource(res -> res.getAttributes().setEnergySource(energySource),
                "energySource", oldValue, energySource);
        }
        return this;
    }

    @Override
    public double getMaxP() {
        return getResource().getAttributes().getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP);
        double oldValue = getResource().getAttributes().getMaxP();
        if (maxP != oldValue) {
            updateResource(res -> res.getAttributes().setMaxP(maxP),
                "maxP", oldValue, maxP);
        }
        return this;
    }

    @Override
    public double getMinP() {
        return getResource().getAttributes().getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        var resource = getResource();
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP());
        double oldValue = resource.getAttributes().getMinP();
        if (minP != oldValue) {
            updateResource(res -> res.getAttributes().setMinP(minP),
                "minP", oldValue, minP);
        }
        return this;
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
        index.removeGenerator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return this.isRegulatingWithMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE);
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getTargetV(), getTargetQ(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        boolean oldValue = this.isRegulating();
        if (voltageRegulatorOn != oldValue) {
            this.setRegulating(voltageRegulatorOn);
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        }
        return this;
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        setRegTerminal(regulatingTerminal);
        return this;
    }

    @Override
    public double getTargetV() {
        return getRegulatingTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        updateTargetV(targetV);
        updateEquivalentLocalTargetV(Double.NaN);
        return this;
    }

    @Override
    public Generator setTargetV(double targetV, double localTargetV) {
        updateTargetV(targetV);
        updateEquivalentLocalTargetV(localTargetV);
        return this;
    }

    private void updateTargetV(double targetV) {
        if (Double.isNaN(targetV)) {
            ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        }
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV, getTargetQ(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation != null && regulation.isWithTerminal() && regulation.getMode() == com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE) {
            regulation.setTargetValue(targetV);
        } else {
            setLocalTargetV(targetV);
        }
    }

    private void updateEquivalentLocalTargetV(double localTargetV) {
        double oldValue = getResource().getAttributes().getEquivalentLocalTargetV();
        if (Double.compare(localTargetV, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setEquivalentLocalTargetV(localTargetV),
                    "EquivalentLocalTargetV", oldValue, localTargetV);
        }
    }

    @Override
    public double getEquivalentLocalTargetV() {
        return getLocalTargetV();
    }

    @Override
    public double getLocalTargetV() {
        double value = getResource().getAttributes().getLocalTargetV();
        return Double.isNaN(value) ? getResource().getAttributes().getEquivalentLocalTargetV() : value;
    }

    @Override
    public Generator setLocalTargetV(double targetV) {
        double oldValue = getLocalTargetV();
        updateResource(resource -> {
            resource.getAttributes().setLocalTargetV(targetV);
            resource.getAttributes().setEquivalentLocalTargetV(targetV);
        }, "localTargetV", oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return getResource().getAttributes().getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        ValidationUtil.checkActivePowerSetpoint(this, targetP, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getTargetP();
        if (targetP != oldValue) {
            updateResource(res -> res.getAttributes().setTargetP(targetP),
                "targetP", oldValue, targetP);
        }
        return this;
    }

    @Override
    public double getTargetQ() {
        return getLocalTargetQ();
    }

    @Override
    public double getLocalTargetQ() {
        double value = getResource().getAttributes().getLocalTargetQ();
        return Double.isNaN(value) ? getResource().getAttributes().getTargetQ() : value;
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        return setLocalTargetQ(targetQ);
    }

    @Override
    public Generator setLocalTargetQ(double targetQ) {
        if (Double.isNaN(targetQ) && !isVoltageRegulatorOn()
            && getNetwork().getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0) {
            throw new ValidationException(this, "invalid value (NaN) for reactive power setpoint (voltage regulator is off)");
        }
        var resource = getResource();
        double oldValue = getLocalTargetQ();
        if (Double.compare(targetQ, oldValue) != 0) { // could be nan
            updateResource(res -> {
                res.getAttributes().setTargetQ(targetQ);
                res.getAttributes().setLocalTargetQ(targetQ);
            }, "localTargetQ", oldValue, targetQ);
        }
        return this;
    }

    @Override
    public double getRatedS() {
        return getResource().getAttributes().getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = getResource().getAttributes().getRatedS();
        if (Double.compare(ratedS, oldValue) != 0) { // could be nan
            updateResource(res -> res.getAttributes().setRatedS(ratedS),
                "ratedS", oldValue, ratedS);
        }
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        ReactiveLimitsAttributes oldValue = getResource().getAttributes().getReactiveLimits();
        updateResource(res -> res.getAttributes().setReactiveLimits(reactiveLimits),
            "reactiveLimits", oldValue, reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        var resource = getResource();
        ReactiveLimitsAttributes reactiveLimitsAttributes = resource.getAttributes().getReactiveLimits();
        if (reactiveLimitsAttributes.getKind() == ReactiveLimitsKind.CURVE) {
            return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimitsAttributes, this);
        } else {
            return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimitsAttributes, this);
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
    public ReactiveCapabilityShapeAdder newReactiveCapabilityShape() {
        throw new UnsupportedOperationException("Reactive capability shape feature not implemented yet");
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl<>(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl<>(this, this);
    }

    private <E extends Extension<Generator>> E createCoordinatedReactiveControlExtension() {
        E extension = null;
        var resource = getResource();
        CoordinatedReactiveControlAttributes attributes = resource.getAttributes().getCoordinatedReactiveControl();
        if (attributes != null) {
            extension = (E) new CoordinatedReactiveControlImpl((GeneratorImpl) getInjection());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createGeneratorShortCircuitExtension() {
        E extension = null;
        var resource = getResource();
        ShortCircuitAttributes attributes = resource.getAttributes().getGeneratorShortCircuitAttributes();
        if (attributes != null) {
            extension = (E) new GeneratorShortCircuitImpl((GeneratorImpl) getInjection());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createEntsoeCategoryExtension() {
        E extension = null;
        var resource = getResource();
        GeneratorEntsoeCategoryAttributes attributes = resource.getAttributes().getEntsoeCategoryAttributes();
        if (attributes != null) {
            extension = (E) new GeneratorEntsoeCategoryImpl((GeneratorImpl) getInjection());
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == CoordinatedReactiveControl.class) {
            extension = createCoordinatedReactiveControlExtension();
        } else if (type == GeneratorEntsoeCategory.class) {
            extension = createEntsoeCategoryExtension();
        } else if (type == GeneratorShortCircuit.class) {
            extension = createGeneratorShortCircuitExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if ("coordinatedReactiveControl".equals(name)) {
            extension = createCoordinatedReactiveControlExtension();
        } else if ("entsoeCategory".equals(name)) {
            extension = createEntsoeCategoryExtension();
        } else if ("generatorShortCircuit".equals(name)) {
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
        addIfNotNull(extensions, createCoordinatedReactiveControlExtension());
        addIfNotNull(extensions, createEntsoeCategoryExtension());
        addIfNotNull(extensions, createGeneratorShortCircuitExtension());
        return extensions;
    }

    @Override
    public boolean isCondenser() {
        return getResource().getAttributes().isCondenser();
    }

    @Override
    public <E extends Extension<Generator>> boolean removeExtension(Class<E> type) {
        super.removeExtension(type);
        if (type == GeneratorEntsoeCategory.class) {
            var resource = getResource();
            if (resource.getAttributes().getEntsoeCategoryAttributes() != null) {
                resource.getAttributes().setEntsoeCategoryAttributes(null);
                return true;
            }
            return false;
        }
        if (type == CoordinatedReactiveControl.class) {
            var resource = getResource();
            if (resource.getAttributes().getCoordinatedReactiveControl() != null) {
                resource.getAttributes().setCoordinatedReactiveControl(null);
                return true;
            }
            return false;
        }
        if (type == GeneratorShortCircuit.class) {
            var resource = getResource();
            if (resource.getAttributes().getGeneratorShortCircuitAttributes() != null) {
                resource.getAttributes().setGeneratorShortCircuitAttributes(null);
                return true;
            }
            return false;
        }
        if (type.isAssignableFrom(ConnectablePosition.class)) {
            var resource = getResource();
            if (resource.getAttributes().getPosition() != null) {
                resource.getAttributes().setPosition(null);
                return true;
            }
            return false;
        }
        return false;
    }
}
