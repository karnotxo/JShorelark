/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.evolution;

import lombok.Value;

/**
 * Statistics for a generation in the evolution.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class EvolutionStatistics {
  /** The generation number. */
  int generation;

  /** The minimum fitness. */
  float minFitness;

  /** The maximum fitness. */
  float maxFitness;

  /** The average fitness. */
  float avgFitness;

  /** The median fitness. */
  float medianFitness;

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "Generation %d: min=%.2f, max=%.2f, avg=%.2f, median=%.2f",
        generation, minFitness, maxFitness, avgFitness, medianFitness);
  }
}
