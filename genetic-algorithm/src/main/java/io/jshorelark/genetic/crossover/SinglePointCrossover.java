/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.crossover;

import java.util.Random;

import io.jshorelark.genetic.Chromosome;

/**
 * Implements single-point crossover.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class SinglePointCrossover implements CrossoverMethod {
  /** {@inheritDoc} */
  @Override
  public Chromosome crossover(Random random, Chromosome parent1, Chromosome parent2) {
    if (parent1 == null || parent2 == null) {
      throw new IllegalArgumentException("Parents cannot be null");
    }

    if (parent1.length() != parent2.length()) {
      throw new IllegalArgumentException("Parents must have same length");
    }

    if (parent1.isEmpty()) {
      return Chromosome.of();
    }

    // Select crossover point (1 to length-1)
    int point = 1 + random.nextInt(parent1.length() - 1);

    // Create child genes by combining parents
    final var childGenes = new float[parent1.length()];

    // Take genes from parent1 up to crossover point
    for (int i = 0; i < point; i++) {
      childGenes[i] = parent1.get(i);
    }

    // Take remaining genes from parent2
    for (int i = point; i < parent2.length(); i++) {
      childGenes[i] = parent2.get(i);
    }

    return Chromosome.of(childGenes);
  }
}
