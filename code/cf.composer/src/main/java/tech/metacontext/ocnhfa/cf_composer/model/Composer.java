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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import static java.util.function.Predicate.not;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.impl.StandardGraph.FractionMode;
import tech.metacontext.ocnhfa.antsomg.model.*;
import tech.metacontext.ocnhfa.antsomg.impl.StandardMove;
import static tech.metacontext.ocnhfa.cf_composer.enums.EcclesiasticalMode.RANDOM;
import static tech.metacontext.ocnhfa.cf_composer.model.Parameters.*;
import tech.metacontext.ocnhfa.cf_composer.model.x.*;
import tech.metacontext.ocnhfa.cf_composer.model.y.*;
import tech.metacontext.ocnhfa.cf_composer.enums.*;
import tech.metacontext.ocnhfa.cf_composer.ex.*;

/**
 * Renaissance ContusFirmus Composer.
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class Composer implements AntsOMGSystem<MusicThread> {

    private String id;
    private String log_path;
    private Logger logger;

    private int thread_number = DEFAULT_THREAD_NUMBER;
    private EcclesiasticalMode ecclesiastical_mode = RANDOM;
    private String preset_source;

    private Map<String, Graph> graphs;
    private List<MusicThread> musicThreads;
    private int navigation_count; //navigation count
    private boolean toCadence;

    public static synchronized Composer getInstance() {

        return new Composer();
    }

    private Composer() {

    }

    public void setLogger(String id) {

        try {
            this.id = id;
            this.log_path = System.getProperty("user.dir") + File.separator
                    + "log" + File.separator + String.format("Composer-%s.log", this.id);
            this.logger = Logger.getLogger(log_path);
            var fh = new FileHandler(log_path);
            this.logger.addHandler(fh);
            var formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            this.logger.setUseParentHandlers(false);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(Composer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void init() {

        this.logger.log(Level.INFO, "Initializing RCFComposer, id = {0}", this.id);

        this.logger.log(Level.INFO, "Music thread number = {0}, ecclesiastical mode = {1}",
                new Object[]{this.thread_number, this.ecclesiastical_mode.name()});

        this.logger.log(Level.INFO, "Initializing MusicSpace...");
        init_graphs();

        this.logger.log(Level.INFO, "Initializing music threads...");
        init_population();
    }

    public void init(int thread_number, EcclesiasticalMode ecclesiastical_mode) {

        this.thread_number = thread_number;
        this.ecclesiastical_mode = ecclesiastical_mode;
        this.init();
    }

    public void init(Document doc, int thread_number) {

        this.thread_number = (thread_number > 0) ? thread_number
                : Integer.valueOf(doc.getRootElement().element("thread_number").getTextTrim());
        this.ecclesiastical_mode = EcclesiasticalMode.valueOf(doc.getRootElement().element("mode").getTextTrim());
        this.preset_source = doc.getRootElement().attributeValue("id");
        this.init();

        var x = doc.getRootElement().element("MusicSpace");
        x.elements("MusicPath").stream().forEach(e -> {
            var from = e.element("from").getTextTrim();
            var to = e.element("to").getTextTrim();
            var cost = Double.valueOf(e.element("cost").getTextTrim());
            var pheromoneTrail = Double.valueOf(e.element("pheromoneTrail").getTextTrim());
            var path = this.getX().queryByVertex(MusicThought.getNode(from)).stream()
                    .filter(p -> Objects.equals(p.getTo(), MusicThought.getNode(to)))
                    .findFirst().get();
            path.setCost(cost);
            path.setPheromoneTrail(pheromoneTrail);
        });
        var y = doc.getRootElement().element("PitchSpace");
        y.elements("PitchPath").stream().forEach(e -> {
            var from = e.element("from").getTextTrim().toUpperCase();
            var to = e.element("to").getTextTrim().toUpperCase();
            var cost = Double.valueOf(e.element("cost").getTextTrim());
            var pheromoneTrail = Double.valueOf(e.element("pheromoneTrail").getTextTrim());
            var path = this.getY().queryByVertex(Pitch.valueOf(from).getNode()).stream()
                    .filter(p -> Objects.equals(p.getTo(), Pitch.valueOf(to).getNode()))
                    .findFirst().get();
            path.setCost(cost);
            path.setPheromoneTrail(pheromoneTrail);
        });
    }

    public String asXML() {

        var doc = DocumentHelper.createDocument();
        var root = doc.addElement("Composer").addAttribute("id", this.id);
        if (Objects.nonNull(this.preset_source)) {
            root.addElement("preset_source").addText(this.preset_source);
        }
        root.addElement("thread_number").addText(String.valueOf(this.thread_number));
        root.addElement("mode").addText(this.ecclesiastical_mode.name());
        try {
            var x = DocumentHelper.parseText(this.getX().asXML()).getRootElement();
            var y = DocumentHelper.parseText(this.getY().asXML()).getRootElement();
            root.add(x);
            root.add(y);
        } catch (DocumentException ex) {
            Logger.getLogger(Composer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc.asXML();
    }

    @Override
    public void init_graphs() {

        this.graphs = new HashMap<>();
        this.graphs.put("x", new MusicSpace(X_ALPHA, X_BETA));
        this.graphs.put("y", new PitchSpace(Y_ALPHA, Y_BETA));
    }

    @Override
    public void init_population() {

        this.musicThreads = new ArrayList<>();
        for (var i = 0; i < this.thread_number; i++) {
            var mt = new MusicThread(this.ecclesiastical_mode, this.getX().getStart(), this.logger);
            this.musicThreads.add(mt);
        }
    }

    @Override
    public void navigate() {

        this.logger.log(Level.INFO, "*** navigating, navigation_count = {0}",
                navigation_count++);
        this.musicThreads.stream()
                .filter(not(MusicThread::isCompleted))
                .forEach(thread -> {
                    while (!nav_y(thread, nav_x(thread))) {
                    }
                    this.logger.log(Level.INFO, thread.toString());
                });
        evaporate();
        this.toCadence = navigation_count >= CF_LENGTH_LOWER;
    }

    private MusicThought nav_x(MusicThread thread)
            throws UnexpectedLocationException, UnexpectedMusicNodeException {

        if (!thread.getCurrentTrace().x.equals(this.getX().getStart())) {
            throw new UnexpectedLocationException(thread.getCurrentTrace());
        }
        var y = thread.getCurrentTrace().y;
        if (thread.getCf().size() >= 2 && thread.getCf().getMelody().getLast().getName().matches("[BF].")) {
            thread.setCurrentTrace(new MusicTrace(MusicThought.DIRECTIONAL, y));
            thread.setCurrentTrace(new MusicTrace(MusicThought.CONJUNCT, y));
            thread.setCurrentTrace(new MusicTrace(this.getX().getStart(), y));
            return MusicThought.Directional_Conjunct;
        }
        if (thread.getCf().size() >= 2 && thread.lastPitchPath().getInterval() > 3) {
            thread.setCurrentTrace(new MusicTrace(MusicThought.COMPLEMENTAL, y));
            thread.setCurrentTrace(new MusicTrace(MusicThought.SHORTTERM, y));
            thread.setCurrentTrace(new MusicTrace(this.getX().getStart(), y));
            return MusicThought.Complemental_ShortTerm;
        }

        var move1 = x_move(thread); //Start to Directional/Complemental
        var move2 = x_move(thread); //Directional/Complemental to Upward-Downward/ShortTerm-LongTerm
        MusicThought mt = MusicThought.getInstance(
                move1.getSelected().getTo(),
                move2.getSelected().getTo());
        if (Objects.isNull(mt)) {
            throw new UnexpectedMusicNodeException(
                    move1.getSelected().getTo(),
                    move2.getSelected().getTo());
        }
        var move3 = x_move(thread);
        if (!move3.getSelected().getTo().equals(this.getX().getStart())) {
            throw new UnexpectedLocationException(thread.getCurrentTrace());
        }
        thread.setCurrentTrace(new MusicTrace(move3, y));
        return mt;
    }

    private StandardMove<MusicPath> x_move(MusicThread thread) {

        var current_x = this.getX().move(thread.getCurrentTrace().x,
                X_PHEROMONE_DEPOSIT_AMOUNT, X_EXPLORE_CHANCE);
        thread.setCurrentTrace(new MusicTrace(current_x,
                thread.getCurrentTrace().y));
        return current_x;
    }

    private boolean nav_y(MusicThread thread, MusicThought mt) {

        this.logger.log(Level.INFO, "nav_y invoked with MusicThought = {0}", mt.name());
        if (this.toCadence) {
            var paths = this.getY().queryByVertex(thread.getCurrentTrace().y);
            var cadences = thread.getEcclesiastical_Mode().getCadences().stream()
                    .map(c -> new AbstractMap.SimpleEntry<>(c, paths.stream()
                    /**/.filter(p -> c.isLinked(p.getTo())).findAny().orElse(null)))
                    .filter(entry -> Objects.nonNull(entry.getValue()))
                    .filter(entry -> Pitch.diff(entry.getValue()) > -3)
                    .collect(Collectors.toList());
//            System.out.println("cadences = " + cadences);
            if (!cadences.isEmpty() && Math.random() > Parameters.Y_EXPLORE_CHANCE) {
                var selected = cadences.get(new Random().nextInt(cadences.size()));
                thread.addCadence(selected.getKey());
                return true;
            }
        }
        var current_y = this.getY().move(thread.getCurrentTrace().y,
                thread.getCf().getDominant(), mt.getPredicate(thread),
                Y_PHEROMONE_DEPOSIT_AMOUNT, Y_EXPLORE_CHANCE);
        if (Objects.nonNull(current_y)) {
            current_y.setMt(mt);
            thread.addPitchMove(current_y);
            return true;
        } else {
            this.logger.log(Level.WARNING, "Current MusicThought leads to no possibilities.");
        }
        return false;
    }

    @Override
    public void evaporate() {

        this.logger.log(Level.INFO, "evaporate...");
        this.getX().getEdges().forEach((p) -> {
            p.evaporate(X_PHEROMONE_EVAPORATE_RATE);
        });
        this.getY().getEdges().forEach((p) -> {
            p.evaporate(Y_PHEROMONE_EVAPORATE_RATE);
        });
    }

    @Override
    public boolean isAimAchieved() {

        return musicThreads.stream().allMatch(MusicThread::isCompleted);
    }

    /**
     *
     * @param project_dir
     * @return composer folder in File object.
     */
    public File saveComposer(String project_dir) {

        File parent = new File(project_dir, "Composer-" + this.id);
        parent.mkdirs();
        // write composer.xml
        try (var fw_composer = new FileWriter(new File(parent, "composer.xml"));
                var bw_composer = new BufferedWriter(fw_composer);) {
            bw_composer.write(this.asXML());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        // write graph_x.graphviz
        try (var fw_graph_x = new FileWriter(new File(parent, "graph_x.graphviz"));
                var bw_graph_x = new BufferedWriter(fw_graph_x);) {
            bw_graph_x.write(this.getX().asGraphviz());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        // write graph_y.graphviz
        try (var fw_graph_y = new FileWriter(new File(parent, "graph_y.graphviz"));
                var bw_graph_y = new BufferedWriter(fw_graph_y);) {
            bw_graph_y.write(this.getY().asGraphviz());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return parent;
    }

    /*
     * Default getters and setters.
     */
    public int getCount() {

        return navigation_count;
    }

    public void setCount(int count) {

        this.navigation_count = count;
    }

    @Override
    public List<MusicThread> getAnts() {

        return musicThreads;
    }

    public void setAnts(List<MusicThread> mts) {

        this.musicThreads = mts;
    }

    public boolean isToCadence() {

        return toCadence;
    }

    public void setToCadence(boolean toCadence) {

        this.toCadence = toCadence;
    }

    @Override
    public Map<String, Graph> getGraphs() {

        return this.graphs;
    }

    public MusicSpace getX() {

        return (MusicSpace) getGraphs().get("x");
    }

    public PitchSpace getY() {

        return (PitchSpace) getGraphs().get("y");
    }

    public List<MusicThread> getMusicThreads() {

        return musicThreads;
    }

    public void setMusicThreads(List<MusicThread> musicThreads) {

        this.musicThreads = musicThreads;
    }

    public int getNavigation_count() {

        return navigation_count;
    }

    public int getThread_number() {

        return thread_number;
    }

    public void setThread_number(int thread_number) {

        this.thread_number = thread_number;
    }

    public String getLog_path() {

        return this.log_path;
    }

    public String getId() {
        return id;
    }

    public void setFraction_mode(FractionMode fraction_mode) {

        this.getX().setFraction_mode(fraction_mode);
        this.getY().setFraction_mode(fraction_mode);
    }
}
