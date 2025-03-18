/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesControlAreaAdder;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CgmesControlAreaAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesControlAreaAdderImpl implements CgmesControlAreaAdder {

    private final CgmesControlAreas parent;

    private final NetworkImpl network;

    private String id;

    private String name;

    private String energyIdentificationCodeEic;

    private double netInterchange = Double.NaN;

    private double pTolerance = Double.NaN;

    CgmesControlAreaAdderImpl(CgmesControlAreas parent, NetworkImpl network) {
        this.parent = parent;
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public CgmesControlAreaAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setEnergyIdentificationCodeEic(String energyIdentificationCodeEic) {
        this.energyIdentificationCodeEic = energyIdentificationCodeEic;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setNetInterchange(double netInterchange) {
        this.netInterchange = netInterchange;
        return this;
    }

    @Override
    public CgmesControlAreaAdder setPTolerance(double pTolerance) {
        this.pTolerance = pTolerance;
        return this;
    }

    @Override
    public CgmesControlAreaImpl add() {
        if (id == null) {
            throw new PowsyblException("Undefined ID for CGMES control area");
        }
        CgmesControlAreaAttributes attributes = new CgmesControlAreaAttributes(id, name, energyIdentificationCodeEic, new ArrayList<>(), new ArrayList<>(), netInterchange, pTolerance);
        List<CgmesControlAreaAttributes> controlAreas = network.getResource().getAttributes().getCgmesControlAreas().getControlAreas();
        int index = controlAreas.size();
        controlAreas.add(attributes);
        return new CgmesControlAreaImpl(parent, network, index);
    }
}
