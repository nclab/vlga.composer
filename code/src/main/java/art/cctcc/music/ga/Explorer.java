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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Explorer {

  private final String voice;
  private final int chord_no;
  private final int populationLimit;
  private final double elitismRate;
  private final double co_rate;
  private final double cm_rate;
  private final double mo_rate;
  private final int max_mutation_loci;
  private final int max_mutation_voices;
  private final List<Evaluation> evals;
  private final double fitnessAim;
  private final int generationLimit;
  private final MyGeneticAlgorithm ga;
  private final StoppingCondition condition;
  private final long timestamp;
  private final String filename;

  private Individual fittest;

  public Explorer(
      String voice,
      int chord_no,
      int populationLimit,
      double elitismRate,
      double co_rate,
      double cm_rate,
      double mo_rate,
      int max_mutation_loci,
      int max_mutation_voices,
      List<Evaluation> evals,
      double fitnessAim,
      int generationLimit)
      throws IOException {

    this.voice = voice;
    this.populationLimit = populationLimit;
    this.elitismRate = elitismRate;
    this.evals = evals;
    this.fitnessAim = fitnessAim;
    this.generationLimit = generationLimit;

    this.co_rate = co_rate;
    this.cm_rate = cm_rate;
    this.mo_rate = mo_rate;
    this.max_mutation_loci = max_mutation_loci;
    this.max_mutation_voices = max_mutation_voices;
    this.ga =
        new MyGeneticAlgorithm(
            new CrossoverByChord(0.8, 0.3),
            new MutationByChord(this.max_mutation_loci, this.max_mutation_voices),
            new TournamentSelection(2),
            this.co_rate,
            this.cm_rate,
            this.mo_rate);
    this.chord_no = chord_no;

    Individual.VOICE = this.voice;
    Individual.EVALS = this.evals;

    this.condition =
        p -> {
          var fittest = p.getFittestChromosome();
          if (fittest instanceof Individual idv && idv.getProgression().contains("X")) return false;
          return fittest.getFitness() >= this.fitnessAim
              || this.ga.getGenerationsEvolved() >= this.generationLimit;
        };

    this.timestamp = Instant.now().toEpochMilli();
    this.filename = String.format("vlga-%dx%d-%s", voice.length(), chord_no, this.timestamp);
  }

  public void start() throws IOException {

    logParameters();

    this.ga.log("\nEvolution begins...");
    var init_population = new MyPopulation(this.populationLimit, this.elitismRate, this.chord_no);
    var final_pa = this.ga.evolve(init_population, condition);

    this.fittest = (Individual) final_pa.getFittestChromosome();

    this.ga.log("Fittest = \n" + fittest);
    this.ga.log(String.format("fitness = %3f", fittest.getFitness()));
    this.ga.log("generation = " + this.ga.getGenerationsEvolved());
  }

  public void saveScore() {

    this.fittest.saveScore(DATA_FOLDER, this.filename + ".musicxml", "Composer-" + this.timestamp);
  }

  public void saveData() throws IOException {

    Files.write(DATA_FOLDER.resolve(this.filename + ".txt"), this.ga.text_log);
  }

  private void logParameters() {

    this.ga.log(String.format("Voice = %s", this.voice));
    this.ga.log("Chord No. = " + this.chord_no);
    this.ga.log("Population = " + this.populationLimit);
    this.ga.log("Elitism Rate = " + this.elitismRate);
    this.ga.log("Crossover Only Rate = " + this.co_rate);
    this.ga.log("Crossover + Mutation Rate = " + this.cm_rate);
    this.ga.log("Mutation Only Rate = " + this.mo_rate);
    this.ga.log("Fitness Aim = " + this.fitnessAim);
    this.ga.log("Generation Limit = " + this.generationLimit);
    this.ga.log("Max Mutation Loci = " + this.max_mutation_loci);
    this.ga.log("Max Mutated Voices = " + this.max_mutation_voices);
    this.ga.log("Evaluation:");
    this.evals.forEach(e -> ga.log(" - " + e));
  }
}
