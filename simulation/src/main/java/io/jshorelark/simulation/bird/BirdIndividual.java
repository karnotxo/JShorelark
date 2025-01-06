/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.Random;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.genetic.Individual;
import io.jshorelark.simulation.Config;

import lombok.Getter;

/**
 * Represents a bird individual for genetic algorithm.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class BirdIndividual implements Individual {
  /** The bird being wrapped. */
  private final Bird bird;

  /** The config for the simulation. */
  @Getter private final Config config;

  /**
   * Creates a new individual with the given bird and config.
   *
   * @param bird a {@link io.jshorelark.simulation.bird.Bird} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   */
  public BirdIndividual(Bird bird, Config config) {
    this.bird = bird.clone();
    this.config = config;
  }

  /** {@inheritDoc} */
  @Override
  public float getFitness() {
    return bird.getSatiation();
  }

  /** {@inheritDoc} */
  @Override
  public Chromosome getChromosome() {
    return bird.toChromosome();
  }

  /**
   * Creates a new individual from a chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @return a {@link io.jshorelark.simulation.bird.BirdIndividual} object
   */
  public static BirdIndividual fromChromosome(Chromosome chromosome, Config config) {
    return new BirdIndividual(
        Bird.fromChromosome(config, new java.util.Random(), chromosome), config);
  }

  /**
   * Creates a new individual from a bird.
   *
   * @param bird a {@link io.jshorelark.simulation.bird.Bird} object
   * @return a {@link io.jshorelark.simulation.bird.BirdIndividual} object
   */
  public static BirdIndividual of(Bird bird) {
    return new BirdIndividual(bird, bird.getConfig());
  }

  /**
   * Converts this individual back to a bird.
   *
   * @param random a {@link java.util.Random} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @return a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public Bird toBird(Random random, Config config) {
    return Bird.fromChromosome(config, random, getChromosome());
  }

  /** Factory for creating BirdIndividuals from chromosomes. */
  public static class Factory implements Individual.Factory<BirdIndividual> {
    private final Config config;

    public Factory(Config config) {
      this.config = config;
    }

    @Override
    public BirdIndividual create(Chromosome chromosome) {
      return fromChromosome(chromosome, config);
    }
  }
}
