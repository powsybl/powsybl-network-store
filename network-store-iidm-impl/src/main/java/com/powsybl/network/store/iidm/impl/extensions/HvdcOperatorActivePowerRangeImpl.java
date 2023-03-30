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

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class HvdcOperatorActivePowerRangeImpl extends AbstractExtension<HvdcLine> implements HvdcOperatorActivePowerRange {

    private HvdcLineImpl hvdcLine;

    public HvdcOperatorActivePowerRangeImpl(HvdcLineImpl hvdcLine) {
        this.hvdcLine = Objects.requireNonNull(hvdcLine);
    }

    @Override
    public HvdcLine getExtendable() {
        return hvdcLine;
    }

    @Override
    public void setExtendable(HvdcLine hvdcLine) {
        this.hvdcLine = (HvdcLineImpl) hvdcLine;
    }

    @Override
    public float getOprFromCS1toCS2() {
        return hvdcLine.getResource().getAttributes().getHvdcOperatorActivePowerRange().getOprFromCS1toCS2();
    }

    @Override
    public float getOprFromCS2toCS1() {
        return hvdcLine.getResource().getAttributes().getHvdcOperatorActivePowerRange().getOprFromCS2toCS1();
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS1toCS2(float oprFromCS1toCS2) {
        hvdcLine.updateResource(res -> res.getAttributes().getHvdcOperatorActivePowerRange().setOprFromCS1toCS2(checkOPR(oprFromCS1toCS2, getExtendable().getConverterStation1(), getExtendable().getConverterStation2())));
        return this;
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS2toCS1(float oprFromCS2toCS1) {
        hvdcLine.updateResource(res -> res.getAttributes().getHvdcOperatorActivePowerRange().setOprFromCS2toCS1(checkOPR(oprFromCS2toCS1, getExtendable().getConverterStation1(), getExtendable().getConverterStation2())));
        return this;
    }

    private float checkOPR(float opr, HvdcConverterStation<?> from, HvdcConverterStation<?> to) {
        if ((!Float.isNaN(opr)) && (opr < 0)) {
            String message = "OPR from " + from.getId() + " to " + to.getId() + " must be greater than 0 (current value " + opr + ").";
            throw new IllegalArgumentException(message);
        }
        return opr;
    }
}
