/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Attributes;
import com.powsybl.network.store.model.RatioTapChangerAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerParentAttributes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl extends AbstractTapChanger<TapChangerParent, RatioTapChangerImpl, RatioTapChangerAttributes> implements RatioTapChanger, Validable {

    private final Function<Attributes, TapChangerParentAttributes> attributesGetter;

    public RatioTapChangerImpl(TapChangerParent parent, NetworkObjectIndex index, Function<Attributes, TapChangerParentAttributes> attributesGetter) {
        super(parent, index, "ratio tap changer");
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
    }

    @Override
    protected RatioTapChangerAttributes getAttributes() {
        return getAttributes(getResource());
    }

    protected RatioTapChangerAttributes getAttributes(Resource<?> resource) {
        return attributesGetter.apply(resource.getAttributes()).getRatioTapChangerAttributes();
    }

    @Override
    public double getTargetV() {
        return getAttributes().getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), targetV, parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getAttributes().getTargetV();
        if (Double.compare(targetV, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setTargetV(targetV));
            notifyUpdate(() -> getTapChangerAttribute() + ".targetV", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetV);
        }
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return getAttributes().isLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), status, getRegulationTerminal(), getTargetV(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        boolean oldValue = getAttributes().isLoadTapChangingCapabilities();
        if (status != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setLoadTapChangingCapabilities(status));
            notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, status);
        }
        return this;
    }

    @Override
    public int getHighTapPosition() {
        var attributes = getAttributes();
        return attributes.getLowTapPosition() + attributes.getSteps().size() - 1;
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, hasLoadTapChangingCapabilities(), getRegulationTerminal(), getTargetV(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);

        Set<TapChanger<?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating, true);

        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), hasLoadTapChangingCapabilities(), regulationTerminal, getTargetV(), parent.getNetwork(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public int getStepCount() {
        return getAttributes().getSteps().size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        var attributes = getAttributes();
        int tapPositionIndex = tapPosition - attributes.getLowTapPosition();
        return new RatioTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        var attributes = getAttributes();
        int tapPositionIndex = attributes.getTapPosition() - attributes.getLowTapPosition();
        return new RatioTapChangerStepImpl(this, tapPositionIndex);
    }

    @Override
    public void remove() {
        parent.setRatioTapChanger(null);
    }

    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public String getMessageHeader() {
        return "ratioTapChanger '" + parent.getTransformer().getId() + "': ";
    }
}
