/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

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
        return checkResource().getAttributes().getSectionCount();
    }

    @Override
    public ShuntCompensator setSectionCount(int sectionCount) {
        var resource = checkResource();
        ValidationUtil.checkSections(this, sectionCount, getMaximumSectionCount());
        int oldValue = resource.getAttributes().getSectionCount();
        resource.getAttributes().setSectionCount(sectionCount);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "sectionCount", variantId, oldValue, sectionCount);
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return checkResource().getAttributes().getModel().getMaximumSectionCount();
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
        var resource = checkResource();
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        var resource = checkResource();
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return checkResource().getAttributes().getModel().getType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        var resource = checkResource();
        ShuntCompensatorModelAttributes shuntCompensatorModelAttributes = resource.getAttributes().getModel();
        if (shuntCompensatorModelAttributes.getType() == ShuntCompensatorModelType.LINEAR) {
            return new ShuntCompensatorLinearModelImpl(this, (ShuntCompensatorLinearModelAttributes) shuntCompensatorModelAttributes);
        } else {
            return new ShuntCompensatorNonLinearModelImpl(this, (ShuntCompensatorNonLinearModelAttributes) shuntCompensatorModelAttributes);
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
        return checkResource().getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        var resource = checkResource();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getTargetV());
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, getTargetDeadband());
        boolean oldValue = resource.getAttributes().isVoltageRegulatorOn();
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
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
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        var resource = checkResource();
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes oldValue = resource.getAttributes().getRegulatingTerminal();
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        updateResource();
        index.notifyUpdate(this, "regulatingTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return checkResource().getAttributes().getTargetV();
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        var resource = checkResource();
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV);
        double oldValue = resource.getAttributes().getTargetV();
        resource.getAttributes().setTargetV(targetV);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return checkResource().getAttributes().getTargetDeadband();
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        var resource = checkResource();
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", isVoltageRegulatorOn(), targetDeadband);
        double oldValue = resource.getAttributes().getTargetDeadband();
        resource.getAttributes().setTargetDeadband(targetDeadband);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetDeadband", variantId, oldValue, targetDeadband);
        return this;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        // TODO
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeShuntCompensator(resource.getId());
        getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        index.notifyAfterRemoval(resource.getId());
    }
}
