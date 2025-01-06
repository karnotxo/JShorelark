/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import java.util.List;

import io.jshorelark.genetic.statistics.Statistics;

import lombok.Value;

/**
 * Result of evolving a population, containing both the evolved population and statistics.
 *
 * @param <T> type of individuals in the population
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class EvolutionResult<T extends Individual> {
  /** The evolved population. */
  List<T> population;

  /** Statistics about the evolution. */
  Statistics statistics;

  /**
   * Creates a new evolution result.
   *
   * @param population the evolved population
   * @param statistics statistics about the evolution
   */
  public EvolutionResult(List<T> population, Statistics statistics) {
    this.population = List.copyOf(population); // Create immutable copy
    this.statistics = statistics;
  }

  /**
   * Gets the evolved population.
   *
   * @return an unmodifiable view of the population
   */
  public List<T> getPopulation() {
    return population; // Already immutable from constructor
  }
}
