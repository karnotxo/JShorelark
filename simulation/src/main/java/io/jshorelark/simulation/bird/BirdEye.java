/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.List;
import java.util.Objects;

/**
 * The bird's segmented field of view.
 *
 * <p>Mirrors Rust's {@code Eye}: the field of view is split into {@code eyeCells} equal bins; each
 * visible food item adds {@code (fovRange - distance) / fovRange} to exactly one bin, so nearer
 * food produces a stronger signal. The strength is a pure function of geometry and is what the
 * brain consumes as input.
 */
public final class BirdEye {

  private final float fovRange;
  private final float fovAngle;
  private final int cells;

  /**
   * Creates an eye configured from the simulation configuration.
   *
   * @param config the simulation configuration
   * @throws IllegalArgumentException if the field of view range, angle or cell count is not
   *     positive
   */
  public BirdEye(Config config) {
    Objects.requireNonNull(config, "config");
    if (config.eyeFovRange() <= 0.0f) {
      throw new IllegalArgumentException("eyeFovRange must be positive");
    }
    if (config.eyeFovAngle() <= 0.0f) {
      throw new IllegalArgumentException("eyeFovAngle must be positive");
    }
    if (config.eyeCells() <= 0) {
      throw new IllegalArgumentException("eyeCells must be positive");
    }
    this.fovRange = config.eyeFovRange();
    this.fovAngle = config.eyeFovAngle();
    this.cells = config.eyeCells();
  }

  /**
   * Builds the vision vector for a position and heading.
   *
   * <p>Mirrors Rust's {@code Eye::process_vision} exactly:
   *
   * <ol>
   *   <li>the angle to the food is measured from +Y, counter-clockwise positive. {@code
   *       Rotation2::rotation_between(&Vector2::y(), &vec)} computes {@code atan2(y.perp(vec),
   *       y.dot(vec))}, and for the 2D cross product that is {@code atan2(-vec.x, vec.y)}. The
   *       minus sign is what makes a bird's left and right distinguishable; dropping it mirrors the
   *       field of view.
   *   <li>the heading is subtracted and the result wrapped into {@code [-π, π]};
   *   <li>food outside the field of view is skipped;
   *   <li>the remaining angle is shifted into {@code [0, fovAngle]} to pick a bin.
   * </ol>
   *
   * @param position the bird's position
   * @param rotation the bird's heading in radians
   * @param foods the food items to look at
   * @return one strength value per cell
   */
  public float[] processVision(Vector2D position, float rotation, List<Food> foods) {
    float[] vision = new float[cells];
    float halfAngle = fovAngle / 2.0f;

    for (Food food : foods) {
      Vector2D offset = food.position().subtract(position);
      float distance = offset.length();
      if (distance > fovRange) {
        continue;
      }

      // Angle from +Y, matching Rotation2::rotation_between(&Vector2::y(), &vec): nalgebra's
      // rotation_between is atan2(y.perp(vec), y.dot(vec)), i.e. atan2(-vec.x, vec.y). The heading
      // is read back the way `Rotation2::angle()` reads it, through atan2(sin, cos), because Rust
      // stores the heading as a rotation matrix and not as the float we were handed.
      float angle = (float) Math.atan2(-offset.x(), offset.y()) - readRotation(rotation);
      angle = wrapAngle(angle);

      if (angle < -halfAngle || angle > halfAngle) {
        continue;
      }

      float shifted = angle + halfAngle;
      int cell = Math.min((int) (shifted / fovAngle * cells), cells - 1);
      vision[cell] += (fovRange - distance) / fovRange;
    }

    return vision;
  }

  /**
   * Returns the number of vision cells.
   *
   * @return the cell count
   */
  public int getCells() {
    return cells;
  }

  /**
   * Returns the total field of view in radians.
   *
   * @return the field of view angle
   */
  public float getFovAngle() {
    return fovAngle;
  }

  /**
   * Returns the maximum vision distance in world units.
   *
   * @return the field of view range
   */
  public float getFovRange() {
    return fovRange;
  }

  /**
   * Reads a rotation the way {@code Rotation2::angle()} reads it back.
   *
   * <p>Rust stores the heading as the rotation matrix {@code (cos, sin)} and {@code angle()}
   * returns {@code atan2(sin, cos)}. That round trip is not the identity in {@code f32}: {@code
   * 3π/2} becomes {@code -π/2}, and a heading of exactly {@code π} comes back as {@code -π}.
   * Reproducing it keeps the vision grid, including the boundary cases, identical to the reference.
   */
  private static float readRotation(float rotation) {
    return (float) Math.atan2((float) Math.sin(rotation), (float) Math.cos(rotation));
  }

  /**
   * Wraps an angle into {@code [-π, π]}, matching {@code nalgebra::wrap}.
   *
   * <p>{@code nalgebra::wrap} shifts by whole periods only while the value is strictly outside the
   * bounds, so {@code +π} is left untouched rather than becoming {@code -π}. A modulo formulation
   * would silently move that boundary and change which vision cell the edge food lands in.
   */
  private static float wrapAngle(float angle) {
    float pi = (float) Math.PI;
    float twoPi = (float) (2 * Math.PI);
    // A non-finite angle is a caller bug; returning it keeps the guard loops below finite.
    if (!Float.isFinite(angle) || (angle >= -pi && angle <= pi)) {
      return angle;
    }
    float wrapped = angle;
    while (wrapped < -pi) {
      wrapped += twoPi;
    }
    while (wrapped > pi) {
      wrapped -= twoPi;
    }
    return wrapped;
  }
}
