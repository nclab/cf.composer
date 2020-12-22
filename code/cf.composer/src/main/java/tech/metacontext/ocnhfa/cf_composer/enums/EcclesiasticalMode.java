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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static tech.metacontext.ocnhfa.cf_composer.enums.Pitch.*;
import tech.metacontext.ocnhfa.cf_composer.model.devices.Cadence;
import tech.metacontext.ocnhfa.cf_composer.model.y.PitchNode;
import tech.metacontext.ocnhfa.cf_composer.model.y.PitchPath;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum EcclesiasticalMode {

    Dorian(List.of(D3), List.of(A3)),
    Hypodorian(List.of(D3, D4), List.of(F3, F4)),
    Phrygian(List.of(E3), List.of(C4)),
    Hypophrygian(List.of(E3), List.of(A3)),
//    Lydian(List.of(F3), List.of(C4)),
//    Hypolydian(List.of(F3), List.of(A3)),
    Mixolydian(List.of(G3), List.of(D4)),
    Hypomixolydian(List.of(G3), List.of(C4)),
    Aeolian(List.of(A3), List.of(E4)),
    Hypoaeolian(List.of(A3), List.of(C4)),
    Ionian(List.of(C3), List.of(G3)),
    Hypoionian(List.of(C3), List.of(E3)),
//    Locrian(List.of(B2, B3), List.of(G3, G4)),
//    Hypolocrian(List.of(B2, B3), List.of(E3, E4)),
    RANDOM;

    private Map<PitchNode, PitchNode> dominants;
    private List<Cadence> cadences;

    EcclesiasticalMode() {

    }

    EcclesiasticalMode(List<Pitch> finalis, List<Pitch> dominants) {

        this.dominants = IntStream.range(0, finalis.size())
                .mapToObj(i -> new SimpleEntry<>(finalis.get(i).getNode(), dominants.get(i).getNode()))
                .collect(Collectors.toMap(e -> e.getKey(), t -> t.getValue()));
        this.cadences = finalis.stream()
                .filter(pn -> pn.ordinal() + 1 < Pitch.values().length)
                .map(PitchNode::new)
                .map(Cadence::new)
                .collect(Collectors.toList());
    }

    public static EcclesiasticalMode getRandomMode() {

        return values()[new Random().nextInt(EcclesiasticalMode.values().length - 1)];
    }

    public PitchPath getRandomFinalis() {

        var index = new Random().nextInt(dominants.size());
        var result = dominants.keySet().toArray(new PitchNode[0])[index];
        return new PitchPath(result, dominants.get(result), 0.0);
    }

    public Map<PitchNode, PitchNode> getDominants() {

        return this.dominants;
    }

    public List<Cadence> getCadences() {

        return cadences;
    }

}
