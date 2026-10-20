/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.physics;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The container that holds everything that exists in a simulation: birds and food.
 *
 * <p>Mirrors Rust's {@code World}. It is deliberately a plain mutable holder — the simulation owns
 * the rules, the world only stores the entities. Lists are exposed as unmodifiable views so callers
 * cannot bypass the mutation methods.
 */
public final class World {

  private final List<Bird> birds = new ArrayList<>();
  private final List<Food> foods = new ArrayList<>();
  private final Config config;

  private World(Config config) {
    this.config = config;
  }

  /**
   * Creates a world populated with the configured number of random birds and food items.
   *
   * @param config the simulation configuration
   * @param random the random source
   * @return a fully populated world
   */
  public static World random(Config config, Random random) {
    Objects.requireNonNull(config, "config");
    Objects.requireNonNull(random, "random");
    var world = new World(config);
    for (int i = 0; i < config.worldAnimals(); i++) {
      world.birds.add(Bird.random(config, random));
    }
    for (int i = 0; i < config.worldFoods(); i++) {
      world.foods.add(Food.random(random));
    }
    return world;
  }

  /**
   * Creates an empty world.
   *
   * @param config the simulation configuration
   * @return a world with no birds and no food
   */
  public static World empty(Config config) {
    return new World(Objects.requireNonNull(config, "config"));
  }

  /**
   * Returns the configuration this world belongs to.
   *
   * @return the configuration
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Returns the birds, in insertion order.
   *
   * @return an unmodifiable view of the population
   */
  public List<Bird> getBirds() {
    // A read-only view, not a copy: this is read on every step and by every render frame.
    return Collections.unmodifiableList(birds);
  }

  /**
   * Returns the food items, in insertion order.
   *
   * @return an unmodifiable view of the food list
   */
  public List<Food> getFoods() {
    return Collections.unmodifiableList(foods);
  }

  /**
   * Adds a bird.
   *
   * @param bird the bird to add
   */
  public void addBird(Bird bird) {
    birds.add(Objects.requireNonNull(bird, "bird"));
  }

  /**
   * Adds a food item.
   *
   * @param food the food to add
   */
  public void addFood(Food food) {
    foods.add(Objects.requireNonNull(food, "food"));
  }

  /** Removes every bird. */
  public void clearBirds() {
    birds.clear();
  }

  /** Removes every food item. */
  public void clearFoods() {
    foods.clear();
  }

  /**
   * Replaces a consumed food item in place, keeping the food count constant.
   *
   * <p>Mirrors Rust's {@code food.position = rng.gen()}: the item is repositioned, not removed.
   *
   * @param consumed the item that was eaten
   * @param replacement the item that takes its slot
   */
  public void respawnFood(Food consumed, Food replacement) {
    int index = foods.indexOf(consumed);
    if (index < 0) {
      throw new IllegalArgumentException("Food item does not belong to this world: " + consumed);
    }
    foods.set(index, Objects.requireNonNull(replacement, "replacement"));
  }

  /**
   * Moves every food item to a new random position.
   *
   * @param random the random source
   */
  public void randomizeFoods(Random random) {
    foods.replaceAll(ignored -> Food.random(random));
  }
}
