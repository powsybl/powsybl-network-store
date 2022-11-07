/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum AttributeFilter {

    SV(Set.of("p", "q", "calculatedBusesForBusView", "calculatedBusesForBusBreakerView"), SvMixIn.class);

    private final Set<String> included;

    private final Class<?> mixInClass;

    @JsonFilter("SV")
    class SvMixIn {
    }

    AttributeFilter(Set<String> included, Class<?> mixInClass) {
        this.included = Objects.requireNonNull(included);
        this.mixInClass = Objects.requireNonNull(mixInClass);
    }

    public Set<String> getIncluded() {
        return included;
    }

    public Class<?> getMixInClass() {
        return mixInClass;
    }
}
