/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.network.store.model.CgmesTapChangersAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class CgmesTapChangersLoader<C extends Connectable<C>> implements ExtensionLoader<C, CgmesTapChangers<C>, CgmesTapChangersAttributes> {

    public Extension<C> load(C extendable) {
        return new CgmesTapChangersImpl<C>(extendable);
    }

    public String getName() {
        return CgmesTapChangers.NAME;
    }

    public Class<? super CgmesTapChangers<C>> getType() {
        return CgmesTapChangers.class;
    }

    public Class<? super CgmesTapChangersAttributes> getAttributesType() {
        return CgmesTapChangersAttributes.class;
    }
}
