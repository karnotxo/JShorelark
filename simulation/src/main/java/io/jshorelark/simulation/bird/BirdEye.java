/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.List;
import java.util.Objects;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

/**
 * The bird's segmented field of view.
 *
 * <p>Mirrors Rust's {@code Eye}: the field of view is split into {@code eyeCells} equal bins; each
 * visible food item adds {@code (fovRange - distance) / fovRange} to exactly one bin, so nearer food
 * produces a stronger signal. The strength is a pure function of geometry and is what the brain
 * consumes as input.
 */
public final class BirdEye {

  private final float fovRange;
  private final float fovAngle;
  private final int cells;

  /**
   * Creates an eye configured from the simulation configuration.
   *
   * @param config the simulation configuration
   * @throws IllegalArgumentException if the field of view range, angle or cell count is not positive
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
   *   <li>the angle to the food is measured from +Y ({@code atan2(vec.x, vec.y)}), because that is
   *       what {@code Rotation2::rotation_between(&Vector2::y(), &vec)} produces;
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
      Vector2D offset = food.getPosition().subtract(position);
      float distance = offset.length();
      if (distance > fovRange) {
        continue;
      }

      // Angle from +Y, matching Rotation2::rotation_between(&Vector2::y(), &vec).
      float angle = (float) Math.atan2(offset.x(), offset.y()) - rotation;
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

  /** Wraps an angle into {@code [-π, π]}, matching {@code nalgebra::wrap}. */
  private static float wrapAngle(float angle) {
    float twoPi = (float) (2 * Math.PI);
    float wrapped = (angle + (float) Math.PI) % twoPi;
    if (wrapped < 0.0f) {
      wrapped += twoPi;
    }
    return wrapped - (float) Math.PI;
  }
}
