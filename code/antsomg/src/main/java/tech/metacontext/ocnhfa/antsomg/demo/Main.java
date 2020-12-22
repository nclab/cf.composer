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
package tech.metacontext.ocnhfa.antsomg.demo;

import tech.metacontext.ocnhfa.antsomg.model.Graph;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Main {

    public static void main(String[] args) {
        var demo = new DemoSystem(10);
        demo.init_graphs();
        demo.init_population();
        while (!demo.isAimAchieved()) {
            demo.navigate();
        }
        demo.getAnts().forEach(ant -> {
            System.out.println("***Ant");
            ant.route.forEach(loc -> {
                System.out.println(loc);
            });
        });
        demo.getGraphs().values().stream()
                .map(Graph::asXML)
                .forEach(System.out::println);
    }
}
