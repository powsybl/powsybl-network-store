/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Validable;

import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface TapChangerParent extends Validable {
    NetworkImpl getNetwork();

    Identifiable getTransformer();

    String getTapChangerAttribute();

    Set<TapChanger> getAllTapChangers();

    PhaseTapChanger getPhaseTapChanger();

    RatioTapChanger getRatioTapChanger();
}
