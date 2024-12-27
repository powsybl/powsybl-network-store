/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractIidmExtensionAdder<I extends Identifiable<I>, E extends Extension<I>> extends AbstractExtensionAdder<I, E> {

    protected AbstractIidmExtensionAdder(I identifiable) {
        super(identifiable);
    }

    @Override
    public E add() {
        E extension = super.add();
        List<NetworkListener> listeners = ((NetworkImpl) extendable.getNetwork()).getListeners();
        for (NetworkListener listener : listeners) {
            listener.onExtensionCreation(extension);
        }
        return extension;
    }
}
