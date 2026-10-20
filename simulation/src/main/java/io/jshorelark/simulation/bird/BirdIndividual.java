/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.genetic.Individual;
import io.jshorelark.simulation.Config;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A bird seen as a genetic-algorithm individual: its chromosome plus the fitness it earned.
 *
 * <p>Mirrors Rust's {@code AnimalIndividual}. The fitness is captured once, when the individual is
 * created from a bird, so later mutations of the bird cannot change a finished generation's score.
 * Reversing the fitness (the {@code ga_reverse} experiment) is then a matter of rewriting that
 * captured value, exactly as Rust does.
 */
public final class BirdIndividual implements Individual {

  /** Chromosome of the bird that earned this fitness. */
  private final Chromosome chromosome;

  /** Captured fitness (satiation, or its reverse). */
  private final float fitness;

  /** Configuration used to rebuild a bird from the chromosome. */
  private final Config config;

  private BirdIndividual(Chromosome chromosome, float fitness, Config config) {
    this.chromosome = Objects.requireNonNull(chromosome, "chromosome");
    this.fitness = fitness;
    this.config = Objects.requireNonNull(config, "config");
  }

  /**
   * Captures a bird's chromosome and current satiation as a new individual.
   *
   * @param bird the bird to capture
   * @return the individual representing that bird's performance
   */
  public static BirdIndividual of(Bird bird) {
    Objects.requireNonNull(bird, "bird");
    return new BirdIndividual(bird.toChromosome(), bird.getSatiation(), bird.getConfig());
  }

  /**
   * Creates an unevaluated individual from a chromosome.
   *
   * <p>Used when a new population has to be built without a preceding evaluation.
   *
   * @param chromosome the chromosome
   * @param config the configuration for the bird this individual will produce
   * @return an individual with zero fitness
   */
  public static BirdIndividual fromChromosome(Chromosome chromosome, Config config) {
    return new BirdIndividual(chromosome, 0.0f, config);
  }

  /**
   * Returns a copy of this individual with the given fitness.
   *
   * @param newFitness the replacement fitness
   * @return a new individual sharing the same chromosome
   */
  public BirdIndividual withFitness(float newFitness) {
    return new BirdIndividual(chromosome, newFitness, config);
  }

  /**
   * Inverts every fitness against the best one in the population.
   *
   * <p>Mirrors Rust's {@code ga_reverse == 1} branch: {@code fitness = max_satiation - fitness},
   * which turns selection into "survival of the worst".
   *
   * @param individuals the evaluated population
   * @return a new list with inverted fitness values
   */
  public static List<BirdIndividual> reverseFitness(List<BirdIndividual> individuals) {
    if (individuals.isEmpty()) {
      return List.of();
    }
    float maxFitness =
        individuals.stream().map(BirdIndividual::getFitness).max(Float::compare).orElseThrow();
    return individuals.stream().map(i -> i.withFitness(maxFitness - i.fitness)).toList();
  }

  @Override
  public Chromosome getChromosome() {
    return chromosome;
  }

  @Override
  public float getFitness() {
    return fitness;
  }

  /**
   * Builds the bird that carries this individual's chromosome.
   *
   * <p>The returned bird starts at a fresh random position and rotation, as in Rust's {@code
   * AnimalIndividual::into_animal}.
   *
   * @param random the random source for the new bird's placement
   * @return a new bird with this chromosome
   */
  public Bird toBird(Random random) {
    return Bird.fromChromosome(config, random, chromosome);
  }

  /** Factory that rebuilds individuals from chromosomes for the genetic algorithm. */
  public static final class Factory implements Individual.Factory<BirdIndividual> {

    private final Config config;

    /**
     * Creates a factory bound to a configuration.
     *
     * @param config the configuration for the birds it will produce
     */
    public Factory(Config config) {
      this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public BirdIndividual create(Chromosome chromosome) {
      return fromChromosome(chromosome, config);
    }
  }
}
