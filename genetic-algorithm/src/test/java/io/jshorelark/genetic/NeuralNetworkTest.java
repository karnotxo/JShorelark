/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.genetic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NeuralNetwork")
class NeuralNetworkTest {

  private Chromosome createChromosome(float... values) {
    return Chromosome.of(values);
  }

  @Test
  @DisplayName("createChromosome creates valid chromosome")
  void createChromosomeTest() {
    var chromosome = createChromosome(1.0f, 2.0f, 3.0f);

    assertEquals(3, chromosome.length());
    assertEquals(1.0f, chromosome.get(0));
    assertEquals(2.0f, chromosome.get(1));
    assertEquals(3.0f, chromosome.get(2));
  }
}
