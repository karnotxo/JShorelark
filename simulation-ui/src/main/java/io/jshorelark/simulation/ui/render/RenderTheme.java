/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.render;

import javafx.scene.paint.Color;

/**
 * The colours of one UI theme.
 *
 * <p>Only the palette lives here; every drawing routine takes a {@code RenderTheme} parameter and
 * never reads a global flag. That keeps the paint path a pure function of (state, theme, transform)
 * and makes "which colour is this in light mode?" answerable by reading one table.
 */
public enum RenderTheme {
  /** Light text on dark surfaces. */
  DARK(
      Color.web("#3498db"),
      Color.web("#2980b9"),
      Color.web("#2ecc71"),
      Color.web("#e74c3c"),
      Color.rgb(255, 255, 255, 0.2),
      Color.rgb(255, 255, 255, 0.1),
      Color.rgb(255, 255, 255, 0.1),
      Color.rgb(0, 0, 0, 0.8),
      Color.WHITE),

  /** Dark text on light surfaces. */
  LIGHT(
      Color.web("#2980b9"),
      Color.web("#1a5276"),
      Color.web("#27ae60"),
      Color.web("#c0392b"),
      Color.rgb(0, 0, 0, 0.2),
      Color.rgb(0, 0, 0, 0.1),
      Color.rgb(0, 0, 0, 0.1),
      Color.rgb(255, 255, 255, 0.9),
      Color.BLACK);

  private final Color bird;
  private final Color birdOutline;
  private final Color eye;
  private final Color food;
  private final Color trail;
  private final Color sensorEmpty;
  private final Color grid;
  private final Color infoBoxBackground;
  private final Color infoText;

  RenderTheme(
      Color bird,
      Color birdOutline,
      Color eye,
      Color food,
      Color trail,
      Color sensorEmpty,
      Color grid,
      Color infoBoxBackground,
      Color infoText) {
    this.bird = bird;
    this.birdOutline = birdOutline;
    this.eye = eye;
    this.food = food;
    this.trail = trail;
    this.sensorEmpty = sensorEmpty;
    this.grid = grid;
    this.infoBoxBackground = infoBoxBackground;
    this.infoText = infoText;
  }

  /**
   * Returns the bird body colour.
   *
   * @return the body fill
   */
  public Color bird() {
    return bird;
  }

  /**
   * Returns the bird outline colour.
   *
   * @return the outline stroke
   */
  public Color birdOutline() {
    return birdOutline;
  }

  /**
   * Returns the eye colour.
   *
   * @return the eye fill
   */
  public Color eye() {
    return eye;
  }

  /**
   * Returns the food colour.
   *
   * @return the food fill
   */
  public Color food() {
    return food;
  }

  /**
   * Returns the movement trail colour.
   *
   * @return the trail stroke
   */
  public Color trail() {
    return trail;
  }

  /**
   * Returns the colour of a sensor cell that sees nothing.
   *
   * @return the empty sensor fill
   */
  public Color sensorEmpty() {
    return sensorEmpty;
  }

  /**
   * Returns the background grid colour.
   *
   * @return the grid stroke
   */
  public Color grid() {
    return grid;
  }

  /**
   * Returns the inspector background colour.
   *
   * @return the info box fill
   */
  public Color infoBoxBackground() {
    return infoBoxBackground;
  }

  /**
   * Returns the inspector text colour.
   *
   * @return the text fill
   */
  public Color infoText() {
    return infoText;
  }
}
