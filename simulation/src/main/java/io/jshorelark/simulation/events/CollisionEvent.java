/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.events;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.Objects;

/**
 * Immutable snapshot of a bird eating a food item.
 *
 * <p>Presentation data only: no simulation rule reads it. It is a record because every component is
 * a value captured at the moment of the collision, so it cannot drift from the world afterwards.
 *
 * @param birdPosition the bird's position when the collision happened
 * @param birdRotation the bird's heading when the collision happened
 * @param birdSatiation the bird's satiation after eating
 * @param foodPosition the food's position before it was repositioned
 * @param distance the distance between bird and food at the collision
 */
public record CollisionEvent(
    Vector2D birdPosition,
    float birdRotation,
    float birdSatiation,
    Vector2D foodPosition,
    float distance) {

  /** Validates the snapshot. */
  public CollisionEvent {
    Objects.requireNonNull(birdPosition, "birdPosition");
    Objects.requireNonNull(foodPosition, "foodPosition");
  }

  /**
   * Creates a collision event from a bird and the food it just ate.
   *
   * @param bird the bird involved in the collision
   * @param food the food involved in the collision
   * @param distance the distance between bird and food
   * @return the collision event
   */
  public static CollisionEvent create(Bird bird, Food food, float distance) {
    return new CollisionEvent(
        bird.getPosition(), bird.getRotation(), bird.getSatiation(), food.position(), distance);
  }
}
