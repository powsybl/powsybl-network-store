/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreaAdder;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesControlAreasImpl extends AbstractExtension<Network> implements CgmesControlAreas {

    public CgmesControlAreasImpl(NetworkImpl network) {
        super(network.initCgmesControlAreas());
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    @Override
    public CgmesControlAreaAdder newCgmesControlArea() {
        return new CgmesControlAreaAdderImpl(getNetwork().getIndex(), getNetwork().getResource().getAttributes());
    }

    @Override
    public Collection<CgmesControlArea> getCgmesControlAreas() {
        return getNetwork()
                .getResource()
                .getAttributes()
                .getCgmesControlAreas()
                .getControlAreas()
                .stream()
                .map(a -> new CgmesControlAreaImpl(getNetwork().getIndex(), a))
                .collect(Collectors.toSet());
    }

    @Override
    public CgmesControlArea getCgmesControlArea(String controlAreaId) {
        Objects.requireNonNull(controlAreaId);
        return getNetwork()
                .getResource()
                .getAttributes()
                .getCgmesControlAreas()
                .getControlAreas()
                .stream()
                .filter(a -> a.getId().equals(controlAreaId))
                .map(a -> new CgmesControlAreaImpl(getNetwork().getIndex(), a))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean containsCgmesControlAreaId(String controlAreaId) {
        Objects.requireNonNull(controlAreaId);
        return getNetwork()
                .getResource()
                .getAttributes()
                .getCgmesControlAreas()
                .getControlAreas()
                .stream()
                .anyMatch(a -> a.getId().equals(controlAreaId));
    }

    @Override
    public void cleanIfEmpty() {
    }
}
