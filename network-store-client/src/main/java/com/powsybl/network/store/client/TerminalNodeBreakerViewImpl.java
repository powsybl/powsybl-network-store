/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.InjectionAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalNodeBreakerViewImpl<U extends InjectionAttributes> implements Terminal.NodeBreakerView {

    private final U attributes;

    TerminalNodeBreakerViewImpl(U attributes) {
        this.attributes = attributes;
    }

    @Override
    public int getNode() {
        Integer node = attributes.getNode();
        if (node == null) {
            throw new PowsyblException("Not supported in a bus breaker topology");
        }
        return node;
    }
}
