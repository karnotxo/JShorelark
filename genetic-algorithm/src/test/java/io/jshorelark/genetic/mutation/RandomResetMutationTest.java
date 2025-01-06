/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.mutation;

import java.util.Random;
import java.util.stream.DoubleStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.jshorelark.genetic.Chromosome;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RandomResetMutation")
class RandomResetMutationTest {

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
    @DisplayName("throws exception when probability is invalid")
    void throwsOnInvalidProbability() {
      assertThrows(
          IllegalArgumentException.class, () -> new RandomResetMutation(-0.1f, -1.0f, 1.0f));
      assertThrows(
          IllegalArgumentException.class, () -> new RandomResetMutation(1.1f, -1.0f, 1.0f));
    }

    @Test
    @DisplayName("throws exception when min value is greater than max value")
    void throwsOnInvalidRange() {
      assertThrows(
          IllegalArgumentException.class, () -> new RandomResetMutation(0.5f, 1.0f, -1.0f));
    }

    @Test
    @DisplayName("accepts valid parameters")
    void acceptsValidParameters() {
      assertDoesNotThrow(() -> new RandomResetMutation(0.0f, 0.0f, 1.0f));
      assertDoesNotThrow(() -> new RandomResetMutation(0.5f, -1.0f, 1.0f));
      assertDoesNotThrow(() -> new RandomResetMutation(1.0f, -10.0f, 10.0f));
    }
  }

  @Nested
  @DisplayName("mutate")
  class Mutate {

    @Test
    @DisplayName("throws exception when chromosome is null")
    void throwsOnNullChromosome() {
      var mutation = new RandomResetMutation(0.5f, -1.0f, 1.0f);
      assertThrows(IllegalArgumentException.class, () -> mutation.mutate(random, null));
    }

    @Test
    @DisplayName("handles empty chromosome")
    void handlesEmptyChromosome() {
      var mutation = new RandomResetMutation(0.5f, -1.0f, 1.0f);
      var chromosome = Chromosome.of();

      chromosome.mutate(mutation, random);
      assertTrue(chromosome.isEmpty());
    }

    @Test
    @DisplayName("zero probability means no mutations")
    void zeroProbabilityNoMutations() {
      var mutation = new RandomResetMutation(0.0f, -1.0f, 1.0f);
      var chromosome = Chromosome.of(1.0f, 2.0f, 3.0f);

      chromosome.mutate(mutation, random);
      assertChromosomesEqual(Chromosome.of(1.0f, 2.0f, 3.0f), chromosome);
    }

    @Test
    @DisplayName("probability 1.0 means all genes are mutated")
    void probabilityOneAllMutated() {
      var mutation = new RandomResetMutation(1.0f, -1.0f, 1.0f);
      var chromosome = Chromosome.of(1.0f, 2.0f, 3.0f);

      chromosome.mutate(mutation, random);
      assertFalse(areChromosomesEqual(Chromosome.of(1.0f, 2.0f, 3.0f), chromosome));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.2f, 0.5f, 0.8f})
    @DisplayName("mutation rate approximately matches probability")
    void mutationRateMatchesProbability(float probability) {
      var mutation = new RandomResetMutation(probability, -1.0f, 1.0f);
      var original = Chromosome.of(0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
      int trials = 1000;
      int geneCount = original.length();
      int mutatedGenes = 0;

      for (int i = 0; i < trials; i++) {
        var chromosome = Chromosome.of(original.getGenes());
        chromosome.mutate(mutation, random);
        mutatedGenes += countDifferentGenes(original, chromosome);
      }

      // Calculate actual mutation rate
      float actualRate = (float) mutatedGenes / (trials * geneCount);

      // Allow for some statistical variation
      assertEquals(probability, actualRate, 0.05f);
    }

    @Test
    @DisplayName("mutated values are within specified range")
    void valuesWithinRange() {
      float minValue = -2.0f;
      float maxValue = 3.0f;
      var mutation = new RandomResetMutation(1.0f, minValue, maxValue);
      int trials = 1000;

      // Collect mutations
      double[] mutations = new double[trials];
      for (int i = 0; i < trials; i++) {
        var testChromosome = Chromosome.of(0.0f);
        testChromosome.mutate(mutation, random);
        mutations[i] = testChromosome.get(0);
      }

      // Verify all values are within range
      for (final var value : mutations) {
        assertTrue(
            value >= minValue && value <= maxValue,
            String.format("Value %f outside range [%f, %f]", value, minValue, maxValue));
      }

      // Verify values span the range (some near min, some near max)
      double min = DoubleStream.of(mutations).min().orElse(0);
      double max = DoubleStream.of(mutations).max().orElse(0);

      assertTrue(max - min > 0.8 * (maxValue - minValue), "Values don't span the expected range");
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
}
