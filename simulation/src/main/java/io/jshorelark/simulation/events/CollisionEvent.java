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

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * Represents a collision event between a bird and food. This is an immutable snapshot of the
 * collision state.
 */
@RecordBuilder
public record CollisionEvent(
    Vector2D birdPosition,
    float birdRotation,
    float birdSatiation,
    Vector2D foodPosition,
    float distance)
    implements CollisionEventBuilder.With {

  /**
   * Creates a new collision event.
   *
   * @param bird the bird involved in the collision
   * @param food the food involved in the collision
   * @param distance the distance between bird and food at collision
   * @return a new collision event
   */
  public static CollisionEvent create(Bird bird, Food food, float distance) {
    return new CollisionEvent(
        bird.getPosition(), bird.getRotation(), bird.getSatiation(), food.getPosition(), distance);
  }
}
