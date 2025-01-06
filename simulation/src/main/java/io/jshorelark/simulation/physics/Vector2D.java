/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.physics;

import java.util.Random;

import lombok.Value;

/**
 * A 2D vector with x and y coordinates.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class Vector2D implements Cloneable {
  /** Zero vector constant. */
  public static final Vector2D ZERO = new Vector2D(0, 0);

  /** The x coordinate. */
  float x;

  /** The y coordinate. */
  float y;

  /**
   * Creates a new vector at (0,0).
   *
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public static Vector2D zero() {
    return ZERO;
  }

  /**
   * Creates a new vector at (1,0).
   *
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public static Vector2D right() {
    return new Vector2D(1, 0);
  }

  /**
   * Creates a new vector with random coordinates between 0 and 1.
   *
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public static Vector2D random(Random random) {
    return new Vector2D(random.nextFloat(), random.nextFloat());
  }

  /**
   * Creates a new vector from polar coordinates (angle in radians).
   *
   * @param angle a float
   * @param magnitude a float
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public static Vector2D fromPolar(float angle, float magnitude) {
    return new Vector2D(
        (float) (magnitude * Math.cos(angle)), (float) (magnitude * Math.sin(angle)));
  }

  /**
   * Gets the x coordinate.
   *
   * @return a float
   */
  public float x() {
    return x;
  }

  /**
   * Gets the y coordinate.
   *
   * @return a float
   */
  public float y() {
    return y;
  }

  /**
   * Adds another vector to this one.
   *
   * @param other a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D add(Vector2D other) {
    return new Vector2D(x + other.x(), y + other.y());
  }

  /**
   * Subtracts another vector from this one.
   *
   * @param other a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D subtract(Vector2D other) {
    return new Vector2D(x - other.x(), y - other.y());
  }

  /**
   * Scales this vector by a factor.
   *
   * @param factor a float
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D scale(float factor) {
    return new Vector2D(x * factor, y * factor);
  }

  /**
   * Returns the length (magnitude) of this vector.
   *
   * @return a float
   */
  public float length() {
    return (float) Math.sqrt(x * x + y * y);
  }

  /**
   * Returns this vector normalized (length = 1).
   *
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D normalize() {
    float len = length();
    if (len == 0) {
      return Vector2D.zero();
    }
    return new Vector2D(x / len, y / len);
  }

  /**
   * Rotates this vector by an angle in radians (counter-clockwise, matching nalgebra's Rotation2).
   *
   * <p>In nalgebra, Rotation2 is a 2x2 matrix: | cos θ -sin θ | | sin θ cos θ |
   *
   * <p>When applied to a vector [x, y], it gives: [x', y'] = [x * cos θ - y * sin θ, x * sin θ + y
   * * cos θ]
   *
   * @param angle a float
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D rotate(float angle) {
    float cos = (float) Math.cos(angle);
    float sin = (float) Math.sin(angle);
    return new Vector2D(
        x * cos - y * sin, // x' = x * cos θ - y * sin θ
        x * sin + y * cos // y' = x * sin θ + y * cos θ
        );
  }

  /**
   * Returns the angle between this vector and another vector in radians.
   *
   * @param other a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a float
   */
  public float angle(Vector2D other) {
    float dot = x * other.x() + y * other.y();
    float det = x * other.y() - y * other.x();
    return (float) Math.atan2(det, dot);
  }

  /**
   * Returns the squared distance to another vector.
   *
   * @param other a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a float
   */
  public float distanceSquared(Vector2D other) {
    float dx = x - other.x();
    float dy = y - other.y();
    return dx * dx + dy * dy;
  }

  /**
   * Returns the distance to another vector.
   *
   * @param other a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a float
   */
  public float distance(Vector2D other) {
    return (float) Math.sqrt(distanceSquared(other));
  }

  /**
   * Wraps the coordinates between min and max (inclusive).
   *
   * @param min a float
   * @param max a float
   * @return a {@link io.jshorelark.simulation.physics.Vector2D} object
   */
  public Vector2D wrap(float min, float max) {
    float range = max - min;
    float wrappedX = ((x - min) % range + range) % range + min;
    float wrappedY = ((y - min) % range + range) % range + min;
    return new Vector2D(wrappedX, wrappedY);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a copy of this vector.
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Vector2D should be cloneable", e);
    }
  }
}
