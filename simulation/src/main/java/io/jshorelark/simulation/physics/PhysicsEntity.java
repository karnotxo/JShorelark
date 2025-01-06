/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.physics;

/**
 * Interface for objects that have physical properties in the simulation. This includes position,
 * velocity, and radius for collision detection.
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface PhysicsEntity {
  /**
   * Gets the current position of the entity.
   *
   * @return position vector
   */
  Vector2D getPosition();

  /**
   * Sets the position of the entity.
   *
   * @param position new position vector
   */
  void setPosition(Vector2D position);

  /**
   * Gets the current velocity of the entity.
   *
   * @return velocity vector
   */
  Vector2D getVelocity();

  /**
   * Sets the velocity of the entity.
   *
   * @param velocity new velocity vector
   */
  void setVelocity(Vector2D velocity);

  /**
   * Gets the radius of the entity for collision detection.
   *
   * @return radius
   */
  float getRadius();
}
