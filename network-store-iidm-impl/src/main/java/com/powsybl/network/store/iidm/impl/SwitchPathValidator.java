package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.network.store.model.SwitchAttributes;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.PathValidator;

import java.util.Optional;
import java.util.function.Predicate;

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
