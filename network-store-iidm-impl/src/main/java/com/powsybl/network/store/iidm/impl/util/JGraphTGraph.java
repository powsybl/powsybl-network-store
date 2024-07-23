/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.Edge;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class JGraphTGraph {

    private final Graph<Integer, Edge> graph;

    public JGraphTGraph(Graph<Integer, Edge> graph) {
        this.graph = graph;
    }

    public List<List<Edge>> findAllPaths(int from,
                                          Predicate<Integer> pathComplete,
                                          Predicate<Edge> pathCancelled,
                                          Comparator<List<Edge>> comparator) {

        List<List<Edge>> paths = new ArrayList<>();
        BitSet encountered = new BitSet(Collections.max(graph.vertexSet()) + 1);
        List<Edge> path = new ArrayList<>();
        findAllPaths(from, pathComplete, pathCancelled, path, encountered, paths);

        // sort paths by size according to the given comparator
        paths.sort(comparator);
        return paths;
    }

    private void findAllPaths(int v, Predicate<Integer> pathComplete, Predicate<Edge> pathCancelled,
                              List<Edge> path, BitSet encountered, List<List<Edge>> paths) {
        if (v < 0) {
            throw new PowsyblException("Invalid vertex " + v);
        }
        encountered.set(v, true);
        List<Edge> adjacentEdges = graph.edgesOf(v).stream().toList();
        for (int i = 0; i < adjacentEdges.size(); i++) {
            Edge edge = adjacentEdges.get(i);
            if (pathCancelled != null && pathCancelled.test(edge)) {
                // Do not continue on this path if the edge cannot be traversed
                continue;
            }
            int v1 = graph.getEdgeSource(edge);
            int v2 = graph.getEdgeTarget(edge);
            List<Edge> path2;
            BitSet encountered2;
            if (i < adjacentEdges.size() - 1) {
                path2 = new ArrayList<>(path);
                encountered2 = new BitSet(graph.vertexSet().size());
                encountered2.or(encountered);
            } else {
                path2 = path;
                encountered2 = encountered;
            }
            if (v == v2) {
                findAllPaths(edge, v1, pathComplete, pathCancelled, path2, encountered2, paths);
            } else if (v == v1) {
                findAllPaths(edge, v2, pathComplete, pathCancelled, path2, encountered2, paths);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private void findAllPaths(Edge edge, int v1or2, Predicate<Integer> pathComplete, Predicate<Edge> pathCancelled,
                                 List<Edge> path, BitSet encountered, List<List<Edge>> paths) {
        if (encountered.get(v1or2)) {
            return;
        }
        path.add(edge);
        if (Boolean.TRUE.equals(pathComplete.test(v1or2))) {
            paths.add(path);
        } else {
            findAllPaths(v1or2, pathComplete, pathCancelled, path, encountered, paths);
        }
    }
}
