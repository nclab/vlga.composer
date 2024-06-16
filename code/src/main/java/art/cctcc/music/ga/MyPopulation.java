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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.util.FastMath;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MyPopulation extends ElitisticListPopulation {

  public MyPopulation(int populationLimit, double elitismRate, int chord_no) {

    super(populationLimit, elitismRate);
    Stream.generate(() -> new Individual(chord_no))
        .limit(populationLimit)
        .forEach(this::addChromosome);
  }

  public MyPopulation(int populationLimit, double elitismRate) {

    super(populationLimit, elitismRate);
  }

  @Override
  public MyPopulation nextGeneration() {

    var nextGeneration = new MyPopulation(getPopulationLimit(), getElitismRate());
    getChromosomes().stream()
        .sorted()
        .skip((int) FastMath.ceil((1.0 - getElitismRate()) * getPopulationSize()))
        .forEach(nextGeneration::addChromosome);
    return nextGeneration;
  }

  /**
   * Access the fittest chromosome in this population.
   *
   * @return the fittest chromosome.
   */
  public List<Chromosome> getFittestChromosomes() {

    return this.getChromosomes().stream()
        .sorted(Comparator.reverseOrder())
        .limit((int) Math.max(1, this.getPopulationSize() * getElitismRate()))
        .toList();
  }

  public double getAvgFitness() {

    return getChromosomes().stream().mapToDouble(Chromosome::getFitness).average().getAsDouble();
  }

  public double getAvgEliteFitness() {

    return getFittestChromosomes().stream()
        .mapToDouble(Chromosome::getFitness)
        .average()
        .getAsDouble();
  }
}
