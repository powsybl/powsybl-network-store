/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;
import com.powsybl.network.store.iidm.impl.LineImpl;
import com.powsybl.network.store.model.LineFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LineFortescueAdderImpl extends AbstractIidmExtensionAdder<Line, LineFortescue> implements LineFortescueAdder {

    private double rz;
    private double xz;
    private boolean openPhaseA;
    private boolean openPhaseB;
    private boolean openPhaseC;

    public LineFortescueAdderImpl(Line extendable) {
        super(extendable);
    }

    @Override
    public Class<? super LineFortescue> getExtensionClass() {
        return LineFortescue.class;
    }

    @Override
    protected LineFortescue createExtension(Line line) {
        LineFortescueAttributes attributes = LineFortescueAttributes.builder()
            .rz(rz)
            .xz(xz)
            .openPhaseA(openPhaseA)
            .openPhaseB(openPhaseB)
            .openPhaseC(openPhaseC)
            .build();
        ((LineImpl) line).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(LineFortescue.NAME, attributes));
        return new LineFortescueImpl(line);
    }

    @Override
    public LineFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public LineFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }

    @Override
    public LineFortescueAdder withOpenPhaseA(boolean openPhaseA) {
        this.openPhaseA = openPhaseA;
        return this;
    }

    @Override
    public LineFortescueAdder withOpenPhaseB(boolean openPhaseB) {
        this.openPhaseB = openPhaseB;
        return this;
    }

    @Override
    public LineFortescueAdder withOpenPhaseC(boolean openPhaseC) {
        this.openPhaseC = openPhaseC;
        return this;
    }
}
