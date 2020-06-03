/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    public static int getSide(Terminal regulatingTerminal) {
        String side = null;
        if (regulatingTerminal.getConnectable().getTerminals().size() > 1) {
            if (regulatingTerminal.getConnectable() instanceof Branch) {
                Branch branch = (Branch) regulatingTerminal.getConnectable();
                side = branch.getSide(regulatingTerminal).name();
            } else if (regulatingTerminal.getConnectable() instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) regulatingTerminal.getConnectable();
                side = twt.getSide(regulatingTerminal).name();
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + regulatingTerminal.getConnectable().getClass());
            }
        }
        return side == null ? 0 : ThreeWindingsTransformer.Side.valueOf(side).ordinal();
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getTerminalRef();
        Identifiable identifiable = index.getIdentifiable(terminalRefAttributes.getIdEquipment());
        String side = ThreeWindingsTransformer.Side.values()[terminalRefAttributes.getSide()].name();

        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                    : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        resource.getAttributes().setTerminalRef(TerminalRefAttributes.builder()
                .idEquipment(regulatingTerminal.getConnectable().getId())
                .side(getSide(regulatingTerminal))
                .build());
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
    protected String getTypeDescription() {
        return "Generator";
    }
}
