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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dom4j.DocumentHelper;
import tech.metacontext.ocnhfa.antsomg.model.Ant;
import tech.metacontext.ocnhfa.cf_composer.enums.*;
import tech.metacontext.ocnhfa.cf_composer.constrains.MusicThreadRating;
import tech.metacontext.ocnhfa.cf_composer.model.devices.*;
import tech.metacontext.ocnhfa.cf_composer.model.x.MusicNode;
import tech.metacontext.ocnhfa.cf_composer.model.y.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public final class MusicThread implements Ant<MusicTrace>, Comparable<MusicThread> {

    private EcclesiasticalMode ecclesiastical_mode;
    private CantusFirmus cf;
    private MusicTrace currentTrace;
    private List<MusicTrace> route;
    private boolean completed;
    /**
     * The chance the ant strays away from the path determined by pheromone
     */
    private double exploreChance;
    private double pheromoneDeposit;

    public MusicThread(EcclesiasticalMode ecclesiastical_mode, MusicNode start, Logger logger) {

        this.ecclesiastical_mode = (ecclesiastical_mode == EcclesiasticalMode.RANDOM)
                ? EcclesiasticalMode.getRandomMode() : ecclesiastical_mode;
        this.cf = new CantusFirmus(this.ecclesiastical_mode);
        this.setCurrentTrace(new MusicTrace(start, this.cf.getFinalis()));
        this.route = new ArrayList<>();
        logger.log(Level.INFO, "MusicThought in {0}, starting at {1}, created.",
                new Object[]{this.ecclesiastical_mode.name(), this.cf.getFinalis().getPitch()});
    }

    public MusicThread(MusicNode start, Logger logger) {

        this(EcclesiasticalMode.getRandomMode(), start, logger);
    }

    @Override
    public void addCurrentTraceToRoute() {

        this.route.add(this.currentTrace);
    }

    public PitchPath lastPitchPath() {

        if (this.cf.size() < 2) {
            return null;
        }
        return new PitchPath(
                this.cf.getMelody().get(this.cf.size() - 2),
                this.cf.getMelody().getLast(), 0.0);
    }

    public int lastPitchDirection() {

        if (this.cf.size() < 2) {
            return 0;
        }
        return Pitch.diff(lastPitchPath());
    }

    /**
     * return the position of the last pitch in overall range.
     *
     * @return int: 0 if right in the middle, positive if higher than the
     * middle, vice versa.
     */
    public int lastPitchLevel() {

        return Pitch.diff(cf.getMiddle().getNode(), cf.getMelody().getLast());
    }

    public int currentRange() {

        var summary = cf.getMelody().stream()
                .mapToInt(pn -> pn.getPitch().ordinal())
                .summaryStatistics();
        return summary.getMax() - summary.getMin() + 1;
    }

    public void addPitchMove(PitchMove pm) {

        this.cf.add(pm);
        this.setCurrentTrace(new MusicTrace(this.currentTrace.x, pm.getSelected().getTo()));
    }

    public void addByPitches(PitchNode... pitches) {

        var current = new AtomicReference<PitchNode>(this.cf.getMelody().getLast());
        Stream.of(pitches)
                .map(pitch -> {
                    var path = new PitchPath(current.get(), pitch, 0.0);
                    current.set(pitch);
                    return new PitchMove(false, List.of(path), path, MusicThought.NULL);
                })
                .peek(cf::add)
                .map(PitchMove::getSelected)
                .map(PitchPath::getTo)
                .map(pn -> new MusicTrace(this.currentTrace.x, pn))
                .forEach(loc -> this.setCurrentTrace(loc));
    }

    public void addCadence(Cadence cadence) {

        this.addByPitches(cadence.getFormula().toArray(new PitchNode[0]));
        this.completed = true;
    }

    public void saveRoute(File file) {

        var doc = DocumentHelper.createDocument();
        var root = doc.addElement("MusicThread");
        root.addElement("mode")
                .addText(this.ecclesiastical_mode.name());
        root.addElement("CantusFirmus")
                .addText(this.getCf().toString());
        root.addElement("middle")
                .addText(this.getCf().getMiddle().name());
        var rating = root.addElement("rating")
                .addAttribute("rate", String.valueOf(MusicThreadRating.rate(this)));
        rating.addElement("range")
                .addText(String.valueOf(MusicThreadRating.range(this)));
        rating.addElement("length")
                .addText(String.valueOf(MusicThreadRating.length(this)));
        rating.addElement("dominantCount")
                .addText(String.valueOf(MusicThreadRating.dominantCount(this)));
        var history = root.addElement("history")
                .addAttribute("length", String.valueOf(this.cf.size()));
        IntStream.range(1, this.cf.size())
                .forEach(i -> {
                    var ph = this.cf.getHistory().get(i);
                    var pitch_route = history.addElement("PitchHistory")
                            .addAttribute("move", String.valueOf(i));
                    pitch_route.addElement("to")
                            .addText(ph.getSelected().getTo().getName());
                    pitch_route.addElement("MusicThought")
                            .addText(ph.getMt().name());
                    pitch_route.addElement("exploit")
                            .addText(String.valueOf(!ph.isExploring()));
                    var routes = pitch_route.addElement("routes");
                    ph.getPheromoneRecords().entrySet().stream()
                            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                            .forEach(entry -> {
                                var route = routes.addElement("route");
                                route.addElement("to").addText(entry.getKey().getTo().getName());
                                route.addElement("pheromoneTrail").addText(String.valueOf(entry.getValue()));
                            });
                });
        try (var fw = new FileWriter(file);
                var bw = new BufferedWriter(fw);) {
            bw.write(doc.asXML());
        } catch (IOException ex) {
            Logger.getLogger(MusicThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {

        return cf.toString()
                + String.format("(%.1f, %.1f, %.1f -> %.1f)",
                        MusicThreadRating.range(this),
                        MusicThreadRating.dominantCount(this),
                        MusicThreadRating.length(this),
                        MusicThreadRating.rate(this));
    }

    /*
     * Default getters and setters.
     */
    @Override
    public List<MusicTrace> getRoute() {

        return this.route;
    }

    public void setHistory(List<MusicTrace> history) {

        this.route = history;
    }

    public double getExploreRate() {

        return exploreChance;
    }

    public void setExploreChance(double exploreChance) {

        this.exploreChance = exploreChance;
    }

    public EcclesiasticalMode getEcclesiastical_Mode() {

        return ecclesiastical_mode;
    }

    public void setMode(EcclesiasticalMode ecclesiastical_mode) {

        this.ecclesiastical_mode = ecclesiastical_mode;
    }

    public double getPheromoneDeposit() {

        return pheromoneDeposit;
    }

    public void setPheromoneDeposit(double pheromoneDeposit) {

        this.pheromoneDeposit = pheromoneDeposit;
    }

    @Override
    public MusicTrace getCurrentTrace() {

        return currentTrace;
    }

    @Override
    public void setCurrentTrace(MusicTrace currentLocation) {

        if (Objects.nonNull(this.currentTrace)) {
            this.addCurrentTraceToRoute();
        }
        this.currentTrace = currentLocation;
    }

    public boolean isCompleted() {

        return completed;
    }

    public void setCompleted(boolean completed) {

        this.completed = completed;
    }

    public CantusFirmus getCf() {

        return cf;
    }

    @Override
    public int compareTo(MusicThread o) {

        return Double.compare(MusicThreadRating.rate(o), MusicThreadRating.rate(this));
    }

}
