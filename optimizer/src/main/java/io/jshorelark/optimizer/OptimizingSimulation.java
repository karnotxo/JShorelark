/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import java.util.*;

import io.jshorelark.genetic.GeneticAlgorithm;
import io.jshorelark.genetic.crossover.UniformCrossover;
import io.jshorelark.genetic.mutation.GaussianMutation;
import io.jshorelark.genetic.selection.RouletteWheelSelection;
import io.jshorelark.genetic.statistics.Statistics;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.Simulation;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.bird.BirdIndividual;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

import lombok.Getter;

/**
 * Simulation wrapper for optimization experiments. Matches Rust's optimizer::Simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class OptimizingSimulation {
  /** The core simulation. */
  private final Simulation simulation;

  /** The current age (steps in current generation). */
  @Getter private int age;

  /** The current generation. */
  @Getter private int generation;

  /** The current statistics. */
  private Statistics currentStats;

  /**
   * Creates a new optimizing simulation.
   *
   * @param config a {@link io.jshorelark.optimizer.OptimizationConfig} object
   * @param random a {@link java.util.Random} object
   */
  public OptimizingSimulation(OptimizationConfig config, Random random) {
    this.simulation = new Simulation(config.toSimulationConfig(), random);
    this.age = 0;
    this.generation = 0;
  }

  /**
   * Gets the simulation configuration.
   *
   * @return a {@link io.jshorelark.simulation.Config} object
   */
  public Config getConfig() {
    return simulation.getConfig();
  }

  /**
   * Trains the simulation until a generation is complete.
   *
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.optimizer.OptimizationStatistics} object
   */
  public OptimizationStatistics train(Random random) {
    while (true) {
      var stats = step(random);
      if (stats != null) {
        return OptimizationStatistics.fromGaStats(stats, generation - 1);
      }
    }
  }

  /**
   * Steps the simulation forward. Returns statistics if a generation is complete.
   *
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.genetic.statistics.Statistics} object
   */
  public Statistics step(Random random) {
    simulation.update(random);
    return tryEvolving(random);
  }

  /** Tries to evolve the population. Returns statistics if successful. */
  private Statistics tryEvolving(Random random) {
    age++;
    if (age >= simulation.getConfig().getSimGenerationLength()) {
      age = 0;
      generation++;
      return evolve(random);
    }
    return null;
  }

  /** Evolves the population. Returns statistics if successful. */
  private Statistics evolve(Random random) {
    // Get current population
    List<BirdIndividual> individuals = new ArrayList<>();
    for (Bird bird : simulation.getBirds()) {
      individuals.add(BirdIndividual.of(bird));
    }

    // If no individuals, create new random ones
    if (individuals.isEmpty()) {
      for (int i = 0; i < simulation.getConfig().getWorldAnimals(); i++) {
        var bird = Bird.random(simulation.getConfig(), random);
        individuals.add(BirdIndividual.of(bird));
      }
    }

    // Create genetic algorithm
    var algorithm =
        GeneticAlgorithm.create(
            new RouletteWheelSelection(),
            new UniformCrossover(),
            GaussianMutation.create(
                simulation.getConfig().getGaMutChance(), simulation.getConfig().getGaMutCoeff()),
            new BirdIndividual.Factory(simulation.getConfig()));

    // Evolve population
    var result = algorithm.evolve(random, individuals);
    final var newPopulation = result.getFirst();

    // Store current stats
    currentStats = result.getSecond();

    // Convert individuals back to birds and reset world
    simulation.clearBirds();
    for (final var individual : newPopulation) {
      simulation.addBird(((BirdIndividual) individual).toBird(random, simulation.getConfig()));
    }

    // Reset food positions
    for (Food food : simulation.getFoods()) {
      food.setPosition(Vector2D.random(random));
    }

    return currentStats;
  }

  /**
   * Gets the current statistics.
   *
   * @return a {@link io.jshorelark.optimizer.OptimizationStatistics} object
   */
  public OptimizationStatistics getCurrentStats() {
    return OptimizationStatistics.fromGaStats(currentStats, generation);
  }
}
