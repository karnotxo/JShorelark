/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.evolution;

import java.util.*;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.Simulation;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

import lombok.AccessLevel;
import lombok.Getter;
import reactor.core.publisher.Flux;

/**
 * Manages the evolution of birds in the simulation.
 *
 * @author Jose
 * @version :
 */
public class EvolutionManager {
  /** The simulation being managed. */
  @Getter(AccessLevel.PACKAGE)
  private final Simulation simulation;

  /** The current generation. */
  @Getter private int generation;

  /** The current statistics. */
  @Getter private EvolutionStatistics statistics;

  /**
   * Creates a new evolution manager with default config.
   *
   * @param random a {@link java.util.Random} object
   */
  public EvolutionManager(Random random) {
    this(random, Config.getDefault());
  }

  /**
   * Creates a new evolution manager with the given config.
   *
   * @param random a {@link java.util.Random} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   */
  public EvolutionManager(Random random, Config config) {
    this.simulation = new Simulation(config, random);
    this.generation = 0;
    this.statistics = null;
  }

  /**
   * Updates the simulation and returns statistics if a generation is complete.
   *
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.simulation.ui.evolution.EvolutionStatistics} object
   */
  public EvolutionStatistics update(Random random) {
    // Update simulation
    simulation.update(random);

    // Check if generation is complete
    boolean generationComplete = false;
    for (Bird bird : simulation.getBirds()) {
      if (bird.getSatiation() >= simulation.getConfig().getSimGenerationLength()) {
        generationComplete = true;
        break;
      }
    }

    // If generation complete, evolve population
    if (generationComplete) {
      generation++;
      evolve(random);
      return statistics;
    }

    return null;
  }

  /** Evolves the population. */
  private void evolve(Random random) {
    // Calculate statistics
    float minFitness = Float.MAX_VALUE;
    float maxFitness = Float.MIN_VALUE;
    float totalFitness = 0;
    float[] fitnesses = new float[simulation.getBirds().size()];
    int i = 0;

    for (Bird bird : simulation.getBirds()) {
      float fitness = bird.getSatiation();
      fitnesses[i++] = fitness;
      minFitness = Math.min(minFitness, fitness);
      maxFitness = Math.max(maxFitness, fitness);
      totalFitness += fitness;
    }

    float avgFitness = totalFitness / simulation.getBirds().size();
    Arrays.sort(fitnesses);
    float medianFitness = fitnesses[fitnesses.length / 2];

    statistics =
        new EvolutionStatistics(generation, minFitness, maxFitness, avgFitness, medianFitness);

    // Reset bird positions and satiation
    for (Bird bird : simulation.getBirds()) {
      bird.setPosition(Vector2D.random(random));
      bird.setRotation(random.nextFloat() * (float) (2 * Math.PI));
    }

    // Reset food positions
    for (Food food : simulation.getFoods()) {
      food.setPosition(Vector2D.random(random));
    }
  }

  /**
   * Gets the foods in the simulation.
   *
   * @return a {@link java.util.List} object
   */
  public List<Food> getFoods() {
    return simulation.getFoods();
  }

  /**
   * Gets the food size in the simulation.
   *
   * @return a float
   */
  public float getFoodSize() {
    return simulation.getConfig().getFoodSize();
  }

  /**
   * Gets the birds in the simulation.
   *
   * @return a {@link java.util.List} object
   */
  public List<Bird> getBirds() {
    return simulation.getBirds();
  }

  /**
   * Adds food items to the simulation.
   *
   * @param count a int
   * @param random a {@link java.util.Random} object
   */
  public void addFoods(int count, Random random) {
    simulation.addFoods(count, random);
  }

  /**
   * Gets the bird size.
   *
   * @return the bird size
   */
  public double getBirdSize() {
    return simulation.getConfig().getBirdSize();
  }

  /**
   * Gets the eye FOV angle.
   *
   * @return the eye FOV angle
   */
  public double getEyeFovAngle() {
    return simulation.getConfig().getEyeFovAngle();
  }

  /**
   * Gets the number of eye cells.
   *
   * @return the number of eye cells
   */
  public int getEyeCells() {
    return simulation.getConfig().getEyeCells();
  }

  /**
   * Gets the eye FOV range.
   *
   * @return the eye FOV range
   */
  public double getEyeFovRange() {
    return simulation.getConfig().getEyeFovRange();
  }

  /**
   * Gets the world width.
   *
   * @return the world width
   */
  public double getWorldWidth() {
    return simulation.getConfig().getWorldWidth();
  }

  /**
   * Gets the world height.
   *
   * @return the world height
   */
  public double getWorldHeight() {
    return simulation.getConfig().getWorldHeight();
  }

  /**
   * Gets the simulation generation length.
   *
   * @return the simulation generation length
   */
  public double getSimGenerationLength() {
    return simulation.getConfig().getSimGenerationLength();
  }

  /**
   * Gets the collision events.
   *
   * @return the collision events
   */
  public Flux<CollisionEvent> getCollisionEvents() {
    return simulation.getCollisionEvents();
  }
}
