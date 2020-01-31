/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public abstract class AbstractTapChanger {

    protected int lowTapPosition = 0;

    protected Integer tapPosition;

    protected boolean regulating = false;

    protected double targetDeadband = Double.NaN;

    public AbstractTapChanger() {

    }

    public AbstractTapChanger(int lowTapPosition, Integer tapPosition, boolean regulating, double targetDeadband) {
        this.lowTapPosition = lowTapPosition;
        this.tapPosition = tapPosition;
        this.regulating = regulating;
        this.targetDeadband = targetDeadband;
    }
}
