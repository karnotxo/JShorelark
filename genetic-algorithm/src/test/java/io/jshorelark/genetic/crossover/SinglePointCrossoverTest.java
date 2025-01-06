/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.crossover;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Chromosome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SinglePointCrossover")
class SinglePointCrossoverTest {

  private SinglePointCrossover crossover;
  private Random random;

  @BeforeEach
  void setUp() {
    crossover = new SinglePointCrossover();
    // Use a fixed seed for reproducible tests
    random = new Random(42);
  }

  @Nested
  @DisplayName("crossover")
  class Crossover {

    @Test
    @DisplayName("throws exception when parents are null")
    void throwsOnNullParents() {
      var validParent = Chromosome.of(1.0f, 2.0f);

      assertThrows(
          IllegalArgumentException.class, () -> crossover.crossover(random, null, validParent));

      assertThrows(
          IllegalArgumentException.class, () -> crossover.crossover(random, validParent, null));

      assertThrows(IllegalArgumentException.class, () -> crossover.crossover(random, null, null));
    }

    @Test
    @DisplayName("throws exception when parents have different lengths")
    void throwsOnDifferentLengths() {
      var parent1 = Chromosome.of(1.0f, 2.0f);
      var parent2 = Chromosome.of(1.0f, 2.0f, 3.0f);

      assertThrows(
          IllegalArgumentException.class, () -> crossover.crossover(random, parent1, parent2));
    }

    @Test
    @DisplayName("handles empty parents")
    void handlesEmptyParents() {
      var parent1 = Chromosome.of();
      var parent2 = Chromosome.of();

      var child = crossover.crossover(random, parent1, parent2);
      assertTrue(child.isEmpty());
    }

    @Test
    @DisplayName("creates valid child chromosome")
    void createsValidChild() {
      var parent1 = Chromosome.of(1.0f, 2.0f, 3.0f, 4.0f);
      var parent2 = Chromosome.of(5.0f, 6.0f, 7.0f, 8.0f);

      var child = crossover.crossover(random, parent1, parent2);

      // Child should have same length as parents
      assertEquals(parent1.length(), child.length());

      // Child should contain genes from both parents
      boolean hasGenesFromParent1 = false;
      boolean hasGenesFromParent2 = false;

      for (int i = 0; i < child.length(); i++) {
        float gene = child.get(i);
        if (gene <= 4.0f) {
          hasGenesFromParent1 = true;
        }
        if (gene >= 5.0f) {
          hasGenesFromParent2 = true;
        }
      }

      assertTrue(hasGenesFromParent1, "Child should contain genes from parent1");
      assertTrue(hasGenesFromParent2, "Child should contain genes from parent2");
    }

    @Test
    @DisplayName("crossover point is not at endpoints")
    void crossoverPointNotAtEndpoints() {
      var parent1 = Chromosome.of(1.0f, 2.0f, 3.0f, 4.0f);
      var parent2 = Chromosome.of(5.0f, 6.0f, 7.0f, 8.0f);

      // Run many crossovers and verify first and last genes
      IntStream.range(0, 100)
          .forEach(
              i -> {
                var child = crossover.crossover(random, parent1, parent2);

                // First gene should always come from parent1
                assertEquals(1.0f, child.get(0));

                // Last gene should always come from parent2
                assertEquals(8.0f, child.get(child.length() - 1));
              });
    }

    @Test
    @DisplayName("genes maintain their relative positions")
    void maintainsRelativePositions() {
      var parent1 = Chromosome.of(1.0f, 2.0f, 3.0f, 4.0f);
      var parent2 = Chromosome.of(5.0f, 6.0f, 7.0f, 8.0f);

      var child = crossover.crossover(random, parent1, parent2);

      // Find where the crossover happened
      int crossoverPoint = 0;
      for (int i = 1; i < child.length(); i++) {
        if (child.get(i) >= 5.0f) {
          crossoverPoint = i;
          break;
        }
      }

      // Verify genes before crossover point match parent1
      for (int i = 0; i < crossoverPoint; i++) {
        assertEquals(parent1.get(i), child.get(i));
      }

      // Verify genes after crossover point match parent2
      for (int i = crossoverPoint; i < child.length(); i++) {
        assertEquals(parent2.get(i), child.get(i));
      }
    }
  }
}
