/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorModel;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorLinearModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearModelAttributes;
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
    public ConnectableType getType() {
        return ConnectableType.SHUNT_COMPENSATOR;
    }

    @Override
    public int getSectionCount() {
        return resource.getAttributes().getSectionCount();
    }

    @Override
    public ShuntCompensator setSectionCount(int sectionCount) {
        int oldValue = resource.getAttributes().getSectionCount();
        resource.getAttributes().setSectionCount(sectionCount);
        index.notifyUpdate(this, "sectionCount", oldValue, sectionCount);
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return resource.getAttributes().getModel().getMaximumSectionCount();
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
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        if (sectionCount < 0 || sectionCount > getMaximumSectionCount()) {
            throw new PowsyblException("the given count of sections (" + sectionCount + ") is invalid (negative or strictly greater than the number of sections");
        }
        return resource.getAttributes().getModel().getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return resource.getAttributes().getModel().getType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        ShuntCompensatorModelAttributes shuntCompensatorModelAttributes = resource.getAttributes().getModel();
        if (shuntCompensatorModelAttributes.getType() == ShuntCompensatorModelType.LINEAR) {
            return new ShuntCompensatorLinearModelImpl(this, (ShuntCompensatorLinearModelAttributes) shuntCompensatorModelAttributes);
        } else {
            return new ShuntCompensatorNonLinearModelImpl((ShuntCompensatorNonLinearModelAttributes) shuntCompensatorModelAttributes);
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
            throw new PowsyblException("incorrect shunt compensator model type "
                    + type.getName() + ", expected " + shuntCompensatorModel.getClass());
        }
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        boolean oldValue = resource.getAttributes().isVoltageRegulatorOn();
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        index.notifyUpdate(this, "voltageRegulatorOn", oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        TerminalRefAttributes oldValue = resource.getAttributes().getRegulatingTerminal();
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        index.notifyUpdate(this, "regulatingTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return resource.getAttributes().getTargetV();
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        double oldValue = resource.getAttributes().getTargetV();
        resource.getAttributes().setTargetV(targetV);
        index.notifyUpdate(this, "targetV", oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return resource.getAttributes().getTargetDeadband();
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        double oldValue = resource.getAttributes().getTargetDeadband();
        resource.getAttributes().setTargetDeadband(targetDeadband);
        index.notifyUpdate(this, "targetDeadband", oldValue, targetDeadband);
        return this;
    }

    protected String getTypeDescription() {
        return "Shunt compensator";
    }

    @Override
    public void remove() {
        index.removeShuntCompensator(resource.getId());
    }
}
