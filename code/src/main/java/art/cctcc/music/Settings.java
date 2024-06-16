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
package art.cctcc.music;

import art.cctcc.music.ga.Pitch;
import static art.cctcc.music.ga.Pitch.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Settings {

  public static RandomGenerator R = GeneticAlgorithm.getRandomGenerator();

  public static int CHORD_NO = 17;
  public static String VOICE = "SAATTB";

  public static final Path DATA_FOLDER = Path.of("data");

  public static final Pitch[] Soprano = {G4, A5};
  public static final Pitch[] Alto = {G3, C5};
  public static final Pitch[] Tenor = {C3, F4};
  public static final Pitch[] Bass = {E2, C4};

  public static final List<String> TRIADS = // List of diatonic traids
      List.of("CEG", "DFA", "EGB", "FAC", "GBD", "ACE");

  public static final List<String> SEVENTHS = // List of diatonic sevenths
      List.of("CEGB", "DFAC", "EGBD", "FACE", "GBDF", "ACEG", "BDFA");

  public static final double PenaltyMelodicSmoothness = 0.01;
  public static final double PenaltyVoiceIndependence = 0.035;
  public static final double PenaltyImproperOuterVoices = 0.25;
  public static final double PenaltyNotTriadOrSeventhChord = 0.05;
  public static final double PenaltyImproperResolution = 0.015;
  public static final double PenaltySuccessiveDissonance = 0.02;
  public static final double PenaltyNonTriadStart = 0.03;
  public static final double PenaltyImproperCadential = 0.06;

  public static final List<Integer> FEASIBLE_INTERVALS_INNER = List.of(0, 1, 2, 3, -1, -2, -3);
  public static final List<Integer> FEASIBLE_INTERVALS_OUTER =
      List.of(1, 2, 3, 4, 5, 7, -1, -2, -3, -4, -7);

  /**
   * Predefined cadential formulas in regex.
   *
   * <pre>
   * .+[GFgf]C\\]
   * .+[CAa]D\\]
   * .+[DFA]E\\]
   * .+[C]F\\]
   * .+[DFAdf]G\\]
   * .+[EGeg]A\\]
   * </pre>
   */
  public static final List<String> Cadences =
      List.of(
          ".+[GFgf]C\\]",
          ".+[CAa]D\\]",
          ".+[DFA]E\\]",
          ".+[C]F\\]",
          ".+[DFAdf]G\\]",
          ".+[EGeg]A\\]");

  public static final Map<Character, String> RomanNumeral =
      Map.of(
          'C', "I",
          'D', "ii",
          'E', "iii",
          'F', "IV",
          'G', "V",
          'A', "vi",
          'B', "vii",
          'X', "-");
}
