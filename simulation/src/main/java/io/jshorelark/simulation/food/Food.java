/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.food;

import io.jshorelark.simulation.physics.Vector2D;
import java.util.Objects;
import java.util.Random;

/**
 * A food item: a position and nothing else.
 *
 * <p>Mirrors Rust's {@code Food}, which is a single {@code Point2}. When a bird eats, the item is
 * repositioned rather than removed, so a food item carries no "consumed" state.
 *
 * @param position the position in world coordinates
 */
public record Food(Vector2D position) {

  /** Validates the position. */
  public Food {
    Objects.requireNonNull(position, "position");
  }

  /**
   * Creates food at the given position.
   *
   * @param position the position
   * @return the food
   */
  public static Food at(Vector2D position) {
    return new Food(position);
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
