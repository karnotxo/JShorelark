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
 * Defines a method for combining parent chromosomes to create offspring. Different implementations
 * can provide various crossover strategies like single-point, uniform, or arithmetic crossover.
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface CrossoverMethod {
  /**
   * Combines two parent chromosomes to create a child chromosome.
   *
   * @param random the random number generator to use
   * @param parent1 the first parent chromosome
   * @param parent2 the second parent chromosome
   * @return the child chromosome
   * @throws java.lang.IllegalArgumentException if parents have different lengths or are null
   */
  Chromosome crossover(Random random, Chromosome parent1, Chromosome parent2);
}
