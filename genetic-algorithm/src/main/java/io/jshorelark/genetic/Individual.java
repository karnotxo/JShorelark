/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

/**
 * Individual in a population.
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface Individual {
  /**
   * Returns the individual's chromosome.
   *
   * @return a {@link io.jshorelark.genetic.Chromosome} object
   */
  Chromosome getChromosome();

  /**
   * Returns the individual's fitness.
   *
   * @return a float
   */
  float getFitness();

  /** Factory for creating individuals. */
  interface Factory<I extends Individual> {
    /** Creates a new individual from a chromosome. */
    I create(Chromosome chromosome);
  }
}
