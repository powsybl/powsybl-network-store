/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.NodeBreakerBiConnectable;
import lombok.Getter;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Getter
public class Edge extends DefaultWeightedEdge {

    private final NodeBreakerBiConnectable biConnectable;

    public Edge(NodeBreakerBiConnectable biConnectable) {
        this.biConnectable = Objects.requireNonNull(biConnectable);
    }

}
