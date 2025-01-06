/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.selection;

import java.util.List;
import java.util.Random;

import io.jshorelark.genetic.Individual;

/**
 * Defines a method for selecting individuals from a population for breeding. Different
 * implementations can provide various selection strategies like roulette wheel selection,
 * tournament selection, etc. This interface is equivalent to the SelectionMethod trait in the Rust
 * implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface SelectionMethod {
  /**
   * Selects an individual from the population.
   *
   * @param random the random number generator to use
   * @param population the population to select from
   * @return the selected individual
   * @throws java.lang.IllegalArgumentException if the population is empty
   */
  Individual select(Random random, List<? extends Individual> population);
}
