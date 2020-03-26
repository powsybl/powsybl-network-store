/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public interface RelatedVoltageLevelsAttributes {

    Set<String> getVoltageLevels();

}
