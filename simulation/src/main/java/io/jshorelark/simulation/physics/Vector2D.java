/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.physics;

import java.util.Random;

/**
 * An immutable 2D vector in model space (unit square, Y up).
 *
 * <p>Java counterpart of the {@code nalgebra} {@code Point2}/{@code Vector2} operations the Rust
 * simulation uses. All arithmetic returns new instances; no method mutates {@code this}.
 *
 * @param x the horizontal component
 * @param y the vertical component
 */
public record Vector2D(float x, float y) {

  /** The origin. */
  public static final Vector2D ZERO = new Vector2D(0.0f, 0.0f);

  /**
   * Returns the origin.
   *
   * @return the zero vector
   */
  public static Vector2D zero() {
    return ZERO;
  }

  /**
   * Returns the unit vector along +X.
   *
   * @return the right vector
   */
  public static Vector2D right() {
    return new Vector2D(1.0f, 0.0f);
  }

  /**
   * Returns a vector with both coordinates drawn uniformly from {@code [0, 1)}.
   *
   * @param random the random source
   * @return a random point inside the world
   */
  public static Vector2D random(Random random) {
    return new Vector2D(random.nextFloat(), random.nextFloat());
  }

  /**
   * Builds a vector from an angle and a magnitude.
   *
   * @param angle the angle in radians, counter-clockwise from +X
   * @param magnitude the length of the resulting vector
   * @return the polar vector
   */
  public static Vector2D fromPolar(float angle, float magnitude) {
    return new Vector2D(
        (float) (magnitude * Math.cos(angle)), (float) (magnitude * Math.sin(angle)));
  }

  /**
   * Adds another vector.
   *
   * @param other the vector to add
   * @return the sum
   */
  public Vector2D add(Vector2D other) {
    return new Vector2D(x + other.x, y + other.y);
  }

  /**
   * Subtracts another vector.
   *
   * @param other the vector to subtract
   * @return the difference
   */
  public Vector2D subtract(Vector2D other) {
    return new Vector2D(x - other.x, y - other.y);
  }

  /**
   * Multiplies both components by a factor.
   *
   * @param factor the scale factor
   * @return the scaled vector
   */
  public Vector2D scale(float factor) {
    return new Vector2D(x * factor, y * factor);
  }

  /**
   * Returns the Euclidean length.
   *
   * @return the magnitude
   */
  public float length() {
    return (float) Math.sqrt(distanceSquared(ZERO));
  }

  /**
   * Returns a unit vector with the same direction, or the origin for the zero vector.
   *
   * @return the normalised vector
   */
  public Vector2D normalize() {
    float len = length();
    return len == 0.0f ? ZERO : new Vector2D(x / len, y / len);
  }

  /**
   * Rotates this vector counter-clockwise.
   *
   * <p>Reproduces {@code nalgebra::Rotation2}, whose matrix is {@code [[cos, -sin], [sin, cos]]}.
   *
   * @param angle the rotation in radians
   * @return the rotated vector
   */
  public Vector2D rotate(float angle) {
    float cos = (float) Math.cos(angle);
    float sin = (float) Math.sin(angle);
    return new Vector2D(x * cos - y * sin, x * sin + y * cos);
  }

  /**
   * Returns the signed angle from this vector to {@code other}.
   *
   * @param other the target vector
   * @return the angle in radians, in {@code (-π, π]}
   */
  public float angle(Vector2D other) {
    float dot = x * other.x + y * other.y;
    float det = x * other.y - y * other.x;
    return (float) Math.atan2(det, dot);
  }

  /**
   * Returns the squared distance to another vector.
   *
   * <p>Preferred over {@link #distance} in per-tick comparisons because it avoids a square root.
   *
   * @param other the other vector
   * @return the squared distance
   */
  public float distanceSquared(Vector2D other) {
    float dx = x - other.x;
    float dy = y - other.y;
    return dx * dx + dy * dy;
  }

  /**
   * Returns the Euclidean distance to another vector.
   *
   * @param other the other vector
   * @return the distance
   */
  public float distance(Vector2D other) {
    return (float) Math.sqrt(distanceSquared(other));
  }

  /**
   * Wraps both components into {@code [min, max)}.
   *
   * <p>Mirrors {@code nalgebra::wrap}: a component equal to {@code max} wraps to {@code min}, which
   * keeps the toroidal world a half-open square.
   *
   * @param min the inclusive lower bound
   * @param max the exclusive upper bound
   * @return the wrapped vector
   */
  public Vector2D wrap(float min, float max) {
    float range = max - min;
    return new Vector2D(wrapComponent(x, min, range), wrapComponent(y, min, range));
  }

  private static float wrapComponent(float value, float min, float range) {
    float wrapped = (value - min) % range;
    return wrapped < 0.0f ? wrapped + range + min : wrapped + min;
  }
}
