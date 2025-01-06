/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.crossover.UniformCrossover;
import io.jshorelark.genetic.mutation.GaussianMutation;
import io.jshorelark.genetic.selection.RouletteWheelSelection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneticAlgorithm")
class GeneticAlgorithmTest {
  private static final float MUTATION_CHANCE = 0.5f;
  private static final float MUTATION_COEFF = 0.5f;
  private static final Random RANDOM = new Random(42);
  private static final TestIndividual.Factory FACTORY = new TestIndividual.Factory();

  private final GeneticAlgorithm<TestIndividual> algorithm =
      GeneticAlgorithm.create(
          new RouletteWheelSelection(),
          new UniformCrossover(),
          GaussianMutation.create(MUTATION_CHANCE, MUTATION_COEFF),
          FACTORY);

  @Test
  @DisplayName("evolves population")
  void evolvesPopulation() {
    var population =
        List.of(
            individual(0.0f, 0.0f, 0.0f),
            individual(1.0f, 1.0f, 1.0f),
            individual(1.0f, 2.0f, 1.0f),
            individual(1.0f, 2.0f, 4.0f));

    var result = algorithm.evolve(RANDOM, population);
    var evolved = result.getFirst();
    var stats = result.getSecond();

    assertThat(evolved).hasSize(population.size());
    assertThat(evolved).isNotEqualTo(population);

    // Verify statistics
    assertThat(stats.getMinFitness()).isGreaterThanOrEqualTo(0.0f);
    assertThat(stats.getMaxFitness()).isGreaterThan(0.0f);
    assertThat(stats.getAvgFitness()).isBetween(stats.getMinFitness(), stats.getMaxFitness());
  }

  private static TestIndividual individual(float... genes) {
    return FACTORY.create(Chromosome.of(genes));
  }
}
