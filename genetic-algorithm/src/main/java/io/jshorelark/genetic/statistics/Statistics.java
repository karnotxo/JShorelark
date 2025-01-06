/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.statistics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.jshorelark.genetic.Individual;

import lombok.Getter;

/**
 * Statistics about a population.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Getter
public class Statistics {
  /** Minimum fitness. */
  private final float minFitness;

  /** Maximum fitness. */
  private final float maxFitness;

  /** Average fitness. */
  private final float avgFitness;

  /** Median fitness. */
  private final float medianFitness;

  /**
   * Creates statistics with the given values.
   *
   * @param minFitness a float
   * @param maxFitness a float
   * @param avgFitness a float
   * @param medianFitness a float
   */
  @JsonCreator
  public Statistics(
      @JsonProperty("minFitness") float minFitness,
      @JsonProperty("maxFitness") float maxFitness,
      @JsonProperty("avgFitness") float avgFitness,
      @JsonProperty("medianFitness") float medianFitness) {
    this.minFitness = minFitness;
    this.maxFitness = maxFitness;
    this.avgFitness = avgFitness;
    this.medianFitness = medianFitness;
  }

  /**
   * Creates statistics from a population.
   *
   * @param population a {@link java.util.List} object
   * @return a {@link io.jshorelark.genetic.statistics.Statistics} object
   */
  public static Statistics of(List<? extends Individual> population) {
    if (population.isEmpty()) {
      throw new IllegalArgumentException("Population must not be empty");
    }

    float minFitness = Float.POSITIVE_INFINITY;
    float maxFitness = Float.NEGATIVE_INFINITY;
    float sumFitness = 0;

    // Calculate min, max, and sum
    for (Individual individual : population) {
      float fitness = individual.getFitness();
      minFitness = Math.min(minFitness, fitness);
      maxFitness = Math.max(maxFitness, fitness);
      sumFitness += fitness;
    }

    // Calculate average
    float avgFitness = sumFitness / population.size();

    // Calculate median
    float[] fitnesses = new float[population.size()];
    for (int i = 0; i < population.size(); i++) {
      fitnesses[i] = population.get(i).getFitness();
    }
    java.util.Arrays.sort(fitnesses);
    float medianFitness =
        population.size() % 2 == 0
            ? (fitnesses[population.size() / 2 - 1] + fitnesses[population.size() / 2]) / 2
            : fitnesses[population.size() / 2];

    return new Statistics(minFitness, maxFitness, avgFitness, medianFitness);
  }

  /**
   * Gets the minimum fitness.
   *
   * @return a float
   */
  public float getMinFitness() {
    return minFitness;
  }

  /**
   * Gets the maximum fitness.
   *
   * @return a float
   */
  public float getMaxFitness() {
    return maxFitness;
  }

  /**
   * Gets the average fitness.
   *
   * @return a float
   */
  public float getAvgFitness() {
    return avgFitness;
  }

  /**
   * Gets the median fitness.
   *
   * @return a float
   */
  public float getMedianFitness() {
    return medianFitness;
  }
}
