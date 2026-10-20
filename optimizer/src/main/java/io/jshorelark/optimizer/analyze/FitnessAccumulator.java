/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.analyze;

import io.jshorelark.optimizer.OptimizationStatistics;

/**
 * Sums the fitness values of every sample of one configuration so that {@link AnalyzeCommand} can
 * report a per-configuration mean, mirroring Rust's {@code StatsAccumulator}.
 */
final class FitnessAccumulator {

  /** Number of samples folded in so far. */
  private int samples;

  /** Running sums, divided by {@link #samples} when averaging. */
  private float minFitness;

  private float maxFitness;
  private float avgFitness;
  private float medianFitness;

  /**
   * Folds one generation's statistics into the running sums.
   *
   * @param stats the statistics of one generation
   */
  void add(OptimizationStatistics stats) {
    samples++;
    minFitness += stats.minFitness();
    maxFitness += stats.maxFitness();
    avgFitness += stats.avgFitness();
    medianFitness += stats.medianFitness();
  }

  /**
   * Divides the running sums by the sample count.
   *
   * @return the averaged statistics, or all zeros when nothing was accumulated
   */
  OptimizationStatistics average() {
    if (samples == 0) {
      return new OptimizationStatistics(0.0f, 0.0f, 0.0f, 0.0f);
    }
    float count = samples;
    return new OptimizationStatistics(
        minFitness / count, maxFitness / count, avgFitness / count, medianFitness / count);
  }
}
