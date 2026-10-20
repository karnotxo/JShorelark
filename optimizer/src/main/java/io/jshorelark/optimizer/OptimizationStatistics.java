/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jshorelark.genetic.statistics.Statistics;
import java.util.Objects;

/**
 * Fitness summary of one generation, as persisted in the optimizer's JSONL log.
 *
 * <p>Mirrors Rust's {@code OptStatistics} exactly: four values with single-letter JSON names. The
 * generation number is not part of it — {@link OptimizationContext} carries it — so the serialised
 * shape stays byte-compatible with the reference implementation.
 *
 * @param minFitness the lowest fitness in the population
 * @param maxFitness the highest fitness in the population
 * @param avgFitness the mean fitness
 * @param medianFitness the median fitness
 */
public record OptimizationStatistics(
    @JsonProperty("a") float minFitness,
    @JsonProperty("b") float maxFitness,
    @JsonProperty("c") float avgFitness,
    @JsonProperty("d") float medianFitness) {

  /**
   * Copies the domain statistics into the persisted shape.
   *
   * @param statistics the genetic algorithm statistics of the finished generation
   * @return the optimizer's projection
   * @throws NullPointerException if {@code statistics} is {@code null}
   */
  public static OptimizationStatistics from(Statistics statistics) {
    Objects.requireNonNull(statistics, "statistics");
    return new OptimizationStatistics(
        statistics.getMinFitness(),
        statistics.getMaxFitness(),
        statistics.getAvgFitness(),
        statistics.getMedianFitness());
  }
}
