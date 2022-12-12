/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Connectable;

/**
 * FIXME: to implement
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesTapChangersAdderImplProvider<C extends Connectable<C>>
        implements ExtensionAdderProvider<C, CgmesTapChangers<C>, CgmesTapChangersAdderImpl<C>> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return CgmesTapChangers.NAME;
    }

    @Override
    public Class<? super CgmesTapChangersAdderImpl<C>> getAdderClass() {
        return CgmesTapChangersAdderImpl.class;
    }

    @Override
    public CgmesTapChangersAdderImpl<C> newAdder(C extendable) {
        return new CgmesTapChangersAdderImpl<>(extendable);
    }
}
