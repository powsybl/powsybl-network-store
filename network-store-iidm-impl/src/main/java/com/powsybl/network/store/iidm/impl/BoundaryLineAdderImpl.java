/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BoundaryLineAdderImpl extends AbstractInjectionAdder<BoundaryLineAdderImpl> implements BoundaryLineAdder {

    class GenerationAdderImpl implements GenerationAdder {

        private double minP = -Double.MAX_VALUE;

        private double maxP = Double.MAX_VALUE;

        private double targetP = Double.NaN;

        private double targetQ = Double.NaN;

        private double targetV = Double.NaN;

        private boolean voltageRegulationOn = false;

        @Override
        public GenerationAdder setTargetP(double targetP) {
            this.targetP = targetP;
            return this;
        }

        @Override
        public GenerationAdder setMaxP(double maxP) {
            this.maxP = maxP;
            return this;
        }

        @Override
        public GenerationAdder setMinP(double minP) {
            this.minP = minP;
            return this;
        }

        @Override
        public GenerationAdder setTargetQ(double targetQ) {
            this.targetQ = targetQ;
            return this;
        }

        @Override
        public GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn) {
            this.voltageRegulationOn = voltageRegulationOn;
            return this;
        }

        @Override
        public GenerationAdder setTargetV(double targetV) {
            this.targetV = targetV;
            return this;
        }

        @Override
        public BoundaryLineAdder add() {
            ValidationUtil.checkActivePowerLimits(BoundaryLineAdderImpl.this, minP, maxP);
            ValidationUtil.checkActivePowerSetpoint(BoundaryLineAdderImpl.this, targetP, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
            ValidationUtil.checkVoltageControl(BoundaryLineAdderImpl.this, voltageRegulationOn, targetV, targetQ, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());

            generation = BoundaryLineGenerationAttributes
                    .builder()
                    .minP(minP)
                    .maxP(maxP)
                    .targetP(targetP)
                    .targetQ(targetQ)
                    .targetV(targetV)
                    .voltageRegulationOn(voltageRegulationOn)
                    .build();
            return BoundaryLineAdderImpl.this;
        }
    }

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = 0;

    private double b = 0;

    private String pairingKey = null;

    private BoundaryLineGenerationAttributes generation;

    BoundaryLineAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public BoundaryLineAdder setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public BoundaryLineAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public BoundaryLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public BoundaryLineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public BoundaryLineAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public BoundaryLineAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public BoundaryLineAdder setPairingKey(String pairingKey) {
        this.pairingKey = pairingKey;
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        return new GenerationAdderImpl();
    }

    @Override
    public BoundaryLine add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();

        ValidationUtil.checkP0(this, p0, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkQ0(this, q0, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        Resource<BoundaryLineAttributes> resource = Resource.boundaryLineBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(BoundaryLineAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .p0(p0)
                        .q0(q0)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .generation(generation)
                        .pairingKey(pairingKey)
                        .build())
                .build();
        BoundaryLineImpl boundaryLine = getIndex().createBoundaryLine(resource);
        boundaryLine.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return boundaryLine;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.BOUNDARY_LINE.getDescription();
    }
}
