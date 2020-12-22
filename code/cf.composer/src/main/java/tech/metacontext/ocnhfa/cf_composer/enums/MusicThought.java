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

import java.util.function.Predicate;
import tech.metacontext.ocnhfa.cf_composer.ex.UnexpectedMusicNodeException;
import tech.metacontext.ocnhfa.cf_composer.ex.UnexpectedMusicThoughtException;
import tech.metacontext.ocnhfa.cf_composer.model.MusicThread;
import tech.metacontext.ocnhfa.cf_composer.model.x.MusicNode;
import tech.metacontext.ocnhfa.cf_composer.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum MusicThought {

    Directional_Conjunct,
    Directional_Disjunct,
    Complemental_ShortTerm,
    Complemental_LongTerm,
    NULL;

    public static final MusicNode START = new MusicNode("Start");
    public static final MusicNode DIRECTIONAL = new MusicNode("Directional");
    public static final MusicNode COMPLEMENTAL = new MusicNode("Complemental");
    public static final MusicNode CONJUNCT = new MusicNode("Conjunct");
    public static final MusicNode DISJUNCT = new MusicNode("Disjunct");
    public static final MusicNode SHORTTERM = new MusicNode("ShortTerm");
    public static final MusicNode LONGTERM = new MusicNode("LongTerm");

    public static MusicNode getNode(String name) {

        return switch (name) {
            case "Start"->
                START;
            case "Directional"->
                DIRECTIONAL;
            case "Complemental"->
                COMPLEMENTAL;
            case "Conjunct"->
                CONJUNCT;
            case "Disjunct"->
                DISJUNCT;
            case "ShortTerm"->
                SHORTTERM;
            case "LongTerm"->
                LONGTERM;
            default->
                throw new UnexpectedMusicThoughtException(name);
        };
    }

    public Predicate<PitchPath> getPredicate(MusicThread thread) {

        return path -> thread.getCf().size() > 1 ? switch (this) {
            case Directional_Conjunct:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) == 2)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) == -2);
            case Directional_Disjunct:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) > 2)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) < -2);
            case Complemental_LongTerm:
                if (thread.lastPitchLevel() != 0)
                    yield (thread.lastPitchLevel() > 0 && Pitch.diff(path) < 0)
                    || (thread.lastPitchLevel() < 0 && Pitch.diff(path) > 0);
            case Complemental_ShortTerm:
                yield (thread.lastPitchDirection() > 0 && Pitch.diff(path) < 0)
                || (thread.lastPitchDirection() < 0 && Pitch.diff(path) > 0);
            default:
                yield true;
        } : switch (this) {
            case Directional_Conjunct->
                Pitch.diff(path) == 2 || Pitch.diff(path) == -2;
            case Directional_Disjunct->
                Pitch.diff(path) > 2 || Pitch.diff(path) < -2;
            default->
                true;
        };
    }

    public static MusicThought getInstance(MusicNode node1, MusicNode node2)
            throws UnexpectedMusicNodeException {

        switch (node1.getName()) {
            case "Directional":
                switch (node2.getName()) {
                    case "Conjunct":
                        return Directional_Conjunct;
                    case "Disjunct":
                        return Directional_Disjunct;
                }
            case "Complemental":
                switch (node2.getName()) {
                    case "ShortTerm":
                        return Complemental_ShortTerm;
                    case "LongTerm":
                        return Complemental_LongTerm;
                }
        }
        throw new UnexpectedMusicNodeException(node1, node2);
    }
}
