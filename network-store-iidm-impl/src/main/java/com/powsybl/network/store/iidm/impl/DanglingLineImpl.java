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
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.Validable;
import com.powsybl.network.store.model.CurrentLimitsAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.DanglingLineGenerationAttributes;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import com.powsybl.network.store.model.ReactiveCapabilityCurveAttributes;
import com.powsybl.network.store.model.ReactiveLimitsAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DanglingLineImpl extends AbstractInjectionImpl<DanglingLine, DanglingLineAttributes> implements DanglingLine, CurrentLimitsOwner<Void> {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private final DanglingLineImpl danglingLine;

        private final DanglingLineGenerationAttributes attributes;

        private final String id;

        GenerationImpl(DanglingLineImpl danglingLine, DanglingLineGenerationAttributes attributes, String id) {
            this.danglingLine = Objects.requireNonNull(danglingLine);
            this.attributes = attributes;
            if (this.attributes.getReactiveLimits() == null) {
                this.attributes.setReactiveLimits(new MinMaxReactiveLimitsAttributes(-Double.MAX_VALUE, Double.MAX_VALUE));
            }
            this.id = id;
        }

        @Override
        public double getTargetP() {
            return attributes.getTargetP();
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            double oldValue = attributes.getTargetP();
            attributes.setTargetP(targetP);
            danglingLine.notifyUpdate("targetP", oldValue, targetP);
            return this;
        }

        @Override
        public double getMaxP() {
            return attributes.getMaxP();
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            double oldValue = attributes.getMaxP();
            attributes.setMaxP(maxP);
            danglingLine.notifyUpdate("maxP", oldValue, maxP);
            return this;
        }

        @Override
        public double getMinP() {
            return attributes.getMinP();
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            double oldValue = attributes.getMinP();
            attributes.setMinP(minP);
            danglingLine.notifyUpdate("minP", oldValue, minP);
            return this;
        }

        @Override
        public double getTargetQ() {
            return attributes.getTargetQ();
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            double oldValue = attributes.getTargetQ();
            attributes.setTargetQ(targetQ);
            danglingLine.notifyUpdate("targetQ", oldValue, targetQ);
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return attributes.isVoltageRegulationOn();
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            boolean oldValue = attributes.isVoltageRegulationOn();
            attributes.setVoltageRegulationOn(voltageRegulationOn);
            danglingLine.notifyUpdate("voltageRegulationOn", oldValue, voltageRegulationOn);
            return this;
        }

        @Override
        public double getTargetV() {
            return attributes.getTargetV();
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            double oldValue = attributes.getTargetV();
            attributes.setTargetV(targetV);
            danglingLine.notifyUpdate("targetV", oldValue, targetV);
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
            attributes.setReactiveLimits(reactiveLimits);
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
                throw new PowsyblException("incorrect reactive limits type " + type.getName() + ", expected " + reactiveLimits.getClass());
            }
        }

        @Override
        public String getMessageHeader() {
            return "generation part for dangling line '" + id + "': ";
        }
    }

    public DanglingLineImpl(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        super(index, resource);
    }

    static DanglingLineImpl create(NetworkObjectIndex index, Resource<DanglingLineAttributes> resource) {
        return new DanglingLineImpl(index, resource);
    }

    public void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.DANGLING_LINE;
    }

    @Override
    public void remove() {
        index.removeDanglingLine(resource.getId());
    }

    @Override
    protected DanglingLine getInjection() {
        return this;
    }

    @Override
    public double getP0() {
        return resource.getAttributes().getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        double oldValue = resource.getAttributes().getP0();
        resource.getAttributes().setP0(p0);
        notifyUpdate("p0", oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return resource.getAttributes().getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        double oldValue = resource.getAttributes().getQ0();
        resource.getAttributes().setQ0(q0);
        notifyUpdate("q0", oldValue, q0);
        return this;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public DanglingLine setR(double r) {
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public DanglingLine setX(double x) {
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return resource.getAttributes().getG();
    }

    @Override
    public DanglingLine setG(double g) {
        double oldValue = resource.getAttributes().getG();
        resource.getAttributes().setG(g);
        notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return resource.getAttributes().getB();
    }

    @Override
    public DanglingLine setB(double b) {
        double oldValue = resource.getAttributes().getB();
        resource.getAttributes().setB(b);
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public DanglingLine.Generation getGeneration() {
        if (resource.getAttributes().getGeneration() != null) {
            return new GenerationImpl(this, resource.getAttributes().getGeneration(), getId());
        } else {
            return null;
        }
    }

    @Override
    public String getUcteXnodeCode() {
        return resource.getAttributes().getUcteXnodeCode();
    }

    public DanglingLine setUcteXnodeCode(String ucteXnodeCode) {
        String oldValue = resource.getAttributes().getUcteXnodeCode();
        resource.getAttributes().setUcteXnodeCode(ucteXnodeCode);
        notifyUpdate("ucteXnodeCode", oldValue, ucteXnodeCode);
        return this;
    }

    @Override
    public void setCurrentLimits(Void side, CurrentLimitsAttributes currentLimits) {
        CurrentLimitsAttributes oldValue = resource.getAttributes().getCurrentLimits();
        resource.getAttributes().setCurrentLimits(currentLimits);
        notifyUpdate("currentLimits", oldValue, currentLimits);
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return resource.getAttributes().getCurrentLimits() != null
                ? new CurrentLimitsImpl(resource.getAttributes().getCurrentLimits())
                : null;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl<>(null, this);
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
    protected String getTypeDescription() {
        return "Dangling line";
    }
}
