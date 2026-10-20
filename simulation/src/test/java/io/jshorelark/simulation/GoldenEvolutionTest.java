/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.events.CollisionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins a whole evolution run to a fixed seed.
 *
 * <p>The operator tests check each rule in isolation; this one checks that they are wired together
 * in the reference order, because that order is part of the behaviour. Reversing crossover and
 * mutation, dropping the {@code 0.00001f} fitness floor in roulette selection, or reseeding the
 * source mid-generation all leave the unit tests green while changing what the birds learn.
 *
 * <p>The expected values were produced by this implementation and are a regression lock, not an
 * oracle: they say "this run still behaves exactly as it did", not "this is what Rust prints". A
 * fixture recorded from the Rust optimizer is the open follow-up in {@code TO-DO.md}.
 *
 * <p>The configuration is deliberately small but viable. With the default {@code foodSize} of 0.01
 * and a full-length generation this runs for minutes and most generations score zero, which pins
 * nothing; a wider food radius keeps the birds eating while the steps stay in the thousands.
 */
@DisplayName("Golden evolution")
class GoldenEvolutionTest {

  /** Starting seed for every generation and every respawn in this run. */
  private static final long SEED = 20240521L;

  /** Generations to run before comparing, enough for the median to move off its floor. */
  private static final int GENERATIONS = 15;

  /** Chromosome length: 6 inputs x 9 hidden + 9 biases, then 9 hidden x 2 + 2 biases. */
  private static final int CHROMOSOME_LENGTH = 110;

  private static Config config() {
    return Config.defaults().toBuilder()
        .simGenerationLength(150)
        .worldAnimals(20)
        .worldFoods(30)
        .foodSize(0.05f)
        .build();
  }

  /** Runs the whole evolution and returns the statistics of each finished generation. */
  private static List<Statistics> run() {
    var random = new Random(SEED);
    var simulation = new Simulation(config(), random, CollisionListener.NONE);
    var generations = new ArrayList<Statistics>(GENERATIONS);
    for (int i = 0; i < GENERATIONS; i++) {
      generations.add(simulation.train(random));
    }
    return generations;
  }

  @Test
  @DisplayName("reproduces the recorded fitness trajectory")
  void reproducesFitnessTrajectory() {
    var expected =
        new float[][] {
          new float[] {0.0f, 5.0f, 1.2f, 0.5f},
          new float[] {0.0f, 6.0f, 1.65f, 1.0f},
          new float[] {0.0f, 5.0f, 1.35f, 1.0f},
          new float[] {0.0f, 9.0f, 2.6f, 2.0f},
          new float[] {0.0f, 7.0f, 2.35f, 2.0f},
          new float[] {0.0f, 6.0f, 1.75f, 1.0f},
          new float[] {0.0f, 9.0f, 3.7f, 3.5f},
          new float[] {0.0f, 6.0f, 1.6f, 1.0f},
          new float[] {0.0f, 11.0f, 4.35f, 4.5f},
          new float[] {0.0f, 7.0f, 2.7f, 2.0f},
          new float[] {0.0f, 8.0f, 2.45f, 1.5f},
          new float[] {0.0f, 8.0f, 2.85f, 2.0f},
          new float[] {0.0f, 9.0f, 3.65f, 3.5f},
          new float[] {1.0f, 10.0f, 4.15f, 3.0f},
          new float[] {0.0f, 8.0f, 3.7f, 3.0f}
        };

    var actual = run();

    assertThat(actual).hasSize(expected.length);
    for (int i = 0; i < expected.length; i++) {
      var statistics = actual.get(i);
      assertThat(statistics.generation()).as("generation index %d", i).isEqualTo(i);
      assertThat(statistics.minFitness())
          .as("min fitness of generation %d", i)
          .isEqualTo(expected[i][0]);
      assertThat(statistics.maxFitness())
          .as("max fitness of generation %d", i)
          .isEqualTo(expected[i][1]);
      assertThat(statistics.avgFitness())
          .as("avg fitness of generation %d", i)
          .isEqualTo(expected[i][2]);
      assertThat(statistics.medianFitness())
          .as("median fitness of generation %d", i)
          .isEqualTo(expected[i][3]);
    }
  }

  @Test
  @DisplayName("reproduces the recorded final population")
  void reproducesFinalPopulation() {
    // Digest also covers the weight order, so a change in bias-first flattening fails here even
    // when every fitness value happens to stay the same.
    var expected =
        new long[] {
          -838068809747874215L,
          -6607417722794833181L,
          -9115002743197954709L,
          2750058821870847887L,
          5660659294660124130L,
          -1669207484544016868L,
          5233884691853268291L,
          3553417777756484063L,
          -2343613084890938056L,
          6980285894426034660L,
          -2301315882371800204L,
          4020343051314985154L,
          467578767412299733L,
          -986023926816812316L,
          4295438566899587222L,
          2111311280226037906L,
          -8093904672951355398L,
          -3694003553129421371L,
          -5028405386650231509L,
          -7580652336631929172L
        };

    var random = new Random(SEED);
    var simulation = new Simulation(config(), random, CollisionListener.NONE);
    for (int i = 0; i < GENERATIONS; i++) {
      simulation.train(random);
    }

    var digests = simulation.getBirds().stream().map(GoldenEvolutionTest::digest).toList();

    assertThat(digests)
        .containsExactlyElementsOf(java.util.Arrays.stream(expected).boxed().toList());
  }

  @Test
  @DisplayName("learns: median fitness climbs above where it started")
  void medianClimbs() {
    var generations = run();

    // Guards the lock above: a trajectory updated to match a regression would still "pass", but a
    // population that stopped learning cannot show a rising median.
    assertThat(generations.getLast().medianFitness())
        .isGreaterThan(generations.getFirst().medianFitness());
  }

  @Test
  @DisplayName("keeps the chromosome length implied by the topology")
  void chromosomeLengthMatchesTopology() {
    var random = new Random(SEED);
    var simulation = new Simulation(config(), random, CollisionListener.NONE);

    assertThat(simulation.getBirds().getFirst().toChromosome().length())
        .isEqualTo(CHROMOSOME_LENGTH);
  }

  /**
   * Returns a stable digest of a bird's genes.
   *
   * @param bird the bird to digest
   * @return a 64-bit FNV-style digest over the raw bit patterns
   */
  private static long digest(Bird bird) {
    long hash = 1125899906842597L;
    for (float gene : bird.toChromosome().getGenes()) {
      hash = 31L * hash + Float.floatToIntBits(gene);
    }
    return hash;
  }
}
