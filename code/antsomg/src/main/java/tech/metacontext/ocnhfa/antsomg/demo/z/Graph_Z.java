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
package tech.metacontext.ocnhfa.antsomg.demo.z;

import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph;

/**
 *
 * @author Jonathan Chang, Chun-zien <ccz@musicapoetica.org>
 */
public class Graph_Z extends StandardGraph<Edge_Z, Vertex_Z> {

    public Graph_Z(double alpha, double beta) {

        super(alpha, beta);
    }

    @Override
    public void init_graph() {

        this.setStart(new Vertex_Z("Start"));

        var z1 = new Edge_Z(this.getStart(), new Vertex_Z("z1"), 1.0);
        var z2 = new Edge_Z(z1.getTo(), new Vertex_Z("z2"), 1.0);
        var z3 = new Edge_Z(z2.getTo(), this.getStart(), 1.0);
        this.addEdges(z1, z1.<Edge_Z>getReverse(), z2, z2.<Edge_Z>getReverse(), z3, z3.<Edge_Z>getReverse());
    }
}
