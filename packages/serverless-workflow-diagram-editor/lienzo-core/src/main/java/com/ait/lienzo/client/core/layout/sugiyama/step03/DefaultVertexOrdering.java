/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ait.lienzo.client.core.layout.sugiyama.step03;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.ait.lienzo.client.core.layout.OrientedEdgeImpl;
import com.ait.lienzo.client.core.layout.ReorderedGraph;
import com.ait.lienzo.client.core.layout.VertexPosition;
import com.ait.lienzo.client.core.layout.sugiyama.GraphLayer;
import com.ait.lienzo.client.core.layout.sugiyama.LayeredGraph;
import com.ait.lienzo.client.core.layout.sugiyama.OrientedEdge;

import static java.util.stream.Collectors.toList;

/**
 * Order vertices inside layers trying to reduce crossing between edges.
 */
public final class DefaultVertexOrdering implements VertexOrdering {

    private final VertexLayerPositioning vertexPositioning;
    private final LayerCrossingCount crossingCount;
    private final VerticesTransposer verticesTransposer;

    /**
     * Maximum number of iterations to perform.
     * 24 is the optimal number (Gansner et al 1993).
     */
    private static final int MAX_ITERATIONS = 24;

    /**
     * Default constructor.
     * @param vertexPositioning The strategy to find the position of the vertices inside a layer.
     * @param crossingCount The strategy to count the edges crossing.
     * @param verticesTransposer The strategy to transpose vertices in a layer.
     */
    public DefaultVertexOrdering(final VertexLayerPositioning vertexPositioning,
                                 final LayerCrossingCount crossingCount,
                                 final VerticesTransposer verticesTransposer) {
        this.vertexPositioning = vertexPositioning;
        this.crossingCount = crossingCount;
        this.verticesTransposer = verticesTransposer;
    }

    /**
     * Reorder the vertices to reduce edges crossing.
     * @param graph The graph.
     */
    @Override
    public void orderVertices(final ReorderedGraph graph) {
        final LayeredGraph layered = (LayeredGraph) graph;
        final List<OrientedEdge> edges = graph.getEdges();
        final List<GraphLayer> virtualized = createVirtual(edges, layered);
        List<GraphLayer> best = copy(virtualized);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            this.vertexPositioning.positionVertices(virtualized, edges, i);
            this.verticesTransposer.transpose(virtualized, edges, i);
            if (this.crossingCount.crossing(best, edges) > this.crossingCount.crossing(virtualized, edges)) {
                best = copy(virtualized);
            } else {
                break;
            }
        }

        layered.getLayers().clear();
        layered.getLayers().addAll(best);
    }

    private List<GraphLayer> copy(final List<GraphLayer> input) {
        final ArrayList<GraphLayer> copy = new ArrayList<>(input.size());
        for (final GraphLayer value : input) {
            copy.add(value.copy());
        }
        return copy;
    }

    /**
     * Creates virtual vertices in edges that crosses multiple layers.
     * @param edges The existing edges.
     * @param graph The graph.
     * @return The layers with virtual vertices.
     */
    private List<GraphLayer> createVirtual(final List<OrientedEdge> edges,
                                           final LayeredGraph graph) {
        int virtualIndex = 0;
        final List<GraphLayer> virtualized = copy(graph.getLayers());

        for (int i = 0; i < virtualized.size() - 1; i++) {
            final GraphLayer currentLayer = virtualized.get(i);
            final GraphLayer nextLayer = virtualized.get(i + 1);
            for (final VertexPosition vertexPosition : currentLayer.getVertices()) {

                final List<OrientedEdge> outgoing = edges.stream()
                        .filter(e -> Objects.equals(e.getFromVertexId(), vertexPosition.getId()))
                        .filter(e -> Math.abs(getLayerNumber(e.getToVertexId(), virtualized) - getLayerNumber(vertexPosition.getId(), virtualized)) > 1)
                        .collect(toList());

                final List<OrientedEdge> incoming = edges.stream()
                        .filter(e -> Objects.equals(e.getToVertexId(), vertexPosition.getId()))
                        .filter(e -> Math.abs(getLayerNumber(e.getFromVertexId(), virtualized) - getLayerNumber(vertexPosition.getId(), virtualized)) > 1)
                        .collect(toList());

                for (final OrientedEdge edge : outgoing) {
                    final VertexPosition virtualVertexPosition = new VertexPosition("V" + virtualIndex++, true);
                    nextLayer.getVertices().add(virtualVertexPosition);
                    edges.remove(edge);
                    final OrientedEdge v1 = new OrientedEdgeImpl(edge.getFromVertexId(), virtualVertexPosition.getId());
                    final OrientedEdge v2 = new OrientedEdgeImpl(virtualVertexPosition.getId(), edge.getToVertexId());
                    edges.add(v1);
                    edges.add(v2);
                }

                for (final OrientedEdge edge : incoming) {
                    final VertexPosition virtualVertexPosition = new VertexPosition("V" + virtualIndex++, true);
                    nextLayer.getVertices().add(virtualVertexPosition);
                    edges.remove(edge);
                    final OrientedEdge v1 = new OrientedEdgeImpl(virtualVertexPosition.getId(), edge.getToVertexId());
                    final OrientedEdge v2 = new OrientedEdgeImpl(edge.getFromVertexId(), virtualVertexPosition.getId());
                    edges.add(v1);
                    edges.add(v2);
                }
            }
        }

        return virtualized;
    }

    private int getLayerNumber(final String vertex,
                               final List<GraphLayer> layers) {
        final GraphLayer layer = layers
                .stream()
                .filter(l -> l.getVertices().stream().anyMatch(v -> Objects.equals(v.getId(), vertex)))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Can not found the layer of the vertex."));

        return layer.getLevel();
    }
}
