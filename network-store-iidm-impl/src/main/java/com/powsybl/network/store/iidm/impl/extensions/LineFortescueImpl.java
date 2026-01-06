/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.network.store.iidm.impl.LineImpl;
import com.powsybl.network.store.model.LineFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LineFortescueImpl extends AbstractExtension<Line> implements LineFortescue {
    public LineFortescueImpl(Line line) {
        super(line);
    }

    private LineImpl getLine() {
        return (LineImpl) getExtendable();
    }

    private LineFortescueAttributes getLineFortescueAttributes() {
        return (LineFortescueAttributes) getLine().getResource().getAttributes().getExtensionAttributes().get(LineFortescue.NAME);
    }

    @Override
    public double getRz() {
        return getLineFortescueAttributes().getRz();
    }

    @Override
    public void setRz(double rz) {
        double oldValue = getRz();
        if (oldValue != rz) {
            getLine().updateResourceExtension(this, res ->
                getLineFortescueAttributes().setRz(rz), "rz", oldValue, rz);
        }
    }

    @Override
    public double getXz() {
        return getLineFortescueAttributes().getXz();
    }

    @Override
    public void setXz(double xz) {
        double oldValue = getXz();
        if (oldValue != xz) {
            getLine().updateResourceExtension(this, res ->
                getLineFortescueAttributes().setXz(xz), "xz", oldValue, xz);
        }
    }

    @Override
    public boolean isOpenPhaseA() {
        return getLineFortescueAttributes().isOpenPhaseA();
    }

    @Override
    public void setOpenPhaseA(boolean openPhaseA) {
        boolean oldValue = isOpenPhaseA();
        if (oldValue != openPhaseA) {
            getLine().updateResourceExtension(this, res ->
                getLineFortescueAttributes().setOpenPhaseA(openPhaseA), "openPhaseA", oldValue, openPhaseA);
        }
    }

    @Override
    public boolean isOpenPhaseB() {
        return getLineFortescueAttributes().isOpenPhaseB();
    }

    @Override
    public void setOpenPhaseB(boolean openPhaseB) {
        boolean oldValue = isOpenPhaseB();
        if (oldValue != openPhaseB) {
            getLine().updateResourceExtension(this, res ->
                getLineFortescueAttributes().setOpenPhaseB(openPhaseB), "openPhaseB", oldValue, openPhaseB);
        }
    }

    @Override
    public boolean isOpenPhaseC() {
        return getLineFortescueAttributes().isOpenPhaseC();
    }

    @Override
    public void setOpenPhaseC(boolean openPhaseC) {
        boolean oldValue = isOpenPhaseC();
        if (oldValue != openPhaseC) {
            getLine().updateResourceExtension(this, res ->
                getLineFortescueAttributes().setOpenPhaseC(openPhaseC), "openPhaseC", oldValue, openPhaseC);
        }
    }
}
