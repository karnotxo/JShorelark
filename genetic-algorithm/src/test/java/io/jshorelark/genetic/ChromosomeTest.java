/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChromosomeTest {

  @Test
  void length() {
    var chromosome = Chromosome.of(3.0f, 1.0f, 2.0f);
    assertEquals(3, chromosome.length());
  }

  @Test
  void isEmpty() {
    var emptyChromosome = Chromosome.of();
    var nonEmptyChromosome = Chromosome.of(1.0f);

    assertTrue(emptyChromosome.isEmpty());
    assertFalse(nonEmptyChromosome.isEmpty());
  }

  @Test
  void iteration() {
    var chromosome = Chromosome.of(3.0f, 1.0f, 2.0f);
    var genes = chromosome.stream().collect(Collectors.toList());

    assertEquals(3, genes.size());
    assertEquals(3.0f, genes.get(0));
    assertEquals(1.0f, genes.get(1));
    assertEquals(2.0f, genes.get(2));
  }

  @Test
  void indexAccess() {
    var chromosome = Chromosome.of(3.0f, 1.0f, 2.0f);

    assertEquals(3.0f, chromosome.get(0));
    assertEquals(1.0f, chromosome.get(1));
    assertEquals(2.0f, chromosome.get(2));
  }

  @Test
  void fromStream() {
    var originalChromosome = Chromosome.of(3.0f, 1.0f, 2.0f);
    var newChromosome = Chromosome.fromStream(originalChromosome.stream());

    assertEquals(originalChromosome, newChromosome);
  }

  @Test
  void fromList() {
    var genes = new float[] {3.0f, 1.0f, 2.0f};
    var chromosome = Chromosome.of(genes);

    assertEquals(3, chromosome.length());
    assertEquals(3.0f, chromosome.get(0));
    assertEquals(1.0f, chromosome.get(1));
    assertEquals(2.0f, chromosome.get(2));
  }
}
