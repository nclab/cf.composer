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
package tech.metacontext.ocnhfa.cf_composer.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import tech.metacontext.ocnhfa.cf_composer.enums.EcclesiasticalMode;
import tech.metacontext.ocnhfa.cf_composer.musicxml.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Studio {

    public enum ComposerType {

        PRESET, PRESET_STATIC, STANDARD, MODAL;
    }

    static String LINE = "-".repeat(80) + "\n";
    public static File projects_dir = new File(System.getProperty("user.dir"), "projects");

    private String project;
    private String preset;
    private ComposerType composer_type;
    private int composer_number;
    private int thread_number;
    private int target_size;
    private EcclesiasticalMode ecclesiastical_mode;
    private FractionMode fraction_mode;

    public Studio(ComposerType composer_type) {

        this.composer_type = composer_type;
        this.target_size = 20;
    }

    public Studio() {

        this(ComposerType.MODAL);
    }

    public void run() {

        this.project = this.composer_type + "_project_" + LocalTime.now().toString().replaceAll("[:.]", "-");

        Supplier<Stream<Composer>> source = () -> switch (this.composer_type) {
            case PRESET_STATIC:
                Parameters.X_PHEROMONE_DEPOSIT_AMOUNT = 0.0;
                Parameters.X_PHEROMONE_EVAPORATE_RATE = 0.0;
                Parameters.Y_PHEROMONE_DEPOSIT_AMOUNT = 0.0;
                Parameters.Y_PHEROMONE_EVAPORATE_RATE = 0.0;
            case PRESET:
                yield preset_composers(this.preset, this.thread_number);
            case STANDARD:
                yield standard_composers(this.composer_number, this.ecclesiastical_mode, this.thread_number);
            case MODAL:
                yield modal_composers(this.thread_number);
        };
        // Generate composers from specified source and compose.
        var composers = source.get()
                .parallel()
                .peek(c -> {
                    c.setFraction_mode(this.fraction_mode);
                    while (!c.isAimAchieved()) {
                        c.navigate();
                    }
                }).collect(Collectors.toList());
        // Select qualified threads by composer
        var qualified_threads = composers.stream().map(
                c -> new SimpleEntry<>(
                        c,
                        c.getAnts().stream()
                                //.filter(MusicThreadConstrain.apply)
                                .sorted()
                                .limit(target_size)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
        // Calculate qualified rate etc. and preserve data to project folders.
        var project_dir = new File(Studio.projects_dir, this.project);
        var average = qualified_threads.stream()
                .peek(e -> System.out.println(LINE + "Composer: " + e.getKey().getId()))
                .peek(e -> {
                    System.out.println("Saving Composer and Cantus Firmi ...");
                    var composer_path = e.getKey().saveComposer(project_dir.getPath());
                    var cf_path = new File(composer_path, "cantus_firmus");
                    cf_path.mkdirs();
                    IntStream.range(0, e.getValue().size())
                            .peek(i -> System.out.printf("%2d.%s\n", i + 1, e.getValue().get(i)))
                            .forEach(i -> {
                                var filename = "cantus_firmus_" + i + ".xml";
                                e.getValue().get(i)
                                        .saveRoute(new File(cf_path, filename));
                            });
                })
                .peek(e -> System.out.println("Log saved in " + e.getKey().getLog_path()))
                .peek(e -> {
                    System.out.println("Saving Score ...");
                    var score = new Score(e.getKey().getId());
                    e.getValue().stream()
                            .forEach(thread -> {
                                var selected_clef = Clef.clef_selector(thread.getCf().getMiddle());
                                var measure = score.addMeasure(selected_clef);
                                thread.getCf().getMelody().stream()
                                        .forEach(node -> score.addNote(measure, node.getPitch()));
                            });
                    score.saveScore(new File(project_dir, composers.indexOf(e.getKey()) + "_score.musicxml"));
                })
                .peek(e -> System.out.printf("Qualified melodies = %d/%d\n", e.getValue().size(), e.getKey().getThread_number()))
                .mapToDouble(entry -> 100.0 * entry.getValue().size() / entry.getKey().getThread_number())
                .peek(rate -> System.out.printf("Qualified rate = %2.2f%%\n", rate))
                .average().getAsDouble();
        System.out.printf(LINE + "Average qualified rate from %d composers = %2.2f%%\n",
                composers.size(), average);
    }

    private Stream<Composer> preset_composers(String project_path, int thread_number) {

        System.out.println("Creating composers from preset ...");
        var path = new File(Studio.projects_dir, project_path);
        System.out.println("Searching Composers from " + path.getPath());
        var projects = path.listFiles(File::isDirectory);
        System.out.println(projects.length + " folder(s) located.");
        var composers = createComposers(projects.length);
        return IntStream.range(0, projects.length)
                .mapToObj(i -> {
                    var c = composers.get(i);
                    c.init(getComposerXML(projects[i]), thread_number);
                    return c;
                })
                .filter(Objects::nonNull);
    }

    public static Document getComposerXML(File directory) {

        File xml_source = new File(directory, "composer.xml");
        try ( var fr = new FileReader(xml_source);  var br = new BufferedReader(fr);) {
            System.out.println("Reading " + xml_source.getPath() + " ...");
            var doc = br.lines().collect(Collectors.joining(" "));
            return DocumentHelper.parseText(doc);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(Studio.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Stream<Composer> modal_composers(int thread_number) {

        System.out.println("Creating modal composers ...");
        var composers = createComposers(EcclesiasticalMode.values().length - 1);
        return IntStream.range(0, composers.size())
                .mapToObj(i -> {
                    var c = composers.get(i);
                    c.init(thread_number, EcclesiasticalMode.values()[i]);
                    return c;
                });
    }

    private Stream<Composer> standard_composers(int number, EcclesiasticalMode ecclesiastical_mode, int thread_number) {

        System.out.println("Creating standard composers ...");
        return createComposers(number).stream()
                .peek(c -> c.init(thread_number, ecclesiastical_mode));
    }

    private List<Composer> createComposers(int number) {

        var composers = Stream.generate(Composer::getInstance)
                .limit(number)
                .collect(Collectors.toList());
        return composers.stream()
                .peek(c -> c.setLogger(project + "_" + String.valueOf(composers.indexOf(c))))
                .collect(Collectors.toList());
    }

    public String getProject() {

        return this.project;
    }

    public void setProject(String project) {

        this.project = project;
    }

    public String getPreset() {

        return preset;
    }

    public Studio setPreset(String preset) {

        this.preset = preset;
        return this;
    }

    public ComposerType getComposer_type() {

        return composer_type;
    }

    public void setComposer_type(ComposerType composer_type) {

        this.composer_type = composer_type;
    }

    public int getComposer_number() {

        return composer_number;
    }

    public Studio setComposer_number(int composer_number) {

        this.composer_number = composer_number;
        return this;
    }

    public int getThread_number() {

        return thread_number;
    }

    public Studio setThread_number(int thread_number) {

        this.thread_number = thread_number;
        return this;
    }

    public EcclesiasticalMode getEcclesiastical_mode() {

        return this.ecclesiastical_mode;
    }

    public Studio setEcclesiastical_Mode(EcclesiasticalMode ecclesiastical_mode) {

        this.ecclesiastical_mode = ecclesiastical_mode;
        return this;
    }

    public int getTarget_size() {

        return target_size;
    }

    public Studio setTarget_size(int target_size) {

        this.target_size = target_size;
        return this;
    }

    public FractionMode getFraction_mode() {

        return fraction_mode;
    }

    public Studio setFraction_mode(FractionMode fraction_mode) {

        this.fraction_mode = fraction_mode;
        return this;
    }

}
