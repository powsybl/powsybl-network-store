/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder, ShuntCompensatorModelOwner {

    private ShuntCompensatorModelAttributes model;

    private int sectionCount = -1;

    private Terminal regulatingTerminal;

    private boolean voltageRegulatorOn = false;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    class ShuntCompensatorLinearModelAdderImpl<O extends ShuntCompensatorModelOwner> implements ShuntCompensatorLinearModelAdder {

        private final O owner;

        private double bPerSection = Double.NaN;

        private double gPerSection = Double.NaN;

        private int maximumSectionCount = -1;

        ShuntCompensatorLinearModelAdderImpl(O owner) {
            this.owner = owner;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setBPerSection(double bPerSection) {
            this.bPerSection = bPerSection;
            return this;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setGPerSection(double gPerSection) {
            this.gPerSection = gPerSection;
            return this;
        }

        @Override
        public ShuntCompensatorLinearModelAdder setMaximumSectionCount(int maximumSectionCount) {
            this.maximumSectionCount = maximumSectionCount;
            return this;
        }

        @Override
        public ShuntCompensatorAdder add() {
            ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, bPerSection);
            ValidationUtil.checkMaximumSectionCount(ShuntCompensatorAdderImpl.this, maximumSectionCount);

            ShuntCompensatorLinearModelAttributes attributes = ShuntCompensatorLinearModelAttributes.builder()
                    .bPerSection(bPerSection)
                    .gPerSection(gPerSection)
                    .maximumSectionCount(maximumSectionCount)
                    .build();
            owner.setModel(attributes);
            return ShuntCompensatorAdderImpl.this;
        }
    }

    class ShuntCompensatorNonLinearModelAdderImpl<O extends ShuntCompensatorModelOwner> implements ShuntCompensatorNonLinearModelAdder {

        private final O owner;

        private final List<ShuntCompensatorNonLinearSectionAttributes> sections = new ArrayList<>();

        class SectionAdderImpl implements SectionAdder {

            private double b = Double.NaN;

            private double g = Double.NaN;

            @Override
            public SectionAdder setB(double b) {
                this.b = b;
                return this;
            }

            @Override
            public SectionAdder setG(double g) {
                this.g = g;
                return this;
            }

            @Override
            public ShuntCompensatorNonLinearModelAdder endSection() {
                ValidationUtil.checkBPerSection(ShuntCompensatorAdderImpl.this, b);
                if (Double.isNaN(g)) {
                    if (sections.isEmpty()) {
                        g = 0;
                    } else {
                        g = sections.get(sections.size() - 1).getG();
                    }
                }

                ShuntCompensatorNonLinearSectionAttributes shuntCompensatorNonLinearSectionAttributes = ShuntCompensatorNonLinearSectionAttributes.builder()
                                .b(b)
                                .g(g)
                                .build();

                sections.add(shuntCompensatorNonLinearSectionAttributes);
                return ShuntCompensatorNonLinearModelAdderImpl.this;
            }
        }

        ShuntCompensatorNonLinearModelAdderImpl(O owner) {
            this.owner = owner;
        }

        @Override
        public SectionAdder beginSection() {
            return new SectionAdderImpl();
        }

        @Override
        public ShuntCompensatorAdder add() {
            if (sections.isEmpty()) {
                throw new ValidationException(ShuntCompensatorAdderImpl.this, "a shunt compensator must have at least one section");
            }

            ShuntCompensatorNonLinearModelAttributes attributes = ShuntCompensatorNonLinearModelAttributes.builder()
                    .sections(sections)
                    .build();
            owner.setModel(attributes);
            return ShuntCompensatorAdderImpl.this;
        }
    }

    ShuntCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public ShuntCompensatorLinearModelAdder newLinearModel() {
        return new ShuntCompensatorLinearModelAdderImpl(this);
    }

    @Override
    public ShuntCompensatorNonLinearModelAdder newNonLinearModel() {
        return new ShuntCompensatorNonLinearModelAdderImpl(this);
    }

    @Override
    public ShuntCompensatorAdder setSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public void setModel(ShuntCompensatorModelAttributes model) {
        this.model = model;
    }

    @Override
    public ShuntCompensator add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        if (model == null) {
            throw new ValidationException(this, "the shunt compensator model has not been defined");
        }
        ValidationUtil.checkSections(this, sectionCount, model.getMaximumSectionCount(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        if (sectionCount < 0 || sectionCount > model.getMaximumSectionCount()) {
            throw new ValidationException(this, "unexpected section number (" + sectionCount + "): no existing associated section");
        }

        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        RegulationPointAttributes regulationPointAttributes = new RegulationPointAttributes(getId(), IdentifiableType.HVDC_CONVERTER_STATION,
            new TerminalRefAttributes(getId(), null), terminalRefAttributes, null);

        Resource<ShuntCompensatorAttributes> resource = Resource.shuntCompensatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .sectionCount(sectionCount)
                        .model(model)
                        .regulationPoint(regulationPointAttributes)
                        .voltageRegulatorOn(voltageRegulatorOn)
                        .targetV(targetV)
                        .targetDeadband(targetDeadband)
                        .build())
                .build();
        ShuntCompensatorImpl shuntCompensator = getIndex().createShuntCompensator(resource);
        shuntCompensator.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        shuntCompensator.setRegulatingTerminal(regulatingTerminal);
        return shuntCompensator;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.SHUNT_COMPENSATOR.getDescription();
    }
}
