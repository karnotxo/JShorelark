/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation;

import java.util.*;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.World;

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Core simulation that manages the world state. Matches Rust's wsm::Simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class Simulation {
  /** The simulation configuration. */
  @Getter private final Config config;

  /** The world being simulated. */
  private final World world;

  /** Sink for collision events. */
  private final Sinks.Many<CollisionEvent> collisionSink;

  /**
   * Creates a new simulation with the given config.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @param random a {@link java.util.Random} object
   */
  public Simulation(Config config, Random random) {
    this.config = config;
    this.world = World.random(config, random);
    this.collisionSink = Sinks.many().multicast().onBackpressureBuffer();
  }

  /**
   * Gets an unmodifiable view of the birds in this world.
   *
   * @return a {@link java.util.List} object
   */
  public List<Bird> getBirds() {
    return world.getBirds();
  }

  /**
   * Gets an unmodifiable view of the food in this world.
   *
   * @return a {@link java.util.List} object
   */
  public List<Food> getFoods() {
    return world.getFoods();
  }

  /**
   * Adds food items to the world.
   *
   * @param count a int
   * @param random a {@link java.util.Random} object
   */
  public void addFoods(int count, Random random) {
    for (int i = 0; i < count; i++) {
      world.addFood(Food.random(random));
    }
  }

  /**
   * Updates the simulation state.
   *
   * @param random a {@link java.util.Random} object
   */
  public void update(Random random) {
    processCollisions(random);
    processBrains();
    processMovements();
  }

  /** Processes collisions between birds and food. */
  private void processCollisions(Random random) {
    List<Food> newFoods = new ArrayList<>();
    for (Food food : world.getFoods()) {
      boolean wasConsumed = false;
      for (Bird bird : world.getBirds()) {
        float distance = bird.getPosition().distance(food.getPosition());
        float collisionThreshold = config.getBirdSize() + config.getFoodSize();
        if (distance <= collisionThreshold) {
          bird.eat();
          wasConsumed = true;
          // Emit collision event
          onCollision(CollisionEvent.create(bird, food, distance));
          break;
        }
      }
      if (wasConsumed) {
        // Replace consumed food with new food at random position
        newFoods.add(Food.random(random));
      } else {
        newFoods.add(food);
      }
    }
    world.setFoods(newFoods);
  }

  /** Processes bird brains. */
  private void processBrains() {
    for (Bird bird : world.getBirds()) {
      bird.processBrain(world.getFoods(), config);
    }
  }

  /** Processes bird movements. */
  private void processMovements() {
    for (Bird bird : world.getBirds()) {
      bird.processMovement();
    }
  }

  /** Clears all birds from the world. */
  public void clearBirds() {
    world.clearBirds();
  }

  /** Clears all foods from the world. */
  public void clearFoods() {
    world.clearFoods();
  }

  /**
   * Adds a bird to the world.
   *
   * @param bird a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public void addBird(Bird bird) {
    world.addBird(bird);
  }

  /** Steps the simulation forward one tick. */
  public void step() {
    processCollisions(new Random());
    processBrains();
    processMovements();
  }

  /** Handles a collision event. */
  private void onCollision(CollisionEvent event) {
    collisionSink.tryEmitNext(event);
  }

  /** Gets an unmodifiable view of the collision events. */
  public Flux<CollisionEvent> getCollisionEvents() {
    return collisionSink.asFlux();
  }
}
