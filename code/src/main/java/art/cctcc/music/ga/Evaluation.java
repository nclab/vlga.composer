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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Evaluation {

  /** Evaluate if a melody is easy to sing. */
  MelodicSmoothness(
      PenaltyMelodicSmoothness,
      idv ->
          IntStream.range(0, Individual.VOICE.length())
              .map(i -> melodicInfeasibilityCount(Individual.VOICE.charAt(i), idv.getMelody(i)))
              .sum()),

  /** Evaluate if two melodies are independent in terms of counterpoint. */
  VoiceIndependence(
      PenaltyVoiceIndependence,
      idv ->
          IntStream.range(0, Individual.VOICE.length() - 1)
              .map(
                  i ->
                      (int)
                          IntStream.range(i + 1, Individual.VOICE.length())
                              .mapToLong(
                                  j ->
                                      voiceIndependenceCheck(
                                          idv.getMelody(i),
                                          idv.getMelody(j),
                                          "SBS"
                                              .contains(
                                                  ""
                                                      + Individual.VOICE.charAt(i)
                                                      + Individual.VOICE.charAt(j))))
                              .sum())
              .sum()),

  /** Evaluate if S/A are outer voices. */
  ImproperOuterVoices(
      PenaltyImproperOuterVoices,
      idv ->
          (int)
              IntStream.range(0, idv.getChordNumber())
                  .map(idx -> improperOuterVoiceCount(idv.getChord(idx)))
                  .sum()),

  /** Evaluate if all chords are Triad Or Seventh */
  NotTriadOrSeventhChord(
      PenaltyNotTriadOrSeventhChord, idv -> idv.getSeries().split("[X]").length - 1),

  /** Evaluate if the seventh note is handled properly. */
  ImproperResolution(
      PenaltyImproperResolution,
      idv -> improperSeventhResolution(idv) + improperLeadingToneResolution(idv)),

  /** Evaluate if there are successive dissonant chords. */
  SuccessiveDissonantChords(
      PenaltySuccessiveDissonance,
      idv -> (int) Arrays.stream(idv.getSeries().split("[SsX]")).filter(String::isEmpty).count()),

  /** Evaluate if starting with triad. */
  StartWithNonTriad(PenaltyNonTriadStart, idv -> idv.getSeries().matches("\\[[Tt].+") ? 0 : 1),

  /**
   * Evaluate if a proper cadence is presented.
   *
   * @see art.cctcc.ga.Settings#Cadences
   */
  ImproperCadentialForm(
      PenaltyImproperCadential,
      idv ->
          (idv.getSeries().matches(".+[ST]T\\]") ? 0 : 1)
              + (Cadences.stream().anyMatch(idv.getProgression()::matches) ? 0 : 1)
              + (Objects.equals(
                      "" + Pitch.getRoot(idv.getLastChord()),
                      Pitch.values()[Pitch.getTop(idv.getLastChord())].getStep())
                  ? 0
                  : 1));

  private final double unitPenalty;
  private final Function<Individual, Integer> evalFn;

  private Evaluation(double unitPenalty, Function<Individual, Integer> evalFn) {

    this.unitPenalty = unitPenalty;
    this.evalFn = evalFn;
  }

  public double evaluate(Individual idv) {

    return this.unitPenalty * this.evalFn.apply(idv);
  }

  public static int melodicInfeasibilityCount(char v, List<Integer> melody) {

    var count =
        (int)
            IntStream.range(1, melody.size() - 1)
                .mapToObj(i -> List.of(melody.get(i - 1), melody.get(i), melody.get(i + 1)))
                .filter(itvl -> !isMelodicFeasible(v, itvl))
                .count();
    var itvl_groups =
        IntStream.range(1, melody.size())
            .mapToObj(i -> Math.abs(melody.get(i) - melody.get(i - 1)))
            .collect(
                Collectors.groupingBy(itvl -> itvl > 1 ? "Skip" : "Step", Collectors.counting()));
    count +=
        Math.max(0, itvl_groups.getOrDefault("Skip", 0L) - itvl_groups.getOrDefault("Step", 0L));
    return count;
  }

  public static boolean isMelodicFeasible(char v, List<Integer> figure) {

    for (int i = 0; i < figure.size() - 1; i++) {
      if (Pitch.getPitchSet(figure.get(i), figure.get(i + 1)).equals(Set.of("B", "F")))
        return false;
    }

    var itvls =
        IntStream.range(0, figure.size() - 1)
            .map(i -> figure.get(i + 1) - figure.get(i))
            .sorted()
            .toArray();

    // Inner voices
    if (v == 'A' || v == 'T')
      return Arrays.stream(itvls).allMatch(FEASIBLE_INTERVALS_INNER::contains)
          && (itvls.length == 1 || Math.abs(itvls[0] + itvls[1]) < 6);

    // Outer voices
    return Arrays.stream(itvls).allMatch(FEASIBLE_INTERVALS_OUTER::contains)
        && (itvls.length == 1 || Math.abs(itvls[0] + itvls[1]) <= 7);
  }

  /**
   * Count total occurrences of parallel 5th and 8th between two melodies.
   *
   * @param m1 first melody
   * @param m2 second melody
   * @param isOuters specify if applying outer voices ruleset.
   * @return total occurrences of parallel 5th and 8th.
   */
  public static long voiceIndependenceCheck(List<Integer> m1, List<Integer> m2, boolean isOuters) {

    var cLength = Math.min(m1.size(), m2.size());
    var pCounter = 0;
    for (int i = 1; i < cLength; i++) {
      if (Pitch.getPitchSet(m1.get(i), m2.get(i)).equals(Set.of("F", "B"))) continue;
      var currItvl = Math.abs(m2.get(i) - m1.get(i)) % 7;
      var prevItvl = Math.abs(m2.get(i - 1) - m1.get(i - 1)) % 7;
      if (currItvl == 0 || currItvl == 4) {
        if (prevItvl == currItvl && (m2.get(i) - m2.get(i - 1)) * (m1.get(i) - m1.get(i - 1)) != 0)
          pCounter++;
        else if (isOuters && (m2.get(i) - m2.get(i - 1)) * (m1.get(i) - m1.get(i - 1)) > 0)
          pCounter++;
      }
    }
    return pCounter;
  }

  public static int improperSeventhResolution(Individual idv) {

    return IntStream.range(0, idv.getChordNumber() - 1)
        .filter(chord -> Pitch.seventhChordTest(idv.getChord(chord)) != 'X')
        .map(
            chord ->
                (int)
                    Pitch.locateSeventhNote(idv.getChord(chord)).stream()
                        .map(idv::getMelody)
                        .map(m -> m.get(chord + 1) - m.get(chord))
                        .filter(itvl -> itvl != -1 && itvl != 0)
                        .count())
        .sum();
  }

  public static int improperLeadingToneResolution(Individual idv) {

    return IntStream.range(0, Individual.VOICE.length())
        .filter(v -> "SB".contains("" + Individual.VOICE.charAt(v)))
        .mapToObj(idv::getMelody)
        .map(Pitch::translateO2P)
        .map(List::toString)
        .mapToInt(str -> str.split("(B., )+").length - str.split("B., C").length)
        .sum();
  }

  public static int improperOuterVoiceCount(List<Integer> ords) {

    return (int)
        IntStream.range(0, Individual.VOICE.length())
            .filter(v -> "SB".contains("" + Individual.VOICE.charAt(v)))
            .filter(
                v ->
                    !ords.stream()
                        .sorted(
                            Individual.VOICE.charAt(v) == 'B'
                                ? Comparator.naturalOrder()
                                : Comparator.reverseOrder())
                        .findFirst()
                        .get()
                        .equals(ords.get(v)))
            .count();
  }
}
