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
 * Implements uniform crossover.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class UniformCrossover implements CrossoverMethod {
  /** {@inheritDoc} */
  @Override
  public Chromosome crossover(Random random, Chromosome parent1, Chromosome parent2) {
    if (parent1 == null || parent2 == null) {
      throw new IllegalArgumentException("Parents cannot be null");
    }

    if (parent1.length() != parent2.length()) {
      throw new IllegalArgumentException("Parents must have same length");
    }

    final var childGenes = new float[parent1.length()];
    for (int i = 0; i < parent1.length(); i++) {
      // Use nextDouble() for better precision than nextBoolean()
      childGenes[i] = (random.nextBoolean() ? parent1.get(i) : parent2.get(i));
    }

    return Chromosome.of(childGenes);
  }
}
