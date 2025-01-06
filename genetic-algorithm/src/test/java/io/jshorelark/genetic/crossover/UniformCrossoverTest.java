/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.crossover;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Chromosome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UniformCrossover")
class UniformCrossoverTest {
  private Random random;
  private UniformCrossover crossover;

  @BeforeEach
  void setUp() {
    // Use a fixed seed for reproducible tests, matching Rust's ChaCha8Rng
    random = new Random(742);
    crossover = new UniformCrossover();
  }

  @Test
  @DisplayName("genes are selected with approximately equal probability")
  void genesSelectedWithEqualProbability() {
    // Create parents with 100 genes, like Rust test
    float[] parent1Genes = new float[100];
    float[] parent2Genes = new float[100];
    for (int i = 1; i <= 100; i++) {
      parent1Genes[i - 1] = (float) i;
      parent2Genes[i - 1] = (float) -i;
    }

    var parent1 = Chromosome.of(parent1Genes);
    var parent2 = Chromosome.of(parent2Genes);

    // Run multiple trials to account for randomness
    int trials = 1000;
    int totalDiffParent1 = 0;

    for (int trial = 0; trial < trials; trial++) {
      var child = crossover.crossover(random, parent1, parent2);

      // Count genes from parent1 (negative values)
      for (int i = 0; i < child.length(); i++) {
        if (child.get(i) < 0) {
          totalDiffParent1++;
        }
      }
    }

    // Calculate average split across all trials
    float avgSplitParent1 = (float) totalDiffParent1 / (trials * 100);

    // Should be roughly 50/50 split with some tolerance for randomness
    assertEquals(0.5, avgSplitParent1, 0.05);
  }

  @Test
  @DisplayName("throws exception when parents have different lengths")
  void throwsOnDifferentLengths() {
    var parent1 = Chromosome.of(1.0f, 2.0f);
    var parent2 = Chromosome.of(1.0f, 2.0f, 3.0f);

    assertThrows(
        IllegalArgumentException.class, () -> crossover.crossover(random, parent1, parent2));
  }
}
