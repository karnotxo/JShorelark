/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.render;

import io.jshorelark.simulation.physics.Vector2D;

/**
 * Conversion between model space and screen space.
 *
 * <p>The world is the unit square with Y pointing up; the canvas has its origin at the top left
 * with Y pointing down. Every mapping between the two used to be recomputed in each drawing method,
 * and each copy was an opportunity to flip an axis on one path only. This record is now the single
 * place that knows the mapping.
 *
 * <p>Both axes are scaled by the same factor, the smaller canvas dimension, so the world stays
 * square on a non-square window instead of stretching. The remaining space is unused rather than
 * letterboxed, matching what the original renderer draws.
 *
 * @param width the canvas width in pixels
 * @param height the canvas height in pixels
 */
public record SceneTransform(double width, double height) {

  /**
   * Returns the scale factor applied to both axes.
   *
   * @return the smaller of the two canvas dimensions
   */
  public double scale() {
    return Math.min(width, height);
  }

  /**
   * Returns whether the mapping is usable.
   *
   * <p>A canvas that has not been laid out yet reports zero, and a parent mid-layout can report
   * {@code NaN}. Drawing in either state produces garbage instead of an empty frame, so callers
   * check this before painting.
   *
   * @return {@code true} when both dimensions are positive and finite
   */
  public boolean isUsable() {
    return width > 0.0 && height > 0.0 && Double.isFinite(width) && Double.isFinite(height);
  }

  /**
   * Returns whether a length or coordinate is safe to draw with.
   *
   * <p>Guards the paint path against a zero scale: dividing by it yields {@code Infinity}, which
   * makes every subsequent canvas call a no-op or a hang instead of an empty frame.
   *
   * @return {@code true} when the scale factor is positive
   */
  public boolean hasPositiveScale() {
    return scale() > 0.0;
  }

  /**
   * Converts a model-space point to a pixel position.
   *
   * @param model the point in the unit square
   * @return the screen position, with Y flipped
   */
  public Vector2D toScreen(Vector2D model) {
    double scale = scale();
    return new Vector2D((float) (model.x() * scale), (float) ((1.0 - model.y()) * scale));
  }

  /**
   * Converts a pixel position to a model-space point.
   *
   * <p>Inverse of {@link #toScreen}. Returns the origin for an unusable transform rather than
   * dividing by zero and producing {@code Infinity}.
   *
   * @param screen the pixel position
   * @return the point in model space
   */
  public Vector2D toWorld(Vector2D screen) {
    if (!isUsable()) {
      return Vector2D.ZERO;
    }
    double scale = scale();
    return new Vector2D((float) (screen.x() / scale), (float) (1.0 - screen.y() / scale));
  }

  /**
   * Converts a model-space length to pixels.
   *
   * @param worldLength a distance in model space
   * @return the same distance in pixels
   */
  public double toScreenLength(double worldLength) {
    return worldLength * scale();
  }
}
