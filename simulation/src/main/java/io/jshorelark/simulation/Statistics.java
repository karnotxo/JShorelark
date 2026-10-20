/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import java.util.Objects;

/**
 * Statistics of a generation that just finished.
 *
 * <p>Mirrors Rust's {@code Statistics} struct. The four fitness aggregates are delegated to the
 * genetic-algorithm statistics, so this type never recomputes them: exactly one calculator exists.
 *
 * @param generation the generation that produced these values
 * @param ga the population statistics reported by the genetic algorithm
 */
public record Statistics(int generation, io.jshorelark.genetic.statistics.Statistics ga) {

  /** Validates the components. */
  public Statistics {
    Objects.requireNonNull(ga, "ga");
  }

  /**
   * Returns the lowest fitness in the population.
   *
   * @return the minimum fitness
   */
  public float minFitness() {
    return ga.getMinFitness();
  }

  /**
   * Returns the highest fitness in the population.
   *
   * @return the maximum fitness
   */
  public float maxFitness() {
    return ga.getMaxFitness();
  }

  /**
   * Returns the mean fitness of the population.
   *
   * @return the average fitness
   */
  public float avgFitness() {
    return ga.getAvgFitness();
  }

  /**
   * Returns the median fitness of the population.
   *
   * @return the median fitness
   */
  public float medianFitness() {
    return ga.getMedianFitness();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Statistics{minFitness=%.2f, maxFitness=%.2f, avgFitness=%.2f, medianFitness=%.2f}"
        .formatted(minFitness(), maxFitness(), avgFitness(), medianFitness());
  }
}
