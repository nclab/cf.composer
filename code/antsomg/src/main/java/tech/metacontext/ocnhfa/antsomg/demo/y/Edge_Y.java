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
package tech.metacontext.ocnhfa.antsomg.demo.y;

import tech.metacontext.ocnhfa.antsomg.impl.StandardEdge;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Edge_Y extends StandardEdge<Vertex_Y> {

    public Edge_Y(Vertex_Y from, Vertex_Y to, Double cost) {

        super(from, to, cost);
    }

    public Edge_Y(Vertex_Y to) {

        this(null, to, 0.0);
    }
}
