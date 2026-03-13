/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.network.store.model.utils.Views;
import lombok.Getter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Getter
public enum AttributeFilter {

    SV(1),
    STANDARD(2),
    WITH_LIMITS(3);

    private final int priority;

    AttributeFilter(int priority) {
        this.priority = priority;
    }

    public static Class<?> getViewClass(AttributeFilter filter) {
        return switch (filter) {
            case SV -> Views.SvView.class;
            case STANDARD -> Views.Standard.class;
            case WITH_LIMITS -> Views.WithLimits.class;
        };
    }
}
