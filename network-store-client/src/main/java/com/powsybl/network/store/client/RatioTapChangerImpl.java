/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RatioTapChangerImpl implements RatioTapChanger {

    public RatioTapChangerImpl() {
    }

    static RatioTapChangerImpl create() {
        return new RatioTapChangerImpl();
    }

    @Override
    public double getTargetV() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setLoadTapChangingCapabilities(boolean status) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getLowTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setLowTapPosition(int lowTapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getHighTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getTapPosition() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setTapPosition(int tapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getStepCount() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isRegulating() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Terminal getRegulationTerminal() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }
}
