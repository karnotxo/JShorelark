/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.mutation;

import java.util.Random;

/**
 * Implements Gaussian mutation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class GaussianMutation implements MutationMethod {
  private final float chance;
  private final float coeff;

  /**
   * Creates a new mutation with the given parameters.
   *
   * @param chance mutation chance (between 0 and 1)
   * @param coeff mutation coefficient
   * @return a new GaussianMutation instance
   * @throws java.lang.IllegalArgumentException if chance is invalid
   */
  public static GaussianMutation create(float chance, float coeff) {
    validateChance(chance);
    return new GaussianMutation(chance, coeff);
  }

  /** Private constructor - use {@link #create(float, float)} instead. */
  private GaussianMutation(float chance, float coeff) {
    this.chance = chance;
    this.coeff = coeff;
  }

  /**
   * Validates the mutation chance.
   *
   * @param chance mutation chance to validate
   * @throws IllegalArgumentException if chance is invalid
   */
  private static void validateChance(float chance) {
    if (chance < 0.0f || chance > 1.0f) {
      throw new IllegalArgumentException("Chance must be between 0 and 1");
    }
  }

  /** {@inheritDoc} */
  @Override
  public void mutate(final Random random, final float[] genes) {
    if (genes == null) {
      throw new IllegalArgumentException("Genes cannot be null");
    }

    // Mutate genes in place
    for (int i = 0; i < genes.length; i++) {
      // Use nextDouble() for better precision than nextFloat()
      if (random.nextDouble() < chance) {
        float sign = random.nextBoolean() ? 1.0f : -1.0f;
        float delta = sign * coeff * random.nextFloat();
        genes[i] += delta;
      }
    }
  }
}
