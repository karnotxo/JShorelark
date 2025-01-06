/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.mutation;

import java.util.Random;

/**
 * Implements random reset mutation. Randomly resets genes to new values within a specified range
 * with a given probability.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class RandomResetMutation implements MutationMethod {

  private final float probability;
  private final float minValue;
  private final float maxValue;

  /**
   * Creates a new random reset mutation with the specified parameters.
   *
   * @param probability probability of mutating each gene (between 0 and 1)
   * @param minValue minimum value for reset genes
   * @param maxValue maximum value for reset genes
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   * @throws java.lang.IllegalArgumentException if minValue is greater than maxValue
   */
  public RandomResetMutation(float probability, float minValue, float maxValue) {
    if (probability < 0 || probability > 1) {
      throw new IllegalArgumentException("Probability must be between 0 and 1");
    }
    if (minValue > maxValue) {
      throw new IllegalArgumentException("Minimum value must not be greater than maximum value");
    }
    this.probability = probability;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /** {@inheritDoc} */
  @Override
  public void mutate(final Random random, final float[] genes) {
    if (genes == null) {
      throw new IllegalArgumentException("Chromosome cannot be null");
    }

    // Apply mutation to each gene with given probability
    float range = maxValue - minValue;
    for (int i = 0; i < genes.length; i++) {
      if (random.nextDouble() < probability) {
        float newValue = minValue + random.nextFloat() * range;
        genes[i] = newValue;
      }
    }
  }
}
