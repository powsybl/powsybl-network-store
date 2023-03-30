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
import java.util.Optional;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DanglingLineImpl extends AbstractInjectionImpl<DanglingLine, DanglingLineAttributes> implements DanglingLine, LimitsOwner<Void> {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private final DanglingLineImpl danglingLine;

        GenerationImpl(DanglingLineImpl danglingLine) {
            this.danglingLine = Objects.requireNonNull(danglingLine);
        }

        private static DanglingLineGenerationAttributes getAttributes(Resource<DanglingLineAttributes> resource) {
            return resource.getAttributes().getGeneration();
        }

        private DanglingLineGenerationAttributes getAttributes() {
            return getAttributes(danglingLine.getResource());
        }

        @Override
        public double getTargetP() {
            return getAttributes().getTargetP();
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            ValidationUtil.checkActivePowerSetpoint(danglingLine, targetP, ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = getAttributes().getTargetP();
            if (targetP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetP(targetP));
                String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
                danglingLine.notifyUpdate("targetP", variantId, oldValue, targetP);
            }
            return this;
        }

        @Override
        public double getMaxP() {
            return getAttributes().getMaxP();
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            ValidationUtil.checkMaxP(danglingLine, maxP);
            ValidationUtil.checkActivePowerLimits(danglingLine, getMinP(), maxP);
            double oldValue = getAttributes().getMaxP();
            if (maxP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setMaxP(maxP));
                danglingLine.notifyUpdate("maxP", oldValue, maxP);
            }
            return this;
        }

        @Override
        public double getMinP() {
            return getAttributes().getMinP();
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            ValidationUtil.checkMinP(danglingLine, minP);
            ValidationUtil.checkActivePowerLimits(danglingLine, minP, getMaxP());
            double oldValue = getAttributes().getMinP();
            if (minP != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setMinP(minP));
                danglingLine.notifyUpdate("minP", oldValue, minP);
            }
            return this;
        }

        @Override
        public double getTargetQ() {
            return getAttributes().getTargetQ();
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), getTargetV(), targetQ, ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = getAttributes().getTargetQ();
            if (targetQ != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetQ(targetQ));
                String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
                danglingLine.notifyUpdate("targetQ", variantId, oldValue, targetQ);
            }
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return getAttributes().isVoltageRegulationOn();
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn, getTargetV(), getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
            boolean oldValue = getAttributes().isVoltageRegulationOn();
            if (voltageRegulationOn != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setVoltageRegulationOn(voltageRegulationOn));
                danglingLine.notifyUpdate("voltageRegulationOn", oldValue, voltageRegulationOn);
            }
            return this;
        }

        @Override
        public double getTargetV() {
            return getAttributes().getTargetV();
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            ValidationUtil.checkVoltageControl(danglingLine, isVoltageRegulationOn(), targetV, getTargetQ(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
            double oldValue = getAttributes().getTargetV();
            if (targetV != oldValue) {
                danglingLine.updateResource(res -> getAttributes(res).setTargetV(targetV));
                String variantId = danglingLine.getNetwork().getVariantManager().getWorkingVariantId();
                danglingLine.notifyUpdate("targetV", variantId, oldValue, targetV);
            }
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdderImpl<GenerationImpl> newReactiveCapabilityCurve() {
            return new ReactiveCapabilityCurveAdderImpl<>(this);
        }

        @Override
        public MinMaxReactiveLimitsAdderImpl<GenerationImpl> newMinMaxReactiveLimits() {
            return new MinMaxReactiveLimitsAdderImpl<>(this);
        }

        @Override
        public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
            ReactiveLimitsAttributes oldValue = getAttributes().getReactiveLimits();
            danglingLine.updateResource(res -> getAttributes(res).setReactiveLimits(reactiveLimits));
            danglingLine.notifyUpdate("reactiveLimits", oldValue, reactiveLimits);
        }

        @Override
        public ReactiveLimits getReactiveLimits() {
            ReactiveLimitsAttributes reactiveLimits = getAttributes().getReactiveLimits();
            if (reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX) {
                return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimits);
            } else {
                return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimits);
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
            return "generation part for dangling line '" + danglingLine.getId() + "': ";
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
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeDanglingLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    protected DanglingLine getInjection() {
        return this;
    }

    @Override
    public double getP0() {
        return getResource().getAttributes().getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getP0();
        if (p0 != oldValue) {
            updateResource(res -> res.getAttributes().setP0(p0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            notifyUpdate("p0", variantId, oldValue, p0);
        }
        return this;
    }

    @Override
    public double getQ0() {
        return getResource().getAttributes().getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getQ0();
        if (q0 != oldValue) {
            updateResource(res -> res.getAttributes().setQ0(q0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            notifyUpdate("q0", variantId, oldValue, q0);
        }
        return this;
    }

    @Override
    public double getR() {
        return getResource().getAttributes().getR();
    }

    @Override
    public DanglingLine setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = getResource().getAttributes().getR();
        if (r != oldValue) {
            updateResource(res -> res.getAttributes().setR(r));
            notifyUpdate("r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getResource().getAttributes().getX();
    }

    @Override
    public DanglingLine setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = getResource().getAttributes().getX();
        if (x != oldValue) {
            updateResource(res -> res.getAttributes().setX(x));
            notifyUpdate("x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getG() {
        return getResource().getAttributes().getG();
    }

    @Override
    public DanglingLine setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = getResource().getAttributes().getG();
        if (g != oldValue) {
            updateResource(res -> res.getAttributes().setG(g));
            notifyUpdate("g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getB() {
        return getResource().getAttributes().getB();
    }

    @Override
    public DanglingLine setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = getResource().getAttributes().getB();
        if (b != oldValue) {
            updateResource(res -> res.getAttributes().setB(b));
            notifyUpdate("b", oldValue, b);
        }
        return this;
    }

    @Override
    public DanglingLine.Generation getGeneration() {
        var resource = getResource();
        if (resource.getAttributes().getGeneration() != null) {
            return new GenerationImpl(this);
        }
        return null;
    }

    @Override
    public String getUcteXnodeCode() {
        return getResource().getAttributes().getUcteXnodeCode();
    }

    public DanglingLine setUcteXnodeCode(String ucteXnodeCode) {
        String oldValue = getResource().getAttributes().getUcteXnodeCode();
        if (!Objects.equals(ucteXnodeCode, oldValue)) {
            updateResource(res -> res.getAttributes().setUcteXnodeCode(ucteXnodeCode));
            notifyUpdate("ucteXnodeCode", oldValue, ucteXnodeCode);
        }
        return this;
    }

    @Override
    public void setCurrentLimits(Void side, LimitsAttributes currentLimits) {
        var resource = getResource();
        LimitsAttributes oldValue = resource.getAttributes().getCurrentLimits();
        updateResource(res -> res.getAttributes().setCurrentLimits(currentLimits));
        notifyUpdate("currentLimits", oldValue, currentLimits);
    }

    @Override
    public AbstractIdentifiableImpl getIdentifiable() {
        return this;
    }

    @Override
    public CurrentLimits getNullableCurrentLimits() {
        var resource = getResource();
        return resource.getAttributes().getCurrentLimits() != null
                ? new CurrentLimitsImpl(this, resource.getAttributes().getCurrentLimits())
                : null;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return Optional.ofNullable(getNullableCurrentLimits());
    }

    @Override
    public ActivePowerLimits getNullableActivePowerLimits() {
        var resource = getResource();
        return resource.getAttributes().getActivePowerLimits() != null
                ? new ActivePowerLimitsImpl(this, resource.getAttributes().getActivePowerLimits())
                : null;
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return Optional.ofNullable(getNullableActivePowerLimits());
    }

    @Override
    public ApparentPowerLimits getNullableApparentPowerLimits() {
        var resource = getResource();
        return resource.getAttributes().getApparentPowerLimits() != null
                ? new ApparentPowerLimitsImpl(this, resource.getAttributes().getApparentPowerLimits())
                : null;
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return Optional.ofNullable(getNullableApparentPowerLimits());
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
        LimitsAttributes oldValue = getResource().getAttributes().getApparentPowerLimits();
        updateResource(res -> res.getAttributes().setApparentPowerLimits(apparentPowerLimitsAttributes));
        notifyUpdate("apparentPowerLimits", oldValue, apparentPowerLimitsAttributes);
    }

    @Override
    public void setActivePowerLimits(Void unused, LimitsAttributes activePowerLimitsAttributes) {
        LimitsAttributes oldValue = getResource().getAttributes().getActivePowerLimits();
        updateResource(res -> res.getAttributes().setActivePowerLimits(activePowerLimitsAttributes));
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
        var resource = getResource();
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
