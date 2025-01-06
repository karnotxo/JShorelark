/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic;

import lombok.RequiredArgsConstructor;

/** Test implementation of Individual. */
@RequiredArgsConstructor
public class TestIndividual implements Individual {
  /** The individual's chromosome. */
  private final Chromosome chromosome;

  /** The individual's fitness. */
  private final float fitness;

  /** Creates a test individual with the given fitness. */
  public static TestIndividual withFitness(float fitness) {
    return new TestIndividual(Chromosome.of(0.0f), fitness);
  }

  /** Factory for creating test individuals. */
  public static final class Factory implements Individual.Factory<TestIndividual> {
    @Override
    public TestIndividual create(Chromosome chromosome) {
      // Calculate fitness as sum of genes, like in Rust implementation
      float fitness = chromosome.stream().reduce(0f, Float::sum);
      return new TestIndividual(chromosome, fitness);
    }
  }

  @Override
  public Chromosome getChromosome() {
    return chromosome;
  }

  @Override
  public float getFitness() {
    return fitness;
  }
}
