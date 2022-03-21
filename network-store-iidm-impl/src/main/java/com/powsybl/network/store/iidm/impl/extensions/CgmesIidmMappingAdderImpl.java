/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesIidmMappingAdder;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesIidmMappingAdderImpl extends AbstractExtensionAdder<Network, CgmesIidmMapping> implements CgmesIidmMappingAdder {

    private Set<String> topologicalNodes = new HashSet<>();

    CgmesIidmMappingAdderImpl(Network network) {
        super(network);
    }

    @Override
    protected CgmesIidmMapping createExtension(Network network) {
        return new CgmesIidmMappingImpl((NetworkImpl) network, topologicalNodes);
    }

    @Override
    public CgmesIidmMappingAdder addTopologicalNode(String topologicalNodeId, String topologicalNodeName, CgmesIidmMapping.Source source) {
        return null;
    }

    @Override
    public CgmesIidmMappingAdder addBaseVoltage(String baseVoltage, double nominalVoltage, CgmesIidmMapping.Source source) {
        return null;
    }
//
//    @Override
//    public CgmesIidmMappingAdder addTopologicalNode(String topologicalNode) {
//        topologicalNodes.add(Objects.requireNonNull(topologicalNode));
//        return this;
//    }

}
