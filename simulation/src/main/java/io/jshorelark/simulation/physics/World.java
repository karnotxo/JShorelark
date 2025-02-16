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
import java.util.Objects;
import java.util.Random;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents the simulation world containing birds and food.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Getter
@Builder(toBuilder = true)
public class World {
  /** The birds in this world. */
  @Getter(AccessLevel.NONE)
  @Builder.Default
  private final List<Bird> birds = new ArrayList<>();

  /** The food in this world. */
  @Getter(AccessLevel.NONE)
  @Builder.Default
  private final List<Food> foods = new ArrayList<>();

  /** The configuration for this world. */
  private final Config config;

  /**
   * Creates a new random world.
   *
   * @param config the configuration
   * @param random the random number generator
   * @return the world
   */
  public static World random(Config config, Random random) {
    World world = builder().config(config).build();

    // Add initial birds
    for (int i = 0; i < config.getWorldAnimals(); i++) {
      world.birds.add(Bird.random(config, random));
    }

    // Add initial food
    for (int i = 0; i < config.getWorldFoods(); i++) {
      world.foods.add(Food.random(random));
    }

    return world;
  }

  /**
   * Sets the food list.
   *
   * @param newFoods the new food list
   */
  public void setFoods(List<Food> newFoods) {
    foods.clear();
    foods.addAll(newFoods);
  }

  /** Clears all birds. */
  public void clearBirds() {
    birds.clear();
  }

  /** Clears all food. */
  public void clearFoods() {
    foods.clear();
  }

  /**
   * Adds a bird.
   *
   * @param bird the bird to add
   */
  public void addBird(Bird bird) {
    birds.add(bird);
  }

  /**
   * Adds a food item.
   *
   * @param food the food to add
   */
  public void addFood(Food food) {
    foods.add(food);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof World)) return false;
    World world = (World) o;
    return birds.equals(world.birds)
        && foods.equals(world.foods)
        && Objects.equals(config, world.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(birds, foods, config);
  }
}
