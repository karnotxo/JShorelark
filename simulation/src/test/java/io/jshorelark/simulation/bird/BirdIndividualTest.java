/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import static org.assertj.core.api.Assertions.assertThat;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BirdIndividual")
class BirdIndividualTest {

  private Config config;
  private Random random;

  @BeforeEach
  void setUp() {
    config = Config.defaults().toBuilder().eyeCells(2).brainNeurons(2).build();
    random = new Random(42);
  }

  @Test
  @DisplayName("captures the bird's chromosome and satiation")
  void capturesState() {
    var bird = Bird.create(BirdBrain.random(random, config), new Vector2D(0.5f, 0.5f), config);
    bird.eat();
    bird.eat();

    var individual = BirdIndividual.of(bird);

    assertThat(individual.getFitness()).isEqualTo(2.0f);
    assertThat(individual.getChromosome()).isEqualTo(bird.toChromosome());
  }

  @Test
  @DisplayName("starts unevaluated individuals at zero fitness")
  void fromChromosomeStartsAtZero() {
    var chromosome = Chromosome.of(new float[(2 + 1) * 2 + (2 + 1) * 2]);

    var individual = BirdIndividual.fromChromosome(chromosome, config);

    assertThat(individual.getFitness()).isZero();
    assertThat(individual.getChromosome()).isEqualTo(chromosome);
  }

  @Test
  @DisplayName("replaces the fitness without touching the chromosome")
  void withFitnessKeepsChromosome() {
    var individual = BirdIndividual.fromChromosome(Chromosome.of(new float[12]), config);

    var updated = individual.withFitness(7.0f);

    assertThat(updated.getFitness()).isEqualTo(7.0f);
    assertThat(updated.getChromosome()).isEqualTo(individual.getChromosome());
    assertThat(individual.getFitness()).isZero();
  }

  @Test
  @DisplayName("inverts fitness against the best bird, as ga_reverse does")
  void reversesFitness() {
    var best =
        BirdIndividual.fromChromosome(Chromosome.of(new float[12]), config).withFitness(5.0f);
    var worst =
        BirdIndividual.fromChromosome(Chromosome.of(new float[12]), config).withFitness(1.0f);

    var reversed = BirdIndividual.reverseFitness(List.of(best, worst));

    assertThat(reversed).extracting(BirdIndividual::getFitness).containsExactly(0.0f, 4.0f);
  }

  @Test
  @DisplayName("returns an empty list for an empty population")
  void reversesEmptyPopulation() {
    assertThat(BirdIndividual.reverseFitness(List.of())).isEmpty();
  }

  @Test
  @DisplayName("rebuilds a bird carrying the same chromosome")
  void toBirdKeepsChromosome() {
    var individual = BirdIndividual.fromChromosome(Chromosome.of(new float[12]), config);

    var bird = individual.toBird(random);

    assertThat(bird.toChromosome()).isEqualTo(individual.getChromosome());
  }

  @Test
  @DisplayName("factory produces unevaluated individuals bound to its configuration")
  void factoryCreatesIndividuals() {
    var factory = new BirdIndividual.Factory(config);
    var chromosome = Chromosome.of(new float[12]);

    var individual = factory.create(chromosome);

    assertThat(individual.getChromosome()).isEqualTo(chromosome);
    assertThat(individual.getFitness()).isZero();
  }
}
