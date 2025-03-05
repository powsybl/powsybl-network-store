/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.network.store.iidm.impl.HvdcLineImpl;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcOperatorActivePowerRangeImpl extends AbstractExtension<HvdcLine> implements HvdcOperatorActivePowerRange {

    public HvdcOperatorActivePowerRangeImpl(HvdcLineImpl hvdcLine) {
        super(Objects.requireNonNull(hvdcLine));
    }

    private HvdcLineImpl getHvdcLine() {
        return (HvdcLineImpl) getExtendable();
    }

    private HvdcOperatorActivePowerRangeAttributes getHvdcOperatorActivePowerRangeAttributes() {
        return getHvdcLine().getResource().getAttributes().getHvdcOperatorActivePowerRange();
    }

    @Override
    public float getOprFromCS1toCS2() {
        return getHvdcOperatorActivePowerRangeAttributes().getOprFromCS1toCS2();
    }

    @Override
    public float getOprFromCS2toCS1() {
        return getHvdcOperatorActivePowerRangeAttributes().getOprFromCS2toCS1();
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS1toCS2(float oprFromCS1toCS2) {
        float oldValue = getOprFromCS1toCS2();
        if (oldValue != oprFromCS1toCS2) {
            getHvdcLine().updateResourceExtension(this, res -> res.getAttributes().getHvdcOperatorActivePowerRange().setOprFromCS1toCS2(checkOPR(oprFromCS1toCS2, getHvdcLine().getConverterStation1(), getHvdcLine().getConverterStation2())), "oprFromCS1toCS2", oldValue, oprFromCS1toCS2);
        }
        return this;
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS2toCS1(float oprFromCS2toCS1) {
        float oldValue = getOprFromCS2toCS1();
        if (oldValue != oprFromCS2toCS1) {
            getHvdcLine().updateResourceExtension(this, res -> res.getAttributes().getHvdcOperatorActivePowerRange().setOprFromCS2toCS1(checkOPR(oprFromCS2toCS1, getHvdcLine().getConverterStation1(), getHvdcLine().getConverterStation2())), "oprFromCS2toCS1", oldValue, oprFromCS2toCS1);
        }
        return this;
    }

    private float checkOPR(float opr, HvdcConverterStation<?> from, HvdcConverterStation<?> to) {
        if (!Float.isNaN(opr) && opr < 0) {
            String message = "OPR from " + from.getId() + " to " + to.getId() + " must be greater than 0 (current value " + opr + ").";
            throw new IllegalArgumentException(message);
        }
        return opr;
    }
}
