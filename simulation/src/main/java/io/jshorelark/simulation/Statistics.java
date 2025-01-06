/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation;

import lombok.Value;

/**
 * Statistics about the simulation. This class is equivalent to the Statistics struct in the Rust
 * implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class Statistics {
  /** Generation number. */
  int generation;

  /** Genetic algorithm statistics. */
  io.jshorelark.genetic.statistics.Statistics ga;

  /**
   * Returns the min fitness.
   *
   * @return a float
   */
  public float getMinFitness() {
    return ga.getMinFitness();
  }

  /**
   * Returns the max fitness.
   *
   * @return a float
   */
  public float getMaxFitness() {
    return ga.getMaxFitness();
  }

  /**
   * Returns the average fitness.
   *
   * @return a float
   */
  public float getAvgFitness() {
    return ga.getAvgFitness();
  }

  /**
   * Returns the median fitness.
   *
   * @return a float
   */
  public float getMedianFitness() {
    return ga.getMedianFitness();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "Statistics{minFitness=%.2f, maxFitness=%.2f, avgFitness=%.2f, medianFitness=%.2f}%n",
        ga.getMinFitness(), ga.getMaxFitness(), ga.getAvgFitness(), ga.getMedianFitness());
  }
}
