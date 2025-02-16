/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.food;

import java.util.Random;

import io.jshorelark.simulation.physics.Vector2D;

import lombok.Value;
import lombok.With;

/**
 * Represents food in the simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
@With
public class Food {
  /** The food's position. */
  Vector2D position;

  /** Whether this food has been consumed. */
  boolean consumed;

  /**
   * Creates food at the given position.
   *
   * @param position the position
   * @return the food
   */
  public static Food at(Vector2D position) {
    return new Food(position, false);
  }

  /**
   * Creates food at a random position.
   *
   * @param random the random number generator
   * @return the food
   */
  public static Food random(Random random) {
    return at(Vector2D.random(random));
  }
}
