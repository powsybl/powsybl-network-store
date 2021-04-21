/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.iidm.impl.NetworkObjectIndex;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.CgmesControlAreaAttributes;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesControlAreaImpl implements CgmesControlArea {

    private final NetworkObjectIndex index;

    private final CgmesControlAreaAttributes attributes;

    CgmesControlAreaImpl(NetworkObjectIndex index, CgmesControlAreaAttributes attributes) {
        this.index = Objects.requireNonNull(index);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public String getId() {
        return attributes.getId();
    }

    @Override
    public String getName() {
        return attributes.getName();
    }

    @Override
    public String getEnergyIdentificationCodeEIC() {
        return attributes.getEnergyIdentificationCodeEic();
    }

    @Override
    public Set<Terminal> getTerminals() {
        return attributes.getTerminals().stream().map(a -> TerminalRefUtils.getTerminal(index, a)).collect(Collectors.toSet());
    }

    @Override
    public Set<Boundary> getBoundaries() {
        return attributes.getBoundaries().stream().map(a -> TerminalRefUtils.getTerminal(index, a)).collect(Collectors.toSet());
    }

    @Override
    public double getNetInterchange() {
        return attributes.getNetInterchange();
    }

    @Override
    public void add(Terminal terminal) {
        attributes.getTerminals().add(TerminalRefUtils.getTerminalRefAttributes(terminal));
    }

    @Override
    public void add(Boundary boundary) {
        boundaries.add(boundary.);
    }
}
