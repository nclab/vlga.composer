/*
 * Copyright 2022 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music.utils.musicxml;

import art.cctcc.music.ga.Pitch;
import art.cctcc.music.ga.Individual;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.nio.file.Path;
import javax.xml.datatype.DatatypeFactory;
import org.audiveris.proxymusic.BarStyle;
import org.audiveris.proxymusic.ClefSign;
import org.audiveris.proxymusic.GroupBarlineValue;
import org.audiveris.proxymusic.GroupSymbolValue;
import org.audiveris.proxymusic.ObjectFactory;
import org.audiveris.proxymusic.PartList;
import org.audiveris.proxymusic.RightLeftMiddle;
import org.audiveris.proxymusic.ScorePart;
import org.audiveris.proxymusic.ScorePartwise;
import org.audiveris.proxymusic.StartStop;
import org.audiveris.proxymusic.Step;
import org.audiveris.proxymusic.TiedType;
import org.audiveris.proxymusic.TimeSymbol;
import org.audiveris.proxymusic.TypedText;
import org.audiveris.proxymusic.UprightInverted;
import org.audiveris.proxymusic.YesNo;
import org.audiveris.proxymusic.util.Marshalling;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class IndividualScore {

  private static final ObjectFactory OF = new ObjectFactory();

  private final ScorePartwise score_partwise;
  private final PartList part_list;

  private static final TimeSymbol TIME_SYMBOL = TimeSymbol.CUT;
  private static final int CF_DURATION = 4;
  private static final String INSTRUMENT_NAME = "Church Organ";
  private static final int MIDI_PROGRAM = 20;

  private static final String BEAT_UNIT = "whole";
  private static final Integer PER_MINUTE = 52;

  public IndividualScore(String title, String subtitle, String composer, Individual idv) {

    score_partwise = OF.createScorePartwise();

    var work = OF.createWork();
    work.setWorkTitle(title);
    work.setWorkNumber(subtitle);
    score_partwise.setWork(work);

    var identification = OF.createIdentification();
    score_partwise.setIdentification(identification);
    identification.setEncoding(OF.createEncoding());
    var creator = new TypedText();
    identification.getCreator().add(creator);
    creator.setValue(composer);
    creator.setType("composer");
    var today =
        DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(Instant.now().toString());
    var encoding_date = OF.createEncodingEncodingDate(today);
    identification.getEncoding().getEncodingDateOrEncoderOrSoftware().add(encoding_date);

    part_list = OF.createPartList();
    score_partwise.setPartList(part_list);

    var parts =
        IntStream.range(0, Individual.VOICE.length())
            .mapToObj(
                i -> {
                  var voice = Individual.VOICE.charAt(i);
                  var clef =
                      switch (voice) {
                        case 'S' -> XmlClef.G2;
                        case 'A' -> XmlClef.C3;
                        case 'T' -> XmlClef.C4;
                        case 'B' -> XmlClef.F4;
                        default -> null;
                      };
                  return new XmlPart("P" + (i + 1), "" + voice, "" + voice, clef, idv.getMelody(i));
                })
            .toList();
    this.addParts(parts);
  }

  private void addParts(List<XmlPart> parts) {

    var part_group_start = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_start);
    part_group_start.setNumber("1");
    part_group_start.setType(StartStop.START);
    var group_symbol = OF.createGroupSymbol();
    part_group_start.setGroupSymbol(group_symbol);
    group_symbol.setValue(GroupSymbolValue.BRACKET);
    var group_barline = OF.createGroupBarline();
    part_group_start.setGroupBarline(group_barline);
    group_barline.setValue(GroupBarlineValue.YES);

    for (var part : parts) {
      var score_part = _createPart(part);
      part_list.getPartGroupOrScorePart().add(score_part);
      _addMelody(score_part, part.getMelody(), part.getClef());
    }

    var part_group_stop = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_stop);
    part_group_stop.setNumber("1");
    part_group_stop.setType(StartStop.STOP);
  }

  private ScorePart _createPart(XmlPart part) {

    var inst_id = part.getId() + "-" + part.getId().replace("P", "I");
    var score_part = OF.createScorePart();
    score_part.setId(part.getId());

    var part_name = OF.createPartName();
    score_part.setPartName(part_name);
    part_name.setValue(part.getPart_name());

    var name_display = OF.createNameDisplay();
    score_part.setPartNameDisplay(name_display);
    var display_text = OF.createFormattedText();
    name_display.getDisplayTextOrAccidentalText().add(display_text);
    display_text.setValue(part.getPart_name().replace("-", "\n"));
    display_text.setFontSize("10");

    var part_abbr = OF.createPartName();
    score_part.setPartAbbreviation(part_abbr);
    part_abbr.setValue(part.getPart_abbreviation());

    var abbr_display = OF.createNameDisplay();
    score_part.setPartAbbreviationDisplay(abbr_display);
    var abbr_display_text = OF.createFormattedText();
    abbr_display.getDisplayTextOrAccidentalText().add(abbr_display_text);
    abbr_display_text.setValue(part.getPart_abbreviation());
    abbr_display_text.setFontSize("10");

    var score_instrument = OF.createScoreInstrument();
    score_instrument.setId(inst_id);
    score_instrument.setInstrumentName(INSTRUMENT_NAME);
    score_part.getScoreInstrument().add(score_instrument);

    var midi_instrument = OF.createMidiInstrument();
    midi_instrument.setId(score_instrument);
    midi_instrument.setMidiChannel(Integer.valueOf(part.getId().replace("P", "")));
    midi_instrument.setMidiProgram(MIDI_PROGRAM);
    score_part.getMidiDeviceAndMidiInstrument().add(midi_instrument);

    return score_part;
  }

  private void _addMelody(ScorePart score_part, List<Integer> melody, XmlClef xml_clef) {

    var part = OF.createScorePartwisePart();
    score_partwise.getPart().add(part);
    part.setId(score_part);

    var measure_no = 0;

    var bar = melody.size();
    var start_measure = measure_no + 1;
    var end_measure = measure_no + bar;

    for (int i = 0; i < bar; i++) {
      var measure = OF.createScorePartwisePartMeasure();
      part.getMeasure().add(measure);
      measure.setNumber(++measure_no + "");

      if (measure_no == 1) {
        var attributes = OF.createAttributes();
        measure.getNoteOrBackupOrForward().add(attributes);

        // indicates how many divisions per quarter note are used to indicate a note's duration.
        attributes.setDivisions(BigDecimal.valueOf(1));

        var key = OF.createKey();
        attributes.getKey().add(key);
        key.setFifths(BigInteger.ZERO);

        var time = OF.createTime();
        attributes.getTime().add(time);
        time.setSymbol(TIME_SYMBOL);
        time.getTimeSignature().add(OF.createTimeBeats("2"));
        time.getTimeSignature().add(OF.createTimeBeatType("2"));

        var clef = OF.createClef();
        attributes.getClef().add(clef);
        clef.setSign(ClefSign.valueOf(xml_clef.getSign().name()));
        clef.setLine(BigInteger.valueOf(xml_clef.getLine()));
        if (xml_clef.getClefOctaveChange() != 0)
          clef.setClefOctaveChange(BigInteger.valueOf(xml_clef.getClefOctaveChange()));

        if ("P1".equals(score_part.getId())) {
          var direction = OF.createDirection();
          measure.getNoteOrBackupOrForward().add(direction);
          var direction_type = OF.createDirectionType();
          direction.getDirectionType().add(direction_type);
          var metronome = OF.createMetronome();
          direction_type.setMetronome(metronome);
          metronome.getBeatUnit().add(BEAT_UNIT);
          var per_minute = OF.createPerMinute();
          metronome.setPerMinute(per_minute);
          per_minute.setValue(PER_MINUTE.toString());

          //          var sound = OF.createSound(); // Not required for MuseScore 2
          //          measure.getNoteOrBackupOrForward().add(sound);
          //          sound.setTempo(BigDecimal.valueOf(PER_MINUTE * 4));
        }
      } else if (measure_no == start_measure) {
        var print = OF.createPrint();
        measure.getNoteOrBackupOrForward().add(print);
        print.setNewSystem(YesNo.YES);
      }

      var vl_pitch = Pitch.values()[melody.get(i)];
      var note = OF.createNote();
      measure.getNoteOrBackupOrForward().add(note);
      note.setDuration(BigDecimal.valueOf(CF_DURATION));
      var pitch = OF.createPitch();
      note.setPitch(pitch);
      pitch.setStep(Step.valueOf(vl_pitch.getStep()));
      pitch.setOctave(vl_pitch.getOctave());
      var type = OF.createNoteType();
      note.setType(type);
      var notetype = "whole";
      type.setValue(notetype);

      var req_tied = i > 0 && vl_pitch.equals(Pitch.values()[melody.get(i - 1)]);

      //      switch (cpt_pitch.getAccidental()) {
      //        case "sharp" -> {
      //          pitch.setAlter(BigDecimal.ONE);
      //          var accidental = OF.createAccidental();
      //          note.setAccidental(accidental);
      //          accidental.setValue(AccidentalValue.SHARP);
      //        }
      //        case "flat" -> {
      //          pitch.setAlter(BigDecimal.valueOf(-1));
      //          var accidental = OF.createAccidental();
      //          note.setAccidental(accidental);
      //          accidental.setValue(AccidentalValue.FLAT);
      //        }
      //        default -> {
      //          if (IntStream.range(0, i)
      //                  .mapToObj(melody::get)
      //                  .map(CptPitchNode::getPitch)
      //                  .filter(Objects::nonNull)
      //                  .anyMatch(p -> p.getNatural().equals(cpt_pitch) &&
      // !p.getAccidental().isBlank())) {
      //            var accidental = OF.createAccidental();
      //            note.setAccidental(accidental);
      //            accidental.setValue(AccidentalValue.NATURAL);
      //          }
      //        }
      //      }
      var notations = OF.createNotations();
      note.getNotations().add(notations);
      if (req_tied) {
        var tie = OF.createTie();
        note.getTie().add(tie);
        tie.setType(StartStop.STOP);
        var tied = OF.createTied();
        tied.setType(TiedType.STOP);
        notations.getTiedOrSlurOrTuplet().add(tied);
      }
      if (i + 1 < melody.size() && vl_pitch.equals(Pitch.values()[melody.get(i + 1)])) {
        var tie = OF.createTie();
        note.getTie().add(tie);
        tie.setType(StartStop.START);
        var tied = OF.createTied();
        tied.setType(TiedType.START);
        notations.getTiedOrSlurOrTuplet().add(tied);
      }
      if (measure_no == end_measure) {
        var fermata = OF.createFermata();
        if ("P2".equals(score_part.getId())) {
          fermata.setType(UprightInverted.INVERTED);
          fermata.setDefaultY(BigDecimal.valueOf(-65));
        } else {
          fermata.setType(UprightInverted.UPRIGHT);
          fermata.setDefaultY(BigDecimal.valueOf(5));
        }
        notations.getTiedOrSlurOrTuplet().add(fermata);
      }
      var barline = OF.createBarline();
      measure.getNoteOrBackupOrForward().add(barline);
      barline.setLocation(RightLeftMiddle.RIGHT);
      var barstyle = OF.createBarStyleColor();
      barline.setBarStyle(barstyle);
      barstyle.setValue(measure_no == end_measure ? BarStyle.LIGHT_LIGHT : BarStyle.NONE);
    }
  }

  public void writeMusicXML(Path folder, String filename) {

    folder.toFile().mkdirs();
    var destination = folder.resolve(filename).toFile();
    try (var os = new FileOutputStream(destination)) {
      Marshalling.marshal(score_partwise, os, true, 2);
    } catch (Marshalling.MarshallingException | FileNotFoundException ex) {
      Logger.getLogger(IndividualScore.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(IndividualScore.class.getName())
          .log(java.util.logging.Level.SEVERE, null, ex);
    }
  }
}
