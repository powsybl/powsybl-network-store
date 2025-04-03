/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.extensions.CgmesControlAreasAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CgmesControlAreasAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesControlAreasAdderImpl extends AbstractIidmExtensionAdder<Network, CgmesControlAreas> implements CgmesControlAreasAdder {

    CgmesControlAreasAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesControlAreas createExtension(Network extendable) {
        ((NetworkImpl) extendable).updateResourceWithoutNotification(res -> res.getAttributes().setCgmesControlAreas(new CgmesControlAreasAttributes()));
        return new CgmesControlAreasImpl((NetworkImpl) extendable);
    }
}
