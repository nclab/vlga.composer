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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.MutationPolicy;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MutationByChord implements MutationPolicy {

  private final int max_loci;
  private final int max_voices;

  public MutationByChord(int max_loci, int max_voices) {

    this.max_loci = max_loci;
    this.max_voices = max_voices;
  }

  @Override
  public Chromosome mutate(Chromosome original) {

    if (original instanceof Individual idv) {
      var mVoices = R.nextInt(max_voices) + 1;
      var mLoci = R.nextInt(max_loci) + 1;
      return this.getMutation(idv, mLoci, mVoices);
    }
    throw new IllegalArgumentException();
  }

  Individual getMutation(Individual idv, int mLoci, int mVoices) {

    return getMutation(idv, mLoci, mVoices, true);
  }

  /**
   * Generate a mutated individual from current instance with specified mutation loci.
   *
   * @param mLoci number of mutation sites.
   * @param mVoices number of mutated voices each site.
   * @return generated mutation.
   */
  Individual getMutation(Individual idv, int mLoci, int mVoices, boolean canKeep) {

    var newChromosome = new ArrayList<Integer>(idv.getRepresentation());
    var chord_indices =
        IntStream.generate(() -> R.nextInt(idv.getChordNumber())).distinct().limit(mLoci).toArray();
    for (int chord_idx : chord_indices) {
      var mutatedVoiceFlag =
          Stream.generate(() -> R.nextInt(1 << Individual.VOICE.length()))
              .map(Integer::toBinaryString)
              .filter(s -> s.replace("0", "").length() == mVoices)
              .map(s -> "0".repeat(Individual.VOICE.length() - s.length()) + s)
              .findAny()
              .get();
      IntStream.range(0, Individual.VOICE.length())
          .filter(v -> mutatedVoiceFlag.charAt(v) == '1')
          .forEach(
              v -> {
                var voice = Individual.VOICE.charAt(v);
                var vRange = Pitch.getRegister(voice);
                var melody = idv.getMelody(v);
                Function<Integer, List<Integer>> figure =
                    p ->
                        (chord_idx == idv.getChordNumber() - 1)
                            ? List.of(melody.get(chord_idx - 1), p)
                            : List.of(p, melody.get(chord_idx + 1));
                var candidates =
                    IntStream.rangeClosed(vRange[0].ordinal(), vRange[1].ordinal())
                        .filter(p -> canKeep || p != idv.getChord(chord_idx).get(v))
                        .filter(p -> Evaluation.isMelodicFeasible(voice, figure.apply(p)))
                        .toArray();
                newChromosome.set(
                    chord_idx * Individual.VOICE.length() + v,
                    candidates[R.nextInt(candidates.length)]);
              });
    }
    return new Individual(newChromosome);
  }
}
