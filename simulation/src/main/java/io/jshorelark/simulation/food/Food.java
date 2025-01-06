/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.food;

import java.util.Random;

import io.jshorelark.simulation.physics.Vector2D;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents food in the simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class Food {
  /** The position of the food. */
  @Getter @Setter private Vector2D position;

  /** Creates a new food at the given position. */
  private Food(Vector2D position) {
    this.position = position;
  }

  /**
   * Creates a new food at a random position.
   *
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.simulation.food.Food} object
   */
  public static Food random(Random random) {
    return new Food(Vector2D.random(random));
  }

  /**
   * Creates a new food at the given position.
   *
   * @param position a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @return a {@link io.jshorelark.simulation.food.Food} object
   */
  public static Food at(Vector2D position) {
    return new Food(position);
  }
}
