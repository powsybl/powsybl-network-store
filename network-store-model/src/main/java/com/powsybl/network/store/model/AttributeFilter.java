/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum AttributeFilter {

    SV(Set.of("p", "q", "calculatedBusesForBusView", "calculatedBusesForBusBreakerView"));

    private final Set<String> included;

    AttributeFilter(Set<String> included) {
        this.included = Objects.requireNonNull(included);
    }

    public Set<String> getIncluded() {
        return included;
    }
}
