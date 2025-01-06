/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestIndividualTest {
  private static final TestIndividual.Factory FACTORY = new TestIndividual.Factory();

  @Test
  void testWithChromosome() {
    var chromosome = Chromosome.of(1.0f, 2.0f, 3.0f);
    var individual = FACTORY.create(chromosome);

    assertEquals(6.0f, individual.getFitness());
    assertEquals(chromosome, individual.getChromosome());
  }

  @Test
  void testWithFitness() {
    var individual = TestIndividual.withFitness(3.14f);
    assertEquals(3.14f, individual.getFitness());
  }
}
