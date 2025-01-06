/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.mutation;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Chromosome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GaussianMutation")
class GaussianMutationTest {

  private Random random;

  @BeforeEach
  void setUp() {
    // Use a fixed seed for reproducible tests
    random = new Random(42);
  }

  @Nested
  @DisplayName("constructor")
  class Constructor {

    @Test
    @DisplayName("validates chance parameter")
    void validatesChanceParameter() {
      assertThrows(IllegalArgumentException.class, () -> GaussianMutation.create(-0.1f, 1.0f));
      assertThrows(IllegalArgumentException.class, () -> GaussianMutation.create(1.1f, 1.0f));
    }

    @Test
    @DisplayName("accepts valid parameters")
    void acceptsValidParameters() {
      assertDoesNotThrow(() -> GaussianMutation.create(0.0f, 0.0f));
      assertDoesNotThrow(() -> GaussianMutation.create(1.0f, 1.0f));
      assertDoesNotThrow(() -> GaussianMutation.create(0.5f, 0.5f));
    }
  }

  @Nested
  @DisplayName("mutate")
  class Mutate {

    @Test
    @DisplayName("throws exception when chromosome is null")
    void throwsOnNullChromosome() {
      var mutation = GaussianMutation.create(0.5f, 1.0f);
      assertThrows(IllegalArgumentException.class, () -> mutation.mutate(random, null));
    }

    @Test
    @DisplayName("handles empty chromosome")
    void handlesEmptyChromosome() {
      var mutation = GaussianMutation.create(0.5f, 1.0f);
      final var chromosome = Chromosome.of();

      chromosome.mutate(mutation, random);
      assertTrue(chromosome.isEmpty());
    }

    @Test
    @DisplayName("does not mutate with zero chance")
    void doesNotMutateWithZeroChance() {
      var mutation = GaussianMutation.create(0.0f, 1.0f);
      var genes = new float[] {1.0f, 2.0f, 3.0f};
      var random = new Random(42);

      mutation.mutate(random, genes);
      assertThat(genes).containsExactly(1.0f, 2.0f, 3.0f);
    }

    @Test
    @DisplayName("does not mutate with zero coefficient")
    void doesNotMutateWithZeroCoefficient() {
      var mutation = GaussianMutation.create(1.0f, 0.0f);
      var genes = new float[] {1.0f, 2.0f, 3.0f};
      var random = new Random(42);

      mutation.mutate(random, genes);
      assertThat(genes).containsExactly(1.0f, 2.0f, 3.0f);
    }

    @Test
    @DisplayName("always mutates with 100% chance")
    void alwaysMutatesWithFullChance() {
      var mutation = GaussianMutation.create(1.0f, 1.0f);
      var genes = new float[] {1.0f, 2.0f, 3.0f};
      var random = new Random(42);

      mutation.mutate(random, genes);
      assertThat(genes).isNotEqualTo(new float[] {1.0f, 2.0f, 3.0f});
    }

    @Test
    @DisplayName("mutation rate matches probability")
    void mutationRateMatchesProbability() {
      float probability = 0.3f;
      var mutation = GaussianMutation.create(probability, 1.0f);
      var genes = new float[] {1.0f};
      var random = new Random(42);

      int mutations = 0;
      int iterations = 10000;

      for (int i = 0; i < iterations; i++) {
        float[] testGenes = {1.0f};
        mutation.mutate(random, testGenes);
        if (testGenes[0] != 1.0f) {
          mutations++;
        }
      }

      float actualRate = (float) mutations / iterations;
      assertThat(actualRate).isCloseTo(probability, within(0.05f));
    }

    @Test
    @DisplayName("mutation size matches coefficient")
    void mutationSizeMatchesCoefficient() {
      float coeff = 0.5f;
      var mutation = GaussianMutation.create(1.0f, coeff);
      var genes = new float[] {1.0f};
      var random = new Random(42);

      int iterations = 10000;
      float totalDelta = 0;

      for (int i = 0; i < iterations; i++) {
        float[] testGenes = {1.0f};
        mutation.mutate(random, testGenes);
        totalDelta += Math.abs(testGenes[0] - 1.0f);
      }

      float avgDelta = totalDelta / iterations;
      assertThat(avgDelta).isCloseTo(coeff / 2, within(0.05f));
    }
  }

  private void assertChromosomesEqual(Chromosome expected, Chromosome actual) {
    assertTrue(
        areChromosomesEqual(expected, actual),
        String.format("Expected %s but got %s", expected, actual));
  }

  private boolean areChromosomesEqual(Chromosome c1, Chromosome c2) {
    if (c1.length() != c2.length()) {
      return false;
    }
    for (int i = 0; i < c1.length(); i++) {
      if (c1.get(i) != c2.get(i)) {
        return false;
      }
    }
    return true;
  }

  private int countDifferentGenes(Chromosome c1, Chromosome c2) {
    int count = 0;
    for (int i = 0; i < c1.length(); i++) {
      if (c1.get(i) != c2.get(i)) {
        count++;
      }
    }
    return count;
  }

  private double calculateMean(double[] values) {
    return IntStream.range(0, values.length).mapToDouble(i -> values[i]).average().orElse(0.0);
  }
}
