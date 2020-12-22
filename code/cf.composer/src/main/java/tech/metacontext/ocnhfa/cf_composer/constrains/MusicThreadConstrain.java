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
import java.util.function.Predicate;
import static tech.metacontext.ocnhfa.cf_composer.model.Parameters.*;
import tech.metacontext.ocnhfa.cf_composer.model.MusicThread;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MusicThreadConstrain implements Predicate<MusicThread> {

    public static boolean range(MusicThread thread) {

        return thread.currentRange() <= 8 && thread.currentRange() >= 4;
    }

    public static boolean dominantCount(MusicThread thread) {

        return thread.getCf().getMelody().stream()
                .filter(node -> Objects.equals(node, thread.getCf().getDominant()))
                .count() >= DOMINANT_COUNT;
    }

    public static boolean length(MusicThread thread) {

        return thread.getCf().size() <= CF_LENGTH_LOWER * 2;
    }

    @Override
    public boolean test(MusicThread t) {

        return range(t) && dominantCount(t) && length(t);
    }

}
