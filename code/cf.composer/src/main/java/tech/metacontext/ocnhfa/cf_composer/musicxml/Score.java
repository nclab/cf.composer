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
package tech.metacontext.ocnhfa.cf_composer.musicxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import tech.metacontext.ocnhfa.cf_composer.enums.Pitch;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Score {

//    public final String filename;
    public final Document doc;
    public final Element part;
    public final List<Element> measures;

    public Score(String composer) {

//        this.filename = composer;
        this.doc = DocumentHelper.createDocument();
        this.doc.addDocType("score-partwise",
                "-//Recordare//DTD MusicXML 3.1 Partwise//EN",
                "http://www.musicxml.org/dtds/partwise.dtd");
        this.doc.setXMLEncoding("UTF-8");
        var root = this.doc.addElement("score-partwise")
                .addAttribute("version", "3.1");
        root.addElement("movement-title").addText(composer);
        var part_list = root.addElement("part-list");
        var score_part = part_list.addElement("score-part")
                .addAttribute("id", "P1");
        score_part.addElement("part-name")
                .addText("cf");
        this.part = root.addElement("part")
                .addAttribute("id", "P1");
        this.measures = new ArrayList<>();
    }

    public Element addMeasure(Clef selected_clef) {

        var measure = part.addElement("measure")
                .addAttribute("number", "1");
        var attributes = measure.addElement("attributes");
        attributes.addElement("divisions").setText("1");
        attributes.addElement("key")
                .addElement("fifths").addText("0");
        var clef = attributes.addElement("clef");
        clef.addElement("sign").addText(selected_clef.sign);
        clef.addElement("line").addText(selected_clef.line);
        measure.addElement("barline")
                .addAttribute("location", "right")
                .addElement("bar-style").addText("light-light");
        this.measures.add(measure);
        return measure;
    }

    public void addNote(Element measure, Pitch node_pitch) {

        String step = String.valueOf(node_pitch.name().charAt(0));
        int octave = Integer.valueOf(String.valueOf(node_pitch.name().charAt(1)));
        var note = measure.addElement("note");
        var pitch = note.addElement("pitch");
        pitch.addElement("step").addText(step);
        pitch.addElement("octave").addText(String.valueOf(octave));
        note.addElement("duration").addText(String.valueOf(4));
        note.addElement("type").addText("whole");
    }

    public void saveScore(File score_path) {

//        var score_path = new File(parent, String.format("Composer-%s.musicxml", this.filename));
        try (var fw = new FileWriter(score_path);
                var bw = new BufferedWriter(fw);) {
            bw.write(this.doc.asXML());
        } catch (IOException ex) {
            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
