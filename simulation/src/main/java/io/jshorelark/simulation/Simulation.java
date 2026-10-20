/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import io.jshorelark.genetic.GeneticAlgorithm;
import io.jshorelark.genetic.crossover.UniformCrossover;
import io.jshorelark.genetic.mutation.GaussianMutation;
import io.jshorelark.genetic.selection.RouletteWheelSelection;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.bird.BirdIndividual;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.events.CollisionListener;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.World;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * One simulation run: a world plus the step clock that drives it.
 *
 * <p>Mirrors the Rust {@code Simulation} in {@code libs/simulation/src/lib.rs}: each {@link #step}
 * processes collisions, runs every brain, moves every bird, and finally checks whether the
 * generation is over. When it is, a new generation is evolved and its statistics are returned.
 *
 * <p>This type owns the only mutable simulation state and knows nothing about rendering, files or
 * threading.
 */
public final class Simulation {

  /** The simulation configuration. */
  private final Config config;

  /** The world being simulated. */
  private final World world;

  /** Observer for food consumption; never null. */
  private final CollisionListener collisionListener;

  /** Steps elapsed in the current generation. */
  private int age;

  /** Generations completed so far. */
  private int generation;

  /**
   * Creates a simulation with a randomly generated world.
   *
   * @param config the simulation configuration
   * @param random the random source used for spawning and food respawns
   * @param collisionListener observer notified on every food consumption
   * @throws IllegalArgumentException if {@code config} or {@code random} is null
   */
  public Simulation(Config config, Random random, CollisionListener collisionListener) {
    this.config = Objects.requireNonNull(config, "config");
    this.world = World.random(config, Objects.requireNonNull(random, "random"));
    this.collisionListener = Objects.requireNonNull(collisionListener, "collisionListener");
  }

  /**
   * Creates a simulation that ignores consumption events.
   *
   * @param config the simulation configuration
   * @param random the random source used for spawning and food respawns
   */
  public Simulation(Config config, Random random) {
    this(config, random, CollisionListener.NONE);
  }

  /**
   * Returns the configuration this simulation was created with.
   *
   * @return the immutable configuration
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Returns an unmodifiable view of the birds in the world.
   *
   * @return the current population
   */
  public List<Bird> getBirds() {
    return world.getBirds();
  }

  /**
   * Returns an unmodifiable view of the food items in the world.
   *
   * @return the current food list
   */
  public List<Food> getFoods() {
    return world.getFoods();
  }

  /**
   * Returns the number of steps elapsed in the current generation.
   *
   * @return the current age, in simulation steps
   */
  public int getAge() {
    return age;
  }

  /**
   * Returns the number of generations completed so far.
   *
   * @return the completed generation count
   */
  public int getGeneration() {
    return generation;
  }

  /**
   * Advances the simulation by one step.
   *
   * <p>The order is part of the behaviour and must not change: collisions, then brains, then
   * movements, then the generation check.
   *
   * @param random the random source used to respawn consumed food
   * @return the statistics of the generation that just ended, or {@code null} if it is still
   *     running
   */
  public Statistics step(Random random) {
    processCollisions(random);
    processBrains();
    processMovements();
    return tryEvolving(random);
  }

  /**
   * Runs the simulation until a generation completes.
   *
   * <p>Mirrors Rust's {@code Simulation::train}.
   *
   * @param random the random source
   * @return the statistics of the completed generation
   * @throws IllegalStateException if the world has no birds, since no generation can complete
   */
  public Statistics train(Random random) {
    if (world.getBirds().isEmpty()) {
      throw new IllegalStateException("Cannot train an empty population");
    }
    while (true) {
      Statistics statistics = step(random);
      if (statistics != null) {
        return statistics;
      }
    }
  }

  /**
   * Adds food items at random positions.
   *
   * @param count how many items to add
   * @param random the random source
   */
  public void addFoods(int count, Random random) {
    for (int i = 0; i < count; i++) {
      world.addFood(Food.random(random));
    }
  }

  /**
   * Adds a bird to the world.
   *
   * @param bird the bird to add
   */
  public void addBird(Bird bird) {
    world.addBird(bird);
  }

  /** Removes every bird from the world. */
  public void clearBirds() {
    world.clearBirds();
  }

  /** Removes every food item from the world. */
  public void clearFoods() {
    world.clearFoods();
  }

  /**
   * Feeds every bird that is close enough to a food item.
   *
   * <p>Mirrors Rust's {@code process_collisions}: the radius is {@link Config#foodSize()} and
   * nothing else, and a consumed item is repositioned in place rather than removed, so the food
   * count stays constant. Because the loop is bird-major, a bird can be credited for several items
   * in one step.
   */
  private void processCollisions(Random random) {
    float foodSize = config.foodSize();
    for (Bird bird : world.getBirds()) {
      for (Food food : world.getFoods()) {
        float distance = bird.getPosition().distance(food.getPosition());
        if (distance <= foodSize) {
          bird.eat();
          world.respawnFood(food, Food.random(random));
          collisionListener.onCollision(CollisionEvent.create(bird, food, distance));
        }
      }
    }
  }

  /** Runs every bird's brain, which refreshes its vision and its controls. */
  private void processBrains() {
    for (Bird bird : world.getBirds()) {
      bird.processBrain(world.getFoods());
    }
  }

  /** Moves every bird according to its current speed and rotation. */
  private void processMovements() {
    for (Bird bird : world.getBirds()) {
      bird.processMovement();
    }
  }

  /**
   * Counts the step and evolves the population when the generation length is reached.
   *
   * <p>Mirrors Rust's {@code try_evolving}, which increments the age first and evolves on {@code
   * age > sim_generation_length}.
   */
  private Statistics tryEvolving(Random random) {
    age++;
    if (age <= config.simGenerationLength()) {
      return null;
    }
    return evolve(random);
  }

  /**
   * Replaces the population with the offspring of the fittest birds.
   *
   * <p>Mirrors Rust's {@code evolve}: reset the clock, build one individual per bird, optionally
   * reverse the fitness, evolve, rebuild the population from the new chromosomes and reposition
   * every food item. The reported statistics belong to the generation that just ended, hence {@code
   * generation - 1}.
   */
  private Statistics evolve(Random random) {
    age = 0;
    generation++;

    var algorithm =
        GeneticAlgorithm.create(
            new RouletteWheelSelection(),
            new UniformCrossover(),
            GaussianMutation.create(config.gaMutChance(), config.gaMutCoeff()),
            new BirdIndividual.Factory(config));

    List<BirdIndividual> individuals = world.getBirds().stream().map(BirdIndividual::of).toList();
    var result = algorithm.evolve(random, individuals);

    world.clearBirds();
    for (BirdIndividual individual : result.getFirst()) {
      world.addBird(individual.toBird(random));
    }
    world.randomizeFoods(random);

    return new Statistics(generation - 1, result.getSecond());
  }
}
