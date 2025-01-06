/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;

import lombok.Getter;

/**
 * Represents the simulation world containing birds and food.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class World {
  /** The birds in the world. */
  private final List<Bird> birds = new ArrayList<>();

  /** The food in the world. */
  private final List<Food> foods = new ArrayList<>();

  /** The configuration for this world. */
  @Getter private final Config config;

  /**
   * Creates a new empty world with the given config.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   */
  public World(Config config) {
    this.config = config;
  }

  /**
   * Creates a new world with random birds and food based on the config.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.simulation.physics.World} object
   */
  public static World random(Config config, Random random) {
    World world = new World(config);

    // Add random birds
    for (int i = 0; i < config.getWorldAnimals(); i++) {
      world.birds.add(Bird.random(config, random));
    }

    // Add random food
    for (int i = 0; i < config.getWorldFoods(); i++) {
      world.foods.add(Food.random(random));
    }

    return world;
  }

  /**
   * Gets an unmodifiable view of the birds in this world.
   *
   * @return a {@link java.util.List} object
   */
  public List<Bird> getBirds() {
    return Collections.unmodifiableList(birds);
  }

  /**
   * Gets an unmodifiable view of the food in this world.
   *
   * @return a {@link java.util.List} object
   */
  public List<Food> getFoods() {
    return Collections.unmodifiableList(foods);
  }

  /**
   * Adds a bird to the world.
   *
   * @param bird a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public void addBird(Bird bird) {
    birds.add(bird);
  }

  /**
   * Adds food to the world.
   *
   * @param food a {@link io.jshorelark.simulation.food.Food} object
   */
  public void addFood(Food food) {
    foods.add(food);
  }

  /** Clears all birds from the world. */
  public void clearBirds() {
    birds.clear();
  }

  /** Clears all food from the world. */
  public void clearFoods() {
    foods.clear();
  }
}
