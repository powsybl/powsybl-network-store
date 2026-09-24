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
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorModelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class ShuntCompensatorImpl extends AbstractRegulatingInjection<ShuntCompensator, ShuntCompensatorAttributes> implements ShuntCompensator {

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
    protected Class<? extends VoltageRegulationHolder<?>> getVoltageRegulationHolderClass() {
        return ShuntCompensator.class;
    }

    @Override
    public int getSectionCount() {
        return getResource().getAttributes().getSectionCount();
    }

    @Override
    public Integer getSolvedSectionCount() {
        return getResource().getAttributes().getSolvedSectionCount();
    }

    @Override
    public ShuntCompensator setSectionCount(int sectionCount) {
        ValidationUtil.checkSections(this, sectionCount, getMaximumSectionCount(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        int oldValue = getResource().getAttributes().getSectionCount();
        if (sectionCount != oldValue) {
            updateResource(res -> res.getAttributes().setSectionCount(sectionCount),
                "sectionCount", oldValue, sectionCount);
        }
        return this;
    }

    @Override
    public ShuntCompensator setSolvedSectionCount(int solvedSectionCount) {
        Integer oldValue = getResource().getAttributes().getSolvedSectionCount();
        checkSolvedSectionCount(solvedSectionCount, getMaximumSectionCount());
        updateResource(res -> res.getAttributes().setSolvedSectionCount(solvedSectionCount), AttributeFilter.SV,
            "solvedSectionCount", oldValue, solvedSectionCount);
        return this;
    }

    @Override
    public ShuntCompensator unsetSolvedSectionCount() {
        Integer oldValue = getResource().getAttributes().getSolvedSectionCount();
        updateResource(res -> res.getAttributes().setSolvedSectionCount(null), AttributeFilter.SV,
            "solvedSectionCount", oldValue, null);
        return this;
    }

    private void checkSolvedSectionCount(Integer solvedSectionCount, int maximumSectionCount) {
        if (solvedSectionCount != null && (solvedSectionCount < 0 || solvedSectionCount > maximumSectionCount)) {
            throw new ValidationException(this, "unexpected solved section number (" + solvedSectionCount + "): no existing associated section");
        }
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
        return this.isRegulatingWithMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE);
    }

    @Override
    public double getTargetV() {
        return getRegulatingTargetV();
    }

    @Override
    public double getLocalTargetV() {
        double value = getResource().getAttributes().getLocalTargetV();
        return Double.isNaN(value) ? getResource().getAttributes().getTargetV() : value;
    }

    @Override
    public ShuntCompensator setLocalTargetV(double targetV) {
        ValidationUtil.checkLocalTargetQandV(this, ShuntCompensator.class, targetV, Double.NaN,
            getVoltageRegulation(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getLocalTargetV();
        if (Double.compare(targetV, oldValue) != 0) {
            updateResource(resource -> {
                resource.getAttributes().setLocalTargetV(targetV);
                resource.getAttributes().setTargetV(targetV);
            }, "localTargetV", oldValue, targetV);
        }
        return this;
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV,
            getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        if (isRemoteRegulating() && getVoltageRegulation() != null
            && getVoltageRegulation().getMode() == com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE) {
            getVoltageRegulation().setTargetValue(targetV);
        } else {
            setLocalTargetV(targetV);
        }
        return this;
    }

    @Override
    public double getTargetDeadband() {
        VoltageRegulation regulation = getVoltageRegulation();
        return regulation != null ? regulation.getTargetDeadband() : Double.NaN;
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        VoltageRegulation regulation = getVoltageRegulation();
        if (regulation == null) {
            newVoltageRegulation().withMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE)
                .withRegulating(false).withTargetDeadband(targetDeadband).build();
        } else {
            regulation.setTargetDeadband(targetDeadband);
        }
        return this;
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        if (getVoltageRegulation() == null) {
            newVoltageRegulation().withMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE)
                .withRegulating(voltageRegulatorOn).withTargetDeadband(getResource().getAttributes().getTargetDeadband()).build();
        } else {
            getVoltageRegulation().setMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE);
            getVoltageRegulation().setRegulating(voltageRegulatorOn);
        }
        return this;
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        if (getNetwork().getVariantManager().getVariantIds().size() > 1) {
            throw new com.powsybl.commons.PowsyblException(getMessageHeader() + "Cannot set terminal when there are multiple variants");
        }
        if (getVoltageRegulation() == null) {
            newVoltageRegulation().withMode(com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE)
                .withRegulating(false).withTerminal(regulatingTerminal).withTargetValue(getLocalTargetV()).build();
        } else {
            getVoltageRegulation().setTerminal(regulatingTerminal, regulatingTerminal == null ? Double.NaN : getVoltageRegulation().getTargetValue());
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
        index.removeShuntCompensator(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public <E extends Extension<ShuntCompensator>> boolean removeExtension(Class<E> type) {
        super.removeExtension(type);
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
