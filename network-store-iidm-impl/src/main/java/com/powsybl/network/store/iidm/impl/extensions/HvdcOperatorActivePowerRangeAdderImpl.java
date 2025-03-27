/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcOperatorActivePowerRangeAdderImpl extends AbstractIidmExtensionAdder<HvdcLine, HvdcOperatorActivePowerRange>
        implements HvdcOperatorActivePowerRangeAdder {

    private float oprFromCS1toCS2;

    private float oprFromCS2toCS1;

    public HvdcOperatorActivePowerRangeAdderImpl(HvdcLine hvdcLine) {
        super(hvdcLine);
    }

    @Override
    protected HvdcOperatorActivePowerRange createExtension(HvdcLine hvdcLine) {
        HvdcOperatorActivePowerRangeAttributes oldValue = ((HvdcLineImpl) hvdcLine).getResource().getAttributes().getHvdcOperatorActivePowerRange();
        HvdcOperatorActivePowerRangeAttributes attributes = HvdcOperatorActivePowerRangeAttributes.builder()
                        .oprFromCS1toCS2(oprFromCS1toCS2)
                        .oprFromCS2toCS1(oprFromCS2toCS1)
                        .build();
        ((HvdcLineImpl) hvdcLine).updateResource(res -> res.getAttributes().setHvdcOperatorActivePowerRange(attributes),
            "hvdcOperatorActivePowerRange", oldValue, attributes);
        return new HvdcOperatorActivePowerRangeImpl((HvdcLineImpl) hvdcLine);
    }

    @Override
    public HvdcOperatorActivePowerRangeAdder withOprFromCS1toCS2(float oprFromCS1toCS2) {
        this.oprFromCS1toCS2 = oprFromCS1toCS2;
        return this;
    }

    @Override
    public HvdcOperatorActivePowerRangeAdder withOprFromCS2toCS1(float oprFromCS2toCS1) {
        this.oprFromCS2toCS1 = oprFromCS2toCS1;
        return this;
    }
}
