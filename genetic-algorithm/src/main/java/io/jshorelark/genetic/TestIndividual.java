/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import lombok.Builder;

/**
 * Test implementation of Individual.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Builder
public class TestIndividual implements Individual {
  /** The individual's chromosome. */
  private final Chromosome chromosome;

  /** The individual's fitness. */
  public final float fitness;

  private TestIndividual(final Chromosome chromosome, final float fitness) {
    this.chromosome = chromosome;
    this.fitness = fitness;
  }

  /**
   * Creates a test individual with the given fitness.
   *
   * @param fitness a float
   * @return a {@link io.jshorelark.genetic.TestIndividual} object
   */
  public static TestIndividual withFitness(float fitness) {
    return new TestIndividual(Chromosome.of(fitness), fitness);
  }

  /**
   * Creates a new individual from a chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @return a {@link io.jshorelark.genetic.Individual} object
   */
  public static Individual create(Chromosome chromosome) {
    return new TestIndividual(chromosome, chromosome.stream().reduce(0f, Float::sum));
  }

  /** {@inheritDoc} */
  @Override
  public Chromosome getChromosome() {
    return chromosome;
  }

  /** {@inheritDoc} */
  @Override
  public float getFitness() {
    return chromosome.stream().reduce(0f, Float::sum);
  }
}
