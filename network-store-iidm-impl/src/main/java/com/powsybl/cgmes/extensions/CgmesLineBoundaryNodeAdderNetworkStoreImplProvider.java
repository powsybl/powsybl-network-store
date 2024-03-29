/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.TieLine;

/**
 * FIXME: to implement
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesLineBoundaryNodeAdderNetworkStoreImplProvider implements ExtensionAdderProvider<TieLine, CgmesLineBoundaryNode, CgmesLineBoundaryNodeAdderImpl> {
    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<? super CgmesLineBoundaryNodeAdderImpl> getAdderClass() {
        return CgmesLineBoundaryNodeAdderImpl.class;
    }

    @Override
    public CgmesLineBoundaryNodeAdderImpl newAdder(TieLine tieLine) {
        return new CgmesLineBoundaryNodeAdderImpl(tieLine);
    }
}
