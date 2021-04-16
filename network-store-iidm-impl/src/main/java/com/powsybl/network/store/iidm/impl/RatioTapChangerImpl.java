/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.RatioTapChangerAttributes;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes> implements RatioTapChanger, Validable {

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, RatioTapChangerAttributes attributes) {
        super(parent, index, attributes, "ratio tap changer");
    }

    @Override
    public double getTargetV() {
        return attributes.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), targetV, parent.getNetwork());
        double oldValue = attributes.getTargetV();
        attributes.setTargetV(targetV);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".targetV", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetV);
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return attributes.isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), status, getRegulationTerminal(), getTargetV(), parent.getNetwork());
        boolean oldValue = attributes.isLoadTapChangingCapabilities();
        attributes.setLoadTapChangingCapabilities(status);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, status);
        return this;
    }

    @Override
    public int getHighTapPosition() {
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, hasLoadTapChangingCapabilities(), getRegulationTerminal(), getTargetV(), parent.getNetwork());

        Set<TapChanger> tapChangers = new HashSet<>();
        tapChangers.addAll(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating);

        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), regulationTerminal, getTargetV(), parent.getNetwork());
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public int getStepCount() {
        return attributes.getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return new RatioTapChangerStepImpl(this, attributes.getSteps().get(tapPosition - attributes.getLowTapPosition()));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new RatioTapChangerStepImpl(this, attributes.getSteps().get(attributes.getTapPosition() - attributes.getLowTapPosition()));
    }

    @Override
    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public String getMessageHeader() {
        return "ratioTapChanger '" + parent.getTransformer().getId() + "': ";
    }
}
