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
package tech.metacontext.ocnhfa.cf_composer.constrains;

import java.util.Objects;
import static tech.metacontext.ocnhfa.cf_composer.model.Parameters.*;
import tech.metacontext.ocnhfa.cf_composer.model.MusicThread;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicThreadRating {

    public static double range(MusicThread thread) {

        var div = 1.0;
        if (thread.currentRange() < CF_RANGE_LOWER) {
            div += CF_RANGE_LOWER - thread.currentRange();
        }
        if (thread.currentRange() > CF_RANGE_HIGHER) {
            div += thread.currentRange() - CF_RANGE_HIGHER;
        }
        return 100.0 / div;
    }

    public static double dominantCount(MusicThread thread) {

        var count = thread.getCf().getMelody().stream()
                .filter(node -> Objects.equals(node, thread.getCf().getDominant()))
                .count();
        return 100.0 / (Math.abs(DOMINANT_COUNT - count) + 1);
    }

    public static double length(MusicThread thread) {

        var length = thread.getCf().size();
        if (length < CF_LENGTH_LOWER) {
            return 100.0 / (CF_LENGTH_LOWER - length + 1);
        }
        if (length > CF_LENGTH_HIGHER) {
            return 100.0 / (length - CF_LENGTH_HIGHER + 1);
        }
        return 100.0;
    }

    public static double rate(MusicThread thread) {

        return (range(thread) + dominantCount(thread) + length(thread)) / 3.0;
    }
}
