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
package tech.metacontext.ocnhfa.cf_composer.enums;

import java.util.Objects;
import tech.metacontext.ocnhfa.cf_composer.ex.UnexpectedIntervalException;
import tech.metacontext.ocnhfa.cf_composer.model.x.MusicNode;
import tech.metacontext.ocnhfa.cf_composer.model.y.PitchNode;
import tech.metacontext.ocnhfa.cf_composer.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Pitch {

    //0, 1,  2,  3,  4,  5,  6,  7
    G2, A2, B2, C3, D3, E3, F3, G3,
    //8, 9, 10, 11, 12, 13, 14
    A3, B3, C4, D4, E4, F4, G4;

    private final PitchNode node;

    private Pitch() {

        this.node = new PitchNode(this);
    }

    public PitchNode getNode() {

        return node;
    }

    public PitchPath up(int interval) {

        if (interval == 6 && this.name().matches("[CDFG].")) {
            return null;
        }
        return getPitchPath(this.ordinal() + interval - 1);
    }

    public PitchPath down(int interval) {

        return getPitchPath(this.ordinal() - interval + 1);
    }

    public PitchPath getPitchPath(int target_ordinal) {

        try {
            var target = Pitch.values()[target_ordinal];
            var cost = getCostByInterval(Math.abs(this.ordinal() - target_ordinal) + 1);
            return tritoneTest(target) ? null :
                    new PitchPath(this.node, target.node, cost);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public boolean tritoneTest(Pitch target) {

        var join = this.name() + target.name();
        return (join.contains("B") && join.contains("F"));
    }

    public static double getCostByInterval(int interval)
            throws UnexpectedIntervalException {

        return switch (interval) {
            case 2->
                1.0;//return 1.0 / DOMINANT_ATTRACTION_FACTOR;
            case 3->
                1.0;//return 2.5;
            case 5,8->
                4.0;//return 2.5;
            case 4->
                8.0;//return 3.5;
            case 6->
                16.0;//return 5.5;
            default->
                throw new UnexpectedIntervalException(interval);
        };
    }

    public static int diff(PitchNode from, PitchNode to) {

        var from_ordinal = from.getPitch().ordinal();
        var to_ordinal = to.getPitch().ordinal();
        var diff_raw = to_ordinal - from_ordinal;
        return diff_raw + ((diff_raw >= 0) ? 1 : -1);
    }

    public static int diff(PitchPath path) {

        if (Objects.isNull(path)) {
            return 0;
        }
        return diff(path.getFrom(), path.getTo());
    }

    public static Pitch valueOf(MusicNode node) {

        return Pitch.valueOf(node.getName());
    }
}
