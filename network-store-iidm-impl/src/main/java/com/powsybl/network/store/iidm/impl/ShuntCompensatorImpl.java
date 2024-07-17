/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorModelAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class ShuntCompensatorImpl extends AbstractInjectionImpl<ShuntCompensator, ShuntCompensatorAttributes> implements ShuntCompensator {

    public ShuntCompensatorImpl(NetworkObjectIndex index, Resource<ShuntCompensatorAttributes> resource) {
        super(index, resource);
    }

    static ShuntCompensatorImpl create(NetworkObjectIndex index, Resource<ShuntCompensatorAttributes> resource) {
        return new ShuntCompensatorImpl(index, resource);
    }

    @Override
    protected ShuntCompensator getInjection() {
        return this;
    }

    @Override
    public int getSectionCount() {
        return getResource().getAttributes().getSectionCount();
    }

    @Override
    public ShuntCompensator setSectionCount(int sectionCount) {
        ValidationUtil.checkSections(this, sectionCount, getMaximumSectionCount(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        int oldValue = getResource().getAttributes().getSectionCount();
        if (sectionCount != oldValue) {
            updateResource(res -> res.getAttributes().setSectionCount(sectionCount));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "sectionCount", variantId, oldValue, sectionCount);
        }
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return getResource().getAttributes().getModel().getMaximumSectionCount();
    }

    @Override
    public double getB() {
        return getB(getSectionCount());
    }

    @Override
    public double getG() {
        return getG(getSectionCount());
    }

    @Override
    public double getB(int sectionCount) {
        var resource = getResource();
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        var resource = getResource();
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return getResource().getAttributes().getModel().getType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        var resource = getResource();
        ShuntCompensatorModelAttributes shuntCompensatorModelAttributes = resource.getAttributes().getModel();
        if (shuntCompensatorModelAttributes.getType() == ShuntCompensatorModelType.LINEAR) {
            return new ShuntCompensatorLinearModelImpl(this);
        } else {
            return new ShuntCompensatorNonLinearModelImpl(this);
        }
    }

    public void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> type) {
        ShuntCompensatorModel shuntCompensatorModel = getModel();
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(shuntCompensatorModel)) {
            return type.cast(shuntCompensatorModel);
        } else {
            throw new ValidationException(this, "incorrect shunt compensator model type " +
                    type.getName() + ", expected " + shuntCompensatorModel.getClass());
        }
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getResource().getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getTargetV(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, getTargetDeadband(), ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        boolean oldValue = getResource().getAttributes().isVoltageRegulatorOn();
        if (voltageRegulatorOn != oldValue) {
            updateResource(res -> res.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        }
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = getResource().getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes oldValue = getResource().getAttributes().getRegulatingTerminal();
        updateResource(res -> res.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        index.notifyUpdate(this, "regulatingTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return getResource().getAttributes().getTargetV();
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getTargetV();
        if (Double.compare(targetV, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setTargetV(targetV));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetV", variantId, oldValue, targetV);
        }
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getResource().getAttributes().getTargetDeadband();
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", isVoltageRegulatorOn(), targetDeadband, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getTargetDeadband();
        if (Double.compare(targetDeadband, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setTargetDeadband(targetDeadband));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "targetDeadband", variantId, oldValue, targetDeadband);
        }
        return this;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeShuntCompensator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}
