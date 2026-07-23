/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.ReferencePrioritiesAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class ReferencePrioritiesLoader<C extends Connectable<C>> implements ExtensionLoader<C, ReferencePriorities<C>, ReferencePrioritiesAttributes> {
    @Override
    public Extension<C> load(C connectable) {
        return new ReferencePrioritiesImpl<>(connectable);
    }

    @Override
    public String getName() {
        return ReferencePriorities.NAME;
    }

    @Override
    public Class<ReferencePriorities> getType() {
        return ReferencePriorities.class;
    }

    @Override
    public Class<ReferencePrioritiesAttributes> getAttributesType() {
        return ReferencePrioritiesAttributes.class;
    }
}
