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
package tech.metacontext.ocnhfa.cf_composer;

import static java.util.Objects.isNull;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import tech.metacontext.ocnhfa.cf_composer.model.Studio;
import tech.metacontext.ocnhfa.cf_composer.model.Studio.ComposerType;
import tech.metacontext.ocnhfa.cf_composer.enums.EcclesiasticalMode;
import static tech.metacontext.ocnhfa.cf_composer.model.Parameters.*;
import static tech.metacontext.ocnhfa.cf_composer.model.Studio.ComposerType.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Main {

    static String default_project = "STANDARD_project_00-48-00-571338900";

    /**
     * main entry of cf.composer.
     *
     * @param args <br>
     * <b>COMMON</b>: THREAD_NUMBER, TARGET_SIZE, FRACTION_MODE;<br>
     * <b>TYPE-STANDARD</b>: COMPOSER_NUMBER, MODE;<br>
     * <b>TYPE-PRESET/PRESET_STATIC</b>: PROJECT;<br>
     * <b>TYPE-MODAL</b>: None.<br>
     */
    public static void main(String[] args) {

        var params = Stream.of(args)
                .map(arg -> arg.split("="))
                .filter(s -> s.length == 2)
                .peek(s -> System.out.printf("%s=%s\n", s[0], s[1]))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        var type = getParam(params.get("TYPE"),
                PRESET_STATIC, ComposerType::valueOf);

        var thread_number = getParam(params.get("THREAD_NUMBER"),
                type.equals(STANDARD) ? DEFAULT_THREAD_NUMBER * 100 : DEFAULT_THREAD_NUMBER,
                Integer::valueOf);

        var composer_number = getParam(params.get("COMPOSER_NUMBER"),
                DEFAULT_COMPOSER_NUMBER, Integer::valueOf);

        var project = getParam(params.get("PROJECT"),
                default_project, String::valueOf);

        var target_size = getParam(params.get("TARGET_SIZE"),
                DEFAULT_TARGET_SIZE, Integer::valueOf);

        var ecclesiastical_mode = getParam(params.get("MODE"),
                EcclesiasticalMode.RANDOM, EcclesiasticalMode::valueOf);

        var fraction_type = getParam(params.get("FRACTION_MODE"),
                DEFAULT_FRACTION_MODE, FractionMode::valueOf);

        var studio = new Studio(type)
                .setThread_number(thread_number)
                .setTarget_size(target_size)
                .setFraction_mode(fraction_type);
        switch (type) {
            case STANDARD->
                studio.setComposer_number(composer_number)
                        .setEcclesiastical_Mode(ecclesiastical_mode);
            case PRESET,PRESET_STATIC->
                studio.setPreset(project);
        }
        studio.run();
    }

    public static <T> T getParam(String key, T default_value, Function<String, T> function) {

        return isNull(key) ? default_value : function.apply(key);
    }

}
