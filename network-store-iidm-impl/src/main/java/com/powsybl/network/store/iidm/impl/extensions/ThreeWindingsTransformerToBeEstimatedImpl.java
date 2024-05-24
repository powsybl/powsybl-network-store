/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import com.powsybl.network.store.model.ThreeWindingsTransformerToBeEstimatedAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ThreeWindingsTransformerToBeEstimatedImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerToBeEstimated {

    public ThreeWindingsTransformerToBeEstimatedImpl(ThreeWindingsTransformer threeWindingsTransformer) {
        super(threeWindingsTransformer);
    }

    private ThreeWindingsTransformerImpl getThreeWindingsTransformer() {
        return (ThreeWindingsTransformerImpl) getExtendable();
    }

    private ThreeWindingsTransformerToBeEstimatedAttributes getThreeWindingsTransformerAttributes() {
        return (ThreeWindingsTransformerToBeEstimatedAttributes) getThreeWindingsTransformer().getResource().getAttributes().getExtensionAttributes().get(ThreeWindingsTransformerToBeEstimated.NAME);
    }

    @Override
    public boolean shouldEstimateRatioTapChanger1() {
        return getThreeWindingsTransformerAttributes().isRtc1Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger1(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc1Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger2() {
        return getThreeWindingsTransformerAttributes().isRtc2Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger2(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc2Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger3() {
        return getThreeWindingsTransformerAttributes().isRtc3Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger3(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc3Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimateRatioTapChanger(ThreeSides side) {
        return switch (side) {
            case ONE -> getThreeWindingsTransformerAttributes().isRtc1Status();
            case TWO -> getThreeWindingsTransformerAttributes().isRtc2Status();
            case THREE -> getThreeWindingsTransformerAttributes().isRtc3Status();
        };
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated, ThreeSides side) {
        switch (side) {
            case ONE ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc1Status(toBeEstimated));
            case TWO ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc2Status(toBeEstimated));
            case THREE ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setRtc3Status(toBeEstimated));
        }
        return this;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger1() {
        return getThreeWindingsTransformerAttributes().isPtc1Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger1(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc1Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger2() {
        return getThreeWindingsTransformerAttributes().isPtc2Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger2(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc2Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger3() {
        return getThreeWindingsTransformerAttributes().isPtc3Status();
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger3(boolean toBeEstimated) {
        getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc3Status(toBeEstimated));
        return this;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger(ThreeSides side) {
        return switch (side) {
            case ONE -> getThreeWindingsTransformerAttributes().isPtc1Status();
            case TWO -> getThreeWindingsTransformerAttributes().isPtc2Status();
            case THREE -> getThreeWindingsTransformerAttributes().isPtc3Status();
        };
    }

    @Override
    public ThreeWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated, ThreeSides side) {
        switch (side) {
            case ONE ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc1Status(toBeEstimated));
            case TWO ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc2Status(toBeEstimated));
            case THREE ->
                getThreeWindingsTransformer().updateResource(res -> getThreeWindingsTransformerAttributes().setPtc3Status(toBeEstimated));
        }
        return this;
    }
}
