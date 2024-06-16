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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.SelectionPolicy;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MyGeneticAlgorithm extends GeneticAlgorithm {

  /** Mutation-Only Rate */
  public double mo_rate;

  /** Crossover-Only Rate */
  public double co_rate;

  /** Mutation-and-Crossover Rate */
  public double cm_rate;

  public Instant timer;
  public List<String> text_log;

  public MyGeneticAlgorithm(
      CrossoverPolicy crossoverPolicy,
      MutationPolicy mutationPolicy,
      SelectionPolicy selectionPolicy,
      double co_rate,
      double cm_rate,
      double mo_rate)
      throws OutOfRangeException {

    super(crossoverPolicy, 0, mutationPolicy, 0, selectionPolicy);
    this.timer = Instant.now();
    this.text_log = new ArrayList<>();
    this.co_rate = co_rate;
    this.cm_rate = cm_rate;
    this.mo_rate = mo_rate;
  }

  @Override
  public Population nextGeneration(Population currentPopulation) {

    if (currentPopulation instanceof MyPopulation current) {
      var next = current.nextGeneration();
      while (next.getPopulationSize() < next.getPopulationLimit()) {
        var pair = this.getSelectionPolicy().select(current);
        var dice = R.nextDouble();
        if ((dice -= cm_rate) < 0) {
          pair = this.getCrossoverPolicy().crossover(pair.getFirst(), pair.getSecond());
          pair =
              new ChromosomePair(
                  this.getMutationPolicy().mutate(pair.getFirst()),
                  this.getMutationPolicy().mutate(pair.getSecond()));
        } else if ((dice -= co_rate) < 0) {
          pair = this.getCrossoverPolicy().crossover(pair.getFirst(), pair.getSecond());
        } else if ((dice -= mo_rate) < 0) {
          pair =
              new ChromosomePair(
                  this.getMutationPolicy().mutate(pair.getFirst()),
                  this.getMutationPolicy().mutate(pair.getSecond()));
        }
        next.addChromosome(pair.getFirst());
        if (next.getPopulationSize() < next.getPopulationLimit())
          next.addChromosome(pair.getSecond());
      }
      var now = Instant.now();
      var duration = Duration.between(timer, now).getSeconds();
      if (duration >= 3 && (this.getGenerationsEvolved() + 1) % 10 == 0) {
        timer = now;
        if (next.getFittestChromosome() instanceof Individual idv) {
          log(
              String.format(
                  "%6d:%6.3f /%6.3f %s P%s",
                  this.getGenerationsEvolved() + 1,
                  idv.getFitness(),
                  next.getAvgEliteFitness(),
                  idv.getSeries(),
                  idv.getProgression()));
        }
      }
      return next;
    }
    return null;
  }

  public void log(String text) {

    System.out.println(text);
    this.text_log.add(text);
  }
}
