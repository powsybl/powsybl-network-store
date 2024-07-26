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
 * This class is used to provide JGraphT graphs with the same methods as the UndirectedGraph implementation from
 * powsybl-core. As such, the results of the methods in both implementations are the same.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class JGraphTGraph {

    private final Graph<Integer, Edge> graph;

    public JGraphTGraph(Graph<Integer, Edge> graph) {
        this.graph = graph;
    }

    /**
     * Find all paths from the specified vertex.
     * This method relies on two functions to stop the traverse when the target vertex is found or when an edge must not be traversed.
     * <p>
     * This method allocates a {@link List} of {@link List} of {@link Edge} to store the paths, a {@link BitSet} to store the encountered vertices
     * and calls {@link #findAllPaths(int, Predicate, Predicate, List, BitSet, List)}.
     * In the output, the paths are sorted by using the given comparator.
     * </p>
     * <p>This method is a copy of the same method from UndirectedGraph in powsybl-core.</p>
     *
     * @param from the vertex index where the traverse has to start.
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled a function that returns true when the edge must not be traversed.
     * @param comparator a comparator used to sort the paths
     * @return a list that contains the index of the traversed edges.
     */
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

    /**
     * This method is called by {@link #findAllPaths(int, Predicate, Predicate,Comparator)}.
     * For each adjacent edges for which the pathCanceled returns {@literal false}, traverse the other vertex calling {@link #findAllPaths(Edge, int, Predicate, Predicate, List, BitSet, List)}.
     * <p>This method is a copy of the same method from UndirectedGraph in powsybl-core.</p>
     *
     * @param v the current vertex
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled a function that returns true when the edge must not be traversed.
     * @param path a list that contains the traversed edges.
     * @param encountered a BitSet that contains the traversed vertex.
     * @param paths a list that contains the complete paths.
     */
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

    /**
     * This method is called by {@link #findAllPaths(int, Predicate, Predicate, List, BitSet, List)} each time a vertex is traversed.
     * The path is added to the paths list if it's complete, otherwise this method calls the {@link #findAllPaths(int, Predicate, Predicate, List, BitSet, List)}
     * to continue the recursion.
     * <p>This method is a copy of the same method from UndirectedGraph in powsybl-core.</p>
     *
     * @param edge the current edge.
     * @param v1or2 the index of the current vertex.
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled pathCanceled a function that returns true when the edge must not be traversed.
     * @param path a list that contains the traversed edges.
     * @param encountered a BitSet that contains the traversed vertex.
     * @param paths a list that contains the complete paths.
     */
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
