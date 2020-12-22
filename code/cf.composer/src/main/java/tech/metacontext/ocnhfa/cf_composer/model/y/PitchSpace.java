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
package tech.metacontext.ocnhfa.cf_composer.model.y;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;
import tech.metacontext.ocnhfa.cf_composer.enums.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PitchSpace extends StandardGraph<PitchPath, PitchNode> {

    public PitchSpace(double alpha, double beta) {

        super(alpha, beta);
    }

    @Override
    public void init_graph() {

        for (Pitch pitch : Pitch.values()) {
            IntStream.of(2, 3, 4, 5, 6, 8)
                    .mapToObj(pitch::up)
                    .filter(Objects::nonNull)
                    .forEach(this::addEdges);
            IntStream.of(2, 3, 4, 5, 8)
                    .mapToObj(pitch::down)
                    .filter(Objects::nonNull)
                    .forEach(this::addEdges);
        }
    }

    @Override
    @Deprecated
    public PitchMove move(PitchNode current, double pheromone_deposit,
            double explore_chance, double... parameters) {

        System.out.println("Invalid call: PitchSpace move().");
        System.exit(-1);
        return null;
    }

    public PitchMove move(PitchNode current, PitchNode dominant,
            Predicate<PitchPath> filter,
            double pheromone_deposit, double explore_chance,
            double... parameters) {

        var isExploring = Math.random() < explore_chance;
        var paths = this.queryByVertex(current).stream()
                .filter(path -> isExploring || filter.test(path))
                .collect(Collectors.toList());
        if (paths.isEmpty()) {
            return null;
        }
        var fractions = new ArrayList<Double>();
        var sum = paths.stream()
                .mapToDouble(this::getFraction)
                .peek(fractions::add)
                .sum();
        var r = new AtomicReference<Double>(Math.random() * sum);
//        System.out.printf("explore = %b, sum = %f, r = %f,", explore, sum, r.get());
        var selected
                = //* isExploring ? paths.get(new Random().nextInt(paths.size())): */ 
                IntStream.range(0, paths.size())
                        .filter(i -> r.getAndSet(r.get() - fractions.get(i)) < fractions.get(i))
                        .mapToObj(paths::get)
                        .findFirst().get();
//        System.out.printf("selected = %s\n", selected);
        selected.addPheromoneDeposit(pheromone_deposit);
        return new PitchMove(isExploring, paths, selected, MusicThought.NULL);
    }

}
