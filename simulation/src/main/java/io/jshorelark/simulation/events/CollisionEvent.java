/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.events;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a collision event between a bird and food. This is an immutable snapshot of the
 * collision state.
 */
@Value
@Builder
public class CollisionEvent {
  /** Position of the bird when collision occurred. */
  Vector2D birdPosition;

  /** Rotation of the bird when collision occurred. */
  float birdRotation;

  /** Satiation level of the bird when collision occurred. */
  float birdSatiation;

  /** Position of the food when collision occurred. */
  Vector2D foodPosition;

  /** Distance between bird and food at collision. */
  float distance;

  /**
   * Creates a collision event from a bird and food.
   *
   * @param bird the bird involved in the collision
   * @param food the food involved in the collision
   * @param distance the distance between bird and food
   * @return the collision event
   */
  public static CollisionEvent create(Bird bird, Food food, float distance) {
    return builder()
        .birdPosition(bird.getPosition())
        .birdRotation(bird.getRotation())
        .birdSatiation(bird.getSatiation())
        .foodPosition(food.getPosition())
        .distance(distance)
        .build();
  }
}
