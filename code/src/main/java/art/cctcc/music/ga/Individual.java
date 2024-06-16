/*
 * Copyright 2024 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music.ga;

import static art.cctcc.music.Settings.*;
import art.cctcc.music.utils.musicxml.IndividualScore;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.commons.math3.util.Pair;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Individual extends AbstractListChromosome<Integer> {

  public static String VOICE = "SATB";
  public static boolean TRACING;
  public static List<Evaluation> EVALS = List.of(Evaluation.values());

  private String progression;
  private String series;

  public Individual(int chord_no) {

    super(Individual.getRandomChromosome(chord_no));
  }

  public Individual(List<Integer> representation) throws InvalidRepresentationException {

    super(representation);
  }

  @Override
  public AbstractListChromosome<Integer> newFixedLengthChromosome(
      List<Integer> chromosomeRepresentation) {

    return new Individual(chromosomeRepresentation);
  }

  @Override
  public double fitness() {

    var fitness =
        1.0
            - EVALS.stream()
                .map(e -> Pair.create(e, e.evaluate(this)))
                .filter(p -> p.getValue() > 0)
                .mapToDouble(Pair::getValue)
                .sum();

    return fitness;
  }

  public String getProgression() {

    if (this.progression == null)
      this.progression =
          String.format(
              "[%s]",
              IntStream.range(0, getChordNumber())
                  .mapToObj(this::getChord)
                  .map(ords -> "" + Pitch.triadChordTest(ords) + Pitch.seventhChordTest(ords))
                  .map(str -> str.replaceFirst("X", ""))
                  .collect(Collectors.joining()));
    return this.progression;
  }

  public String getSeries() {

    if (this.series == null)
      this.series =
          String.format(
              "[%s]",
              IntStream.range(0, getChordNumber())
                  .mapToObj(this::getChord)
                  .map(
                      c ->
                          Pitch.triadChordTest(c) == 'X'
                              ? (Pitch.seventhChordTest(c) == 'X'
                                  ? "X"
                                  : (Pitch.isRootPosition(c) ? "S" : "s"))
                              : (Pitch.isRootPosition(c) ? "T" : "t"))
                  .collect(Collectors.joining()));
    return this.series;
  }

  private List<List<Integer>> chord_numbers;

  public List<List<Integer>> getFiguredNumerals() {

    if (this.chord_numbers == null)
      this.chord_numbers =
          IntStream.range(0, this.getChordNumber())
              .mapToObj(this::getChord)
              .map(Pitch::getNumbers)
              .map(set -> set.stream().sorted(Comparator.reverseOrder()).toList())
              .toList();
    return this.chord_numbers;
  }

  /**
   * Check if all chord notes are in their respective VOICE registers.
   *
   * @param chromosomeRepresentation
   * @throws InvalidRepresentationException
   */
  @Override
  protected void checkValidity(List<Integer> chromosomeRepresentation)
      throws InvalidRepresentationException {

    for (int i = 0; i < chromosomeRepresentation.size(); i++) {
      var v = i % VOICE.length();
      var p = chromosomeRepresentation.get(i);
      var register = Pitch.getRegister(VOICE.charAt(v));
      if (!Pitch.isInRegister(VOICE.charAt(v), p))
        throw new InvalidRepresentationException(
            LocalizedFormats.ARGUMENT_OUTSIDE_DOMAIN,
            VOICE.charAt(v) + ":" + Pitch.values()[p],
            register[0],
            register[1]);
    }
  }

  /**
   * Generate random chromosome representing chord number specified and predefined VOICE types.
   *
   * @param chord_no chord number specified, resulting in a chromosome with length of chord number
   *     times VOICE number.
   * @return encoded integer list representing generated chord series.
   */
  public static List<Integer> getRandomChromosome(int chord_no) {

    return Stream.generate(() -> IntStream.range(0, VOICE.length()))
        .limit(chord_no)
        .flatMap(s -> s.mapToObj(VOICE::charAt))
        .map(Pitch::getNote)
        .toList();
  }

  /**
   * Get total chord number.
   *
   * @return chord number.
   */
  public int getChordNumber() {

    return this.getLength() / VOICE.length();
  }

  /**
   * Get the melody of specified VOICE.
   *
   * @param v VOICE index specified (starting from 0).
   * @return an integer list representing requested melody.
   */
  public List<Integer> getMelody(int v) {

    return IntStream.range(0, this.getLength() / VOICE.length())
        .mapToObj(c -> this.getRepresentation().get(c * VOICE.length() + v))
        .toList();
  }

  /**
   * Get the chord of specified position.
   *
   * @param pos chord position starting from 0.
   * @return a integer list representing requested chord.
   */
  public List<Integer> getChord(int pos) {

    return IntStream.range(0, VOICE.length())
        .mapToObj(i -> this.getRepresentation().get(pos * VOICE.length() + i))
        .toList();
  }

  public List<Integer> getLastChord() {

    return this.getChord(this.getChordNumber() - 1);
  }

  @Override
  public List<Integer> getRepresentation() {

    return super.getRepresentation();
  }

  @Override
  public int hashCode() {

    int hash = 3;
    hash = 29 * hash + this.getRepresentation().hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Individual other = (Individual) obj;
    return this.getRepresentation().equals(other.getRepresentation());
  }

  @Override
  public String toString() {

    var result =
        '['
            + IntStream.range(0, VOICE.length())
                .mapToObj(i -> " " + VOICE.charAt(i))
                .collect(Collectors.joining(", "))
            + "]\n";
    result +=
        IntStream.range(0, this.getChordNumber())
                .mapToObj(c -> this.getChord(c))
                .map(Pitch::translateO2P)
                .map(List::toString)
                .collect(Collectors.joining("\n"))
            + "\n";
    result += "-".repeat(VOICE.length() * 4) + "\n";
    result += "Harmonic Progression:\n" + this.toRomanNumerals(false);
    return result;
  }

  public String toRomanNumerals(boolean latex) {

    if (latex)
      return IntStream.range(0, this.getChordNumber())
          .mapToObj(this::getLaTeXFigure)
          .collect(Collectors.joining("\n"));

    return IntStream.range(0, this.getChordNumber())
        .mapToObj(this::getTextFigure)
        .collect(Collectors.joining("  "));
  }

  public static final String TEXT_FORMAT = "%s_%s";

  public static final String FIGURE_FORMAT = "$\\myChord{%s}{%s}{%s}{%s}$";

  public String getTextFigure(int i) {

    var symbol = this.getProgression().charAt(i + 1);
    var roman = RomanNumeral.get(Character.toUpperCase(symbol));
    if ("-".equals(roman)) return "-";
    var numbers = Set.copyOf(this.getFiguredNumerals().get(i));

    String figure = roman.toUpperCase();

    if (numbers.contains(7)) figure += "_7";
    else if (numbers.containsAll(List.of(6, 5))) {
      figure += "_6/5";
    } else if (numbers.containsAll(List.of(4, 3))) {
      figure += "_4/3";
    } else if (numbers.contains(2)) {
      figure += "_4/2";
    } else if (numbers.containsAll(List.of(6, 4))) {
      figure += "_6/4";
    } else if (numbers.containsAll(List.of(6, 3))) figure += "_6";

    return figure;
  }

  public String getLaTeXFigure(int i) {

    var symbol = this.getProgression().charAt(i + 1);
    var roman = RomanNumeral.get(Character.toUpperCase(symbol));
    if ("-".equals(roman)) return "-";
    var numbers = Set.copyOf(this.getFiguredNumerals().get(i));

    Object[] figure = {roman, "\\ ", "\\ ", "\\ "};

    switch (symbol) {
      case 'B':
        figure[1] = "\\circ";
        break;
      case 'b':
        figure[1] = "\\textrm{\\diameter}";
        break;
      default:
        figure[2] = "\\ ";
        break;
    }

    if (numbers.contains(7)) figure[3] = "7";
    else if (numbers.containsAll(List.of(6, 5))) {
      figure[2] = "6";
      figure[3] = "5";
    } else if (numbers.containsAll(List.of(4, 3))) {
      figure[2] = "4";
      figure[3] = "3";
    } else if (numbers.contains(2)) {
      figure[2] = "4";
      figure[3] = "2";
    } else if (numbers.containsAll(List.of(6, 4))) {
      figure[2] = "6";
      figure[3] = "4";
    } else if (numbers.containsAll(List.of(6, 3))) figure[3] = "6";

    return String.format(FIGURE_FORMAT, figure);
  }

  public void saveScore(Path folder, String filename, String composer) {

    new IndividualScore(
            "Exploring Voice-Leading with GA",
            String.format("%s x %dmm.", VOICE, this.getChordNumber()),
            composer,
            this)
        .writeMusicXML(folder, filename);
  }
}
