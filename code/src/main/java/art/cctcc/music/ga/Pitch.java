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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Pitch {
  C2, D2, E2, F2, G2, A2, B2,
  C3, D3, E3, F3, G3, A3, B3,
  C4, D4, E4, F4, G4, A4, B4,
  C5, D5, E5, F5, G5, A5, B5;

  /**
   * Get random note according to voice register specified.
   *
   * @param voice <code>'S', 'A', 'T', 'B'</code> for Soprano, Alto, Tenor and Bass.
   * @return random note in the specified register.
   */
  public static int getNote(char voice) {

    var register = getRegister(voice);
    return R.nextInt(register[1].ordinal() - register[0].ordinal() + 1) + register[0].ordinal();
  }

  public static boolean isInRegister(char voice, int p) {

    var register = getRegister(voice);
    return p >= register[0].ordinal() && p <= register[1].ordinal();
  }

  public String getStep() {

    return this.name().substring(0, 1);
  }

  public int getOctave() {

    return Integer.parseInt(this.name().substring(1, 2));
  }

  public static Pitch[] getRegister(char voice) {

    return switch (voice) {
      case 'S' -> Soprano;
      case 'A' -> Alto;
      case 'T' -> Tenor;
      case 'B' -> Bass;
      default -> throw new RuntimeException("getRegister(): Unexpected voice label.");
    };
  }

  public static boolean isConsonantChordFromBasso(List<Integer> ords, boolean checkBass) {

    var lowest = ords.stream().mapToInt(v -> v).sorted().findFirst().getAsInt();
    return (!checkBass || Individual.VOICE.charAt(ords.indexOf(lowest)) == 'B')
        && ords.stream().allMatch(p -> isConsonantInterval(lowest, p, false));
  }

  public static boolean isConsonantChord(List<Integer> ords) {

    return IntStream.range(0, ords.size() - 1)
            .filter(
                i ->
                    IntStream.range(i, ords.size())
                        .anyMatch(j -> !isConsonantInterval(ords.get(i), ords.get(j), true)))
            .count()
        == 0;
  }

  public static boolean isConsonantInterval(int o1, int o2, boolean fourthCons) {

    return !getPitchSet(List.of(o1, o2)).equals(Set.of("F", "B"))
        && (List.of(0, 2, 4, 5).contains(Math.abs(o1 - o2) % 7)
            || (fourthCons && Math.abs(o1 - o2) % 7 == 3));
  }

  public static char triadChordTest(List<Integer> ords) {

    if (getPitchSet(ords).equals(Set.of("B", "D", "F"))) return 'X';
    var nset = getNumbers(ords);
    if (!nset.equals(Set.of(5, 3))
        && !nset.equals(Set.of(3))
        && !nset.equals(Set.of(6, 3))
        && !nset.equals(Set.of(6, 4))) return 'X';
    return getRoot(ords);
  }

  public static char seventhChordTest(List<Integer> ords) {

    if (Set.of("B", "D", "F").equals(getPitchSet(ords))) return 'B';
    var nset = getNumbers(ords);
    if (!nset.equals(Set.of(7, 5, 3))
        && !nset.equals(Set.of(7, 3))
        && !nset.equals(Set.of(6, 5, 3))
        && !nset.equals(Set.of(6, 5))
        && !nset.equals(Set.of(6, 4, 3))
        && !nset.equals(Set.of(6, 4, 2))
        && !nset.equals(Set.of(4, 2))) return 'X';
    return Character.toLowerCase(getRoot(ords));
  }

  public static char getRoot(List<Integer> ords) {

    var pitches = translateO2P(ords);
    for (var pitch : pitches) {
      var chord = SEVENTHS.stream().filter(c -> c.startsWith(pitch.getStep())).findFirst().get();
      if (pitches.stream().map(Pitch::getStep).allMatch(chord::contains)) return chord.charAt(0);
    }
    return 'X';
  }

  public static boolean isRootPosition(List<Integer> ords) {

    var numbers = getNumbers(ords);
    if (Set.of("B", "D", "F").equals(getPitchSet(ords))) return numbers.equals(Set.of(6, 3));
    return numbers.contains(3) && Set.of(7, 5, 3).containsAll(numbers);
  }

  public static Set<Integer> getNumbers(List<Integer> ords) {

    return Set.copyOf(
        ords.stream()
            .map(ord -> (ord - Pitch.getBottom(ords)) % 7 + 1)
            .filter(n -> n > 1)
            .toList());
  }

  public static int getBottom(List<Integer> ords) {

    return ords.stream().sorted().findFirst().get();
  }

  public static int getTop(List<Integer> ords) {

    return ords.stream().sorted(Comparator.reverseOrder()).findFirst().get();
  }

  /**
   * Return indices of all sevenths in the provided chord.
   *
   * @param ords provided chord
   * @return list of indices of sevenths.
   */
  public static List<Integer> locateSeventhNote(List<Integer> ords) {

    var root = "" + getRoot(ords);
    var seventh =
        getPitchSet(ords).equals(Set.of("B", "D", "F"))
            ? "F"
            : "" + SEVENTHS.stream().filter(c -> c.startsWith(root)).findAny().get().charAt(3);
    var chord = translateO2P(ords);
    return IntStream.range(0, ords.size())
        .filter(i -> chord.get(i).getStep().equals(seventh))
        .boxed()
        .toList();
  }

  public static Set<String> getPitchSet(Integer... ords) {

    return getPitchSet(List.of(ords));
  }

  public static Set<String> getPitchSet(List<Integer> ords) {

    return Set.copyOf(Pitch.translateO2P(ords).stream().map(Pitch::getStep).toList());
  }

  public static List<Pitch> translateO2P(List<Integer> ords) {

    return ords.stream().map(v -> Pitch.values()[v]).toList();
  }

  public static List<Integer> translateP2O(List<Pitch> pitches) {

    return pitches.stream().map(Pitch::ordinal).toList();
  }
}
