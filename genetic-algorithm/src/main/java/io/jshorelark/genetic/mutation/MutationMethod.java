/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.mutation;

import java.util.Random;

/**
 * Defines a method for mutating chromosomes.
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface MutationMethod {
  /**
   * Mutates a chromosome by applying random changes to its genes.
   *
   * @param random the random number generator to use
   * @param genes the genes to mutate
   * @throws java.lang.IllegalArgumentException if chromosome is null or chance is not between 0 and
   *     1
   */
  void mutate(Random random, final float[] genes);
}
