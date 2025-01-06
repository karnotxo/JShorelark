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
 * Implements roulette wheel (fitness proportionate) selection. Individuals are selected with
 * probability proportional to their fitness. Higher fitness means higher chance of being selected.
 * This class is equivalent to the RouletteWheelSelection struct in the Rust implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class RouletteWheelSelection implements SelectionMethod {

  private static final float MINIMUM_FITNESS = 0.00001f;

  /** {@inheritDoc} */
  @Override
  public Individual select(Random random, List<? extends Individual> population) {
    if (population == null || population.isEmpty()) {
      throw new IllegalArgumentException("Population cannot be null or empty");
    }

    // Calculate cumulative weights
    float[] cumulativeWeights = new float[population.size()];
    float totalWeight = 0f;

    for (int i = 0; i < population.size(); i++) {
      float fitness = Math.max(population.get(i).getFitness(), MINIMUM_FITNESS);
      totalWeight += fitness;
      cumulativeWeights[i] = totalWeight;
    }

    // Generate random point
    float r = random.nextFloat() * totalWeight;

    // Binary search for the selected individual
    int low = 0;
    int high = population.size() - 1;

    while (low < high) {
      int mid = (low + high) >>> 1;
      if (cumulativeWeights[mid] < r) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }

    return population.get(low);
  }
}
