/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.List;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

/**
 * Represents a bird's eye that can see food.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class BirdEye {
  /** Field of view range. */
  private final float fovRange;

  /** Field of view angle in radians. */
  private final float fovAngle;

  /** Number of cells in the eye. */
  private final int cells;

  /**
   * Creates a new bird eye.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   */
  public BirdEye(Config config) {
    this.fovRange = config.getEyeFovRange();
    this.fovAngle = config.getEyeFovAngle();
    this.cells = config.getEyeCells();
  }

  /**
   * Processes vision based on position, rotation, and visible food.
   *
   * @param position a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @param rotation a float
   * @param foods a {@link java.util.List} object
   * @return an array of {@link float} objects
   */
  public float[] processVision(Vector2D position, float rotation, List<Food> foods) {
    float[] vision = new float[cells];

    for (Food food : foods) {
      // Calculate vector to food
      Vector2D vec = food.getPosition().subtract(position);
      float dist = vec.length();

      // Skip if food is out of range
      if (dist > fovRange) {
        continue;
      }

      // Calculate angle to food relative to Y axis (up)
      float angle = (float) Math.atan2(vec.x(), vec.y());
      angle = angle - rotation;
      angle = wrap(angle, -(float) Math.PI, (float) Math.PI);

      // Skip if food is outside field of view
      if (angle < -fovAngle / 2 || angle > fovAngle / 2) {
        continue;
      }

      // Map angle to cell
      angle = angle + fovAngle / 2;
      float cellF = angle / fovAngle * cells;
      int cell = Math.min((int) cellF, cells - 1);

      // Add food strength to cell based on distance
      vision[cell] += (fovRange - dist) / fovRange;
    }

    return vision;
  }

  /** Wraps an angle to the range [min, max]. */
  private float wrap(float value, float min, float max) {
    float range = max - min;
    float offset = value - min;
    float wrapped = ((offset % range) + range) % range;
    return wrapped + min;
  }
}
