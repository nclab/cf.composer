/*
 * Copyright 2019 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.metacontext.ocnhfa.antsomg.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.model.Graph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <E>
 * @param <V>
 */
public abstract class StandardGraph<E extends StandardEdge<V>, V extends StandardVertex>
        implements Graph<E, V, StandardMove<E>> {

    private List<E> edges;
    private V start;
    protected double alpha, beta;
    private FractionMode fraction_mode;

    public enum FractionMode {

        Power, Coefficient;
    }

    public StandardGraph(double alpha, double beta) {

        this.edges = new ArrayList<>();
        this.alpha = alpha;
        this.beta = beta;
        this.fraction_mode = FractionMode.Coefficient;
        this.init_graph();
    }

    public double getFraction(E edge) {

        return switch (this.fraction_mode) {
            case Power->
                Math.pow(edge.getPheromoneTrail(), this.alpha)
                + Math.pow(1.0 / edge.getCost(), this.beta);
            case Coefficient->
                edge.getPheromoneTrail() * this.alpha
                + 1.0 / edge.getCost() * this.beta;
        };
    }

    @Override
    public StandardMove<E> move(V current, double pheromone_deposit,
            double explore_chance, double... parameters) {

        var paths = this.queryByVertex(current);
        var fractions = new ArrayList<Double>();
        var sum = paths.stream()
                .mapToDouble(this::getFraction)
                .peek(fractions::add)
                .sum();
        var r = new AtomicReference<Double>(Math.random() * sum);
        var isExploring = Math.random() < explore_chance;
        var selected = isExploring ? paths.get(new Random().nextInt(paths.size())) :
                 IntStream.range(0, paths.size())
                        .filter(i -> r.getAndSet(r.get() - fractions.get(i)) < fractions.get(i))
                        .mapToObj(paths::get)
                        .findFirst().get();
        selected.addPheromoneDeposit(pheromone_deposit);
        return StandardMove.getInstance(isExploring, paths, selected);
    }

    @Override
    public List<E> queryByVertex(V vertex) {

        return this.edges.stream()
                .filter(edge -> Objects.equals(edge.getFrom(), vertex))
                .collect(Collectors.toList());
    }

    @Override
    public String asXML() {

        var doc = DocumentHelper.createDocument();
        doc.setXMLEncoding("UTF-8");
        var root = doc.addElement(this.getClass().getSimpleName());
        getEdges().stream().forEach(e -> {
            var edge = root.addElement(e.getClass().getSimpleName());
            edge.addElement("from").setText(e.getFrom().getName());
            edge.addElement("to").setText(e.getTo().getName());
            edge.addElement("cost").setText(String.valueOf(e.getCost()));
            edge.addElement("pheromoneTrail").setText(String.valueOf(e.getPheromoneTrail()));
        });
        return doc.asXML();
    }

    public String asGraphviz() {

        return String.format("digraph %s {\n%s\n}",
                this.getClass().getSimpleName(),
                getEdges().stream().map(path -> String.format("\t%s -> %s [ label=\"p=%.3f,c=%.2f\"];",
                path.getFrom().getName(),
                path.getTo().getName(),
                path.getPheromoneTrail(),
                path.getCost())
                ).collect(Collectors.joining("\n")));
    }

    public void addEdges(E... edges) {

        this.edges.addAll(List.of(edges));
    }

    public List<E> getEdges() {

        return this.edges;
    }

    public void setEdges(List<E> edges) {

        this.edges = edges;
    }

    public V getStart() {

        return start;
    }

    public void setStart(V start) {

        this.start = start;
    }

    public FractionMode getFraction_mode() {

        return fraction_mode;
    }

    public void setFraction_mode(FractionMode fraction_mode) {

        this.fraction_mode = fraction_mode;
    }

}
