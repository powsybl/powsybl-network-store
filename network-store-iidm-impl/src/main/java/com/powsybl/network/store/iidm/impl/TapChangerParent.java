/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Validable;
import com.powsybl.network.store.model.PhaseTapChangerAttributes;
import com.powsybl.network.store.model.RatioTapChangerAttributes;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface TapChangerParent extends Validable {

    NetworkImpl getNetwork();

    AbstractIdentifiableImpl<?, ?> getTransformer();

    String getTapChangerAttribute();

    default Set<TapChanger<?, ?, ?>> getAllTapChangers() {
        Set<TapChanger<?, ?, ?>> tapChangers = new HashSet<>();
        RatioTapChanger ratioTapChanger = getRatioTapChanger();
        if (ratioTapChanger != null) {
            tapChangers.add(ratioTapChanger);
        }
        PhaseTapChanger phaseTapChanger = getPhaseTapChanger();
        if (phaseTapChanger != null) {
            tapChangers.add(phaseTapChanger);
        }
        return tapChangers;
    }

    PhaseTapChanger getPhaseTapChanger();

    RatioTapChanger getRatioTapChanger();

    void setPhaseTapChanger(PhaseTapChangerAttributes attributes);

    void setRatioTapChanger(RatioTapChangerAttributes attributes);
}
