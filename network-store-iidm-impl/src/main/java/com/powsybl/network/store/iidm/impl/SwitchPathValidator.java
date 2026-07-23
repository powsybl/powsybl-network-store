/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.network.store.model.SwitchAttributes;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.PathValidator;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class SwitchPathValidator implements PathValidator<Integer, Edge> {

    private final Predicate<Switch> isSwitchValidInGraph;
    private final NetworkObjectIndex index;

    public SwitchPathValidator(Predicate<Switch> isSwitchValidInGraph, NetworkObjectIndex index) {
        this.isSwitchValidInGraph = isSwitchValidInGraph;
        this.index = index;
    }

    @Override
    public boolean isValidPath(GraphPath<Integer, Edge> graphPath, Edge edge) {

        if (edge.getBiConnectable() instanceof SwitchAttributes switchAttributes) {

            // Get the switch behind the switchAttributes
            Optional<SwitchImpl> sw = index.getSwitch(switchAttributes.getResource().getId());

            // The path is valid if the switch can be operated
            return sw.isPresent() && isSwitchValidInGraph.test(sw.get());
        }
        return true;
    }
}
