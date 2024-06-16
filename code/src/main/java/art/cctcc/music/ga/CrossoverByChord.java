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

import static art.cctcc.music.Settings.R;
import java.util.ArrayList;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.UniformCrossover;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CrossoverByChord implements CrossoverPolicy {

  private final double CROSSOVER_BY_CHORD_RATIO;
  private final CrossoverPolicy CHORDAL;
  private final CrossoverPolicy UNIFORM;

  /** Constructor with no uniform crossover. */
  public CrossoverByChord() {

    this(1.0, 0.0);
  }

  /**
   * Constructor with specified chance for Chordal Crossover and uniform ratio.
   *
   * @param byChordRatio chance to use chordal crossover.
   * @param uniformRatio ratio between parents for uniform crossover.
   */
  public CrossoverByChord(double byChordRatio, double uniformRatio) {

    this.CHORDAL =
        (first, second) -> {
          if (first instanceof Individual idv1 && second instanceof Individual idv2) {
            var locus = R.nextInt(idv1.getChordNumber() - 1) + 1;
            var c1 = new ArrayList<Integer>(idv1.getChord(0));
            var c2 = new ArrayList<Integer>(idv2.getChord(0));
            for (int i = 1; i < idv1.getChordNumber(); i++) {
              c1.addAll((i < locus) ? idv1.getChord(i) : idv2.getChord(i));
              c2.addAll((i < locus) ? idv2.getChord(i) : idv1.getChord(i));
            }
            return new ChromosomePair(new Individual(c1), new Individual(c2));
          }
          throw new MathIllegalArgumentException(
              LocalizedFormats.ARGUMENT_OUTSIDE_DOMAIN, first, second);
        };

    this.UNIFORM = new UniformCrossover(uniformRatio);

    this.CROSSOVER_BY_CHORD_RATIO = byChordRatio;
  }

  @Override
  public ChromosomePair crossover(Chromosome first, Chromosome second)
      throws MathIllegalArgumentException {

    return R.nextDouble() < CROSSOVER_BY_CHORD_RATIO
        ? CHORDAL.crossover(first, second)
        : UNIFORM.crossover(first, second);
  }
}
