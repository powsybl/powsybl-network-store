/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.AbstractRegulatingEquipmentAttributes;
import com.powsybl.network.store.model.Attributes;
import com.powsybl.network.store.model.RegulatingPointAttributes;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class TapChangerRegulatingPoint extends AbstractRegulatingPoint {
    private final AbstractTapChanger tapChanger;

    public TapChangerRegulatingPoint(NetworkObjectIndex index, AbstractTapChanger tapChanger,
                                     Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        super(index, attributesGetter);
        this.tapChanger = tapChanger;
    }

    @Override
    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(tapChanger.getAttributes()).getRegulatingPoint();
    }

    @Override
    protected AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return tapChanger.getTransformer();
    }

    @Override
    protected void resetRegulatingAndRegulationMode(Terminal regulatingTerminal, Terminal localTerminal, ReportNode reportNode) {
        // no reset needed on regulation mode
        setRegulating("regulating", false);
    }
}
