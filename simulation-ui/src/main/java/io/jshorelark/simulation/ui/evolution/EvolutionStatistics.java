/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.evolution;

import io.jshorelark.simulation.Statistics;
import java.util.Objects;

/**
 * A completed generation as the UI needs it: the generation number plus the fitness summary.
 *
 * <p>This is a view type, not a rule: every value comes from the domain {@link Statistics}, so the
 * chart, the terminal and the sound trigger can never disagree with the simulation.
 *
 * @param generation the generation that just finished
 * @param minFitness the lowest fitness in the population
 * @param maxFitness the highest fitness in the population
 * @param avgFitness the mean fitness
 * @param medianFitness the median fitness
 */
public record EvolutionStatistics(
    int generation, float minFitness, float maxFitness, float avgFitness, float medianFitness) {

  /**
   * Builds the view type from domain statistics.
   *
   * @param statistics the completed generation's statistics
   * @return the UI projection
   * @throws NullPointerException if {@code statistics} is {@code null}
   */
  public static EvolutionStatistics from(Statistics statistics) {
    Objects.requireNonNull(statistics, "statistics");
    return new EvolutionStatistics(
        statistics.getGeneration(),
        statistics.getMinFitness(),
        statistics.getMaxFitness(),
        statistics.getAvgFitness(),
        statistics.getMedianFitness());
  }

  @Override
  public String toString() {
    return String.format(
        "Generation %d: min=%.2f, max=%.2f, avg=%.2f, median=%.2f",
        generation, minFitness, maxFitness, avgFitness, medianFitness);
  }
}
