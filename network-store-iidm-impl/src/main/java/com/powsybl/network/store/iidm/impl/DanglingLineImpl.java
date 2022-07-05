/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SV;
import com.powsybl.network.store.model.*;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DanglingLineImpl extends AbstractInjectionImpl<DanglingLine, DanglingLineAttributes> implements DanglingLine, LimitsOwner<Void> {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private final DanglingLineImpl danglingLine;

        private final DanglingLineGenerationAttributes attributes;

        private final String id;

        GenerationImpl(DanglingLineImpl danglingLine, DanglingLineGenerationAttributes attributes, String id) {
            this.danglingLine = Objects.requireNonNull(danglingLine);
            this.attributes = attributes;
            if (this.attributes.getReactiveLimits() == null) {
                this.attributes.setReactiveLimits(new MinMaxReactiveLimitsAttributes(-Double.MAX_VALUE, Double.MAX_VALUE));
                danglingLine.updateResource();
            }
            this.id = id;
        }

        @Override
        public double getTargetP() {
            return attributes.getTargetP();
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            ValidationUtil.checkActivePowerSetpoint(danglingLine, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = attributes.getTargetP();
            attributes.setTargetP(targetP);
            danglingLine.updateResource();
            String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
            danglingLine.notifyUpdate("targetP", variantId, oldValue, targetP);
            return this;
        }

        @Override
        public double getMaxP() {
            return attributes.getMaxP();
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            ValidationUtil.checkMaxP(danglingLine, maxP);
            ValidationUtil.checkActivePowerLimits(danglingLine, getMinP(), maxP);
            double oldValue = attributes.getMaxP();
            attributes.setMaxP(maxP);
            danglingLine.updateResource();
            danglingLine.notifyUpdate("maxP", oldValue, maxP);
            return this;
        }

        @Override
        public double getMinP() {
            return attributes.getMinP();
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            ValidationUtil.checkMinP(danglingLine, minP);
            ValidationUtil.checkActivePowerLimits(danglingLine, minP, getMaxP());
            double oldValue = attributes.getMinP();
            attributes.setMinP(minP);
            danglingLine.updateResource();
            danglingLine.notifyUpdate("minP", oldValue, minP);
            return this;
        }

        @Override
        public double getTargetQ() {
            return attributes.getTargetQ();
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), getTargetV(), targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = attributes.getTargetQ();
            attributes.setTargetQ(targetQ);
            danglingLine.updateResource();
            String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
            danglingLine.notifyUpdate("targetQ", variantId, oldValue, targetQ);
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return attributes.isVoltageRegulationOn();
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn, getTargetV(), getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
            boolean oldValue = attributes.isVoltageRegulationOn();
            attributes.setVoltageRegulationOn(voltageRegulationOn);
            danglingLine.updateResource();
            danglingLine.notifyUpdate("voltageRegulationOn", oldValue, voltageRegulationOn);
            return this;
        }

        @Override
        public double getTargetV() {
            return attributes.getTargetV();
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), targetV, getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = attributes.getTargetV();
            attributes.setTargetV(targetV);
            danglingLine.updateResource();
            String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
            danglingLine.notifyUpdate("targetV", variantId, oldValue, targetV);
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
            return new ReactiveCapabilityCurveAdderImpl<>(this);
        }

        @Override
        public MinMaxReactiveLimitsAdderImpl newMinMaxReactiveLimits() {
            return new MinMaxReactiveLimitsAdderImpl<>(this);
        }

        @Override
        public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
            ReactiveLimitsAttributes oldValue = attributes.getReactiveLimits();
            attributes.setReactiveLimits(reactiveLimits);
            danglingLine.updateResource();
            danglingLine.notifyUpdate("reactiveLimits", oldValue, reactiveLimits);
        }

        @Override
        public ReactiveLimits getReactiveLimits() {
            if (attributes.getReactiveLimits().getKind() == ReactiveLimitsKind.MIN_MAX) {
                return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) attributes.getReactiveLimits());
            } else {
                return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) attributes.getReactiveLimits());
            }
        }

        @Override
        public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
            ReactiveLimits reactiveLimits = getReactiveLimits();
            if (type == null) {
                throw new IllegalArgumentException("type is null");
            }
            if (type.isInstance(reactiveLimits)) {
                return type.cast(reactiveLimits);
            } else {
                throw new ValidationException(this, "incorrect reactive limits type " + type.getName() + ", expected " + reactiveLimits.getClass());
            }
        }

        @Override
        public String getMessageHeader() {
            return "generation part for dangling line '" + id + "': ";
        }
    }

    static class BoundaryImpl implements Boundary {

        private final DanglingLine danglingLine;

        BoundaryImpl(DanglingLine danglingLine) {
            this.danglingLine = Objects.requireNonNull(danglingLine);
        }

        @Override
        public double getV() {
            Terminal t = danglingLine.getTerminal();
            Bus b = t.getBusView().getBus();
            return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), Branch.Side.ONE).otherSideU(danglingLine, true);
        }

        @Override
        public double getAngle() {
            Terminal t = danglingLine.getTerminal();
            Bus b = t.getBusView().getBus();
            return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), Branch.Side.ONE).otherSideA(danglingLine, true);
        }

        @Override
        public double getP() {
            Terminal t = danglingLine.getTerminal();
            Bus b = t.getBusView().getBus();
            return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), Branch.Side.ONE).otherSideP(danglingLine, true);
        }

        @Override
        public double getQ() {
            Terminal t = danglingLine.getTerminal();
            Bus b = t.getBusView().getBus();
            return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), Branch.Side.ONE).otherSideQ(danglingLine, true);
        }

        @Override
        public Branch.Side getSide() {
            return null;
        }

        @Override
        public Connectable getConnectable() {
            return danglingLine;
        }

        @Override
        public VoltageLevel getVoltageLevel() {
            return danglingLine.getTerminal().getVoltageLevel();
        }
    }

    private final BoundaryImpl boundary = new BoundaryImpl(this);

    public DanglingLineImpl(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        super(index, resource);
    }

    static DanglingLineImpl create(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        return new DanglingLineImpl(index, resource);
    }

    public void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(this, attribute, oldValue, newValue);
    }

    public void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        index.notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeDanglingLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal().removeDanglingSwitches();
        }
    }

    @Override
    protected DanglingLine getInjection() {
        return this;
    }

    @Override
    public double getP0() {
        return checkResource().getAttributes().getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        var resource = checkResource();
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = resource.getAttributes().getP0();
        resource.getAttributes().setP0(p0);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return checkResource().getAttributes().getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        var resource = checkResource();
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = resource.getAttributes().getQ0();
        resource.getAttributes().setQ0(q0);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public double getR() {
        return checkResource().getAttributes().getR();
    }

    @Override
    public DanglingLine setR(double r) {
        var resource = checkResource();
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        updateResource();
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return checkResource().getAttributes().getX();
    }

    @Override
    public DanglingLine setX(double x) {
        var resource = checkResource();
        ValidationUtil.checkX(this, x);
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        updateResource();
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return checkResource().getAttributes().getG();
    }

    @Override
    public DanglingLine setG(double g) {
        var resource = checkResource();
        ValidationUtil.checkG(this, g);
        double oldValue = resource.getAttributes().getG();
        resource.getAttributes().setG(g);
        updateResource();
        notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return checkResource().getAttributes().getB();
    }

    @Override
    public DanglingLine setB(double b) {
        var resource = checkResource();
        ValidationUtil.checkB(this, b);
        double oldValue = resource.getAttributes().getB();
        resource.getAttributes().setB(b);
        updateResource();
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public DanglingLine.Generation getGeneration() {
        var resource = checkResource();
        if (resource.getAttributes().getGeneration() != null) {
            return new GenerationImpl(this, resource.getAttributes().getGeneration(), getId());
        } else {
            return null;
        }
    }

    @Override
    public String getUcteXnodeCode() {
        return checkResource().getAttributes().getUcteXnodeCode();
    }

    public DanglingLine setUcteXnodeCode(String ucteXnodeCode) {
        var resource = checkResource();
        String oldValue = resource.getAttributes().getUcteXnodeCode();
        resource.getAttributes().setUcteXnodeCode(ucteXnodeCode);
        updateResource();
        notifyUpdate("ucteXnodeCode", oldValue, ucteXnodeCode);
        return this;
    }

    @Override
    public void setCurrentLimits(Void side, LimitsAttributes currentLimits) {
        var resource = checkResource();
        LimitsAttributes oldValue = resource.getAttributes().getCurrentLimits();
        resource.getAttributes().setCurrentLimits(currentLimits);
        updateResource();
        notifyUpdate("currentLimits", oldValue, currentLimits);
    }

    @Override
    public AbstractIdentifiableImpl getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        var resource = checkResource();
        return resource.getAttributes().getCurrentLimits() != null
                ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits())
                : null;
    }

    @Override
    public ActivePowerLimits getActivePowerLimits() {
        var resource = checkResource();
        return resource.getAttributes().getActivePowerLimits() != null
                ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits())
                : null;
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits() {
        var resource = checkResource();
        return resource.getAttributes().getApparentPowerLimits() != null
                ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits())
                : null;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl<>(null, this);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl<>(null, this);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl<>(null, this);
    }

    @Override
    public void setApparentPowerLimits(Void unused, LimitsAttributes apparentPowerLimitsAttributes) {
        var resource = checkResource();
        LimitsAttributes oldValue = resource.getAttributes().getApparentPowerLimits();
        resource.getAttributes().setApparentPowerLimits(apparentPowerLimitsAttributes);
        updateResource();
        notifyUpdate("apparentPowerLimits", oldValue, apparentPowerLimitsAttributes);
    }

    @Override
    public void setActivePowerLimits(Void unused, LimitsAttributes activePowerLimitsAttributes) {
        var resource = checkResource();
        LimitsAttributes oldValue = resource.getAttributes().getActivePowerLimits();
        resource.getAttributes().setActivePowerLimits(activePowerLimitsAttributes);
        updateResource();
        notifyUpdate("activePowerLimits", oldValue, activePowerLimitsAttributes);
    }

    @Override
    public <E extends Extension<DanglingLine>> void addExtension(Class<? super E> type, E extension) {
        if (type == Xnode.class) {
            Xnode xnode = (Xnode) extension;
            setUcteXnodeCode(xnode.getCode());
        }
        super.addExtension(type, extension);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<DanglingLine>> E getExtension(Class<? super E> type) {
        if (type == Xnode.class) {
            return (E) createXnodeExtension();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<DanglingLine>> E getExtensionByName(String name) {
        if (name.equals("xnode")) {
            return (E) createXnodeExtension();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<DanglingLine>> E createXnodeExtension() {
        E extension = null;
        var resource = checkResource();
        DanglingLineImpl dl = index.getDanglingLine(resource.getId())
                .orElseThrow(() -> new PowsyblException("DanglingLine " + resource.getId() + " doesn't exist"));
        String xNodeCode = resource.getAttributes().getUcteXnodeCode();
        if (xNodeCode != null) {
            extension = (E) new XnodeImpl(dl);
        }
        return extension;
    }

    @Override
    public <E extends Extension<DanglingLine>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createXnodeExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public Boundary getBoundary() {
        return boundary;
    }
}
