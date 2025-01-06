/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.icons;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Provides SVG icons for the UI. Icons are created as JavaFX SVGPath nodes.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class SvgIcons {

  private SvgIcons() {
    // Utility class
  }

  /**
   * Creates a play icon.
   *
   * @param size icon size
   * @param color icon color
   * @return play icon node
   */
  public static Node createPlayIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent("M8 5v14l11-7z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a pause icon.
   *
   * @param size icon size
   * @param color icon color
   * @return pause icon node
   */
  public static Node createPauseIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent("M6 19h4V5H6v14zm8-14v14h4V5h-4z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a reset icon.
   *
   * @param size icon size
   * @param color icon color
   * @return reset icon node
   */
  public static Node createResetIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent(
        "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55"
            + " 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69"
            + " 4.22 1.78L13 11h7V4l-2.35 2.35z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a food icon.
   *
   * @param size icon size
   * @param color icon color
   * @return food icon node
   */
  public static Node createFoodIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent(
        "M18.06 22.99h1.66c.84 0 1.53-.64 1.63-1.46L23 5.05h-5V1h-1.97v4.05h-4.97l.3 2.34c1.71.47"
            + " 3.31 1.32 4.27 2.26 1.44 1.42 2.43 2.89 2.43 5.29v8.05zM1 21.99V21h15.03v.99c0"
            + " .55-.45 1-1.01 1H2.01c-.56 0-1.01-.45-1.01-1zm15.03-7c0-8-15.03-8-15.03"
            + " 0h15.03zM1.02 17h15v2h-15z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a speed icon.
   *
   * @param size icon size
   * @param color icon color
   * @return speed icon node
   */
  public static Node createSpeedIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent(
        "M20.38 8.57l-1.23 1.85a8 8 0 0 1-.22 7.58H5.07A8 8 0 0 1 15.58 6.85l1.85-1.23A10 10 0 0 0"
            + " 3.35 19a2 2 0 0 0 1.72 1h13.85a2 2 0 0 0 1.74-1 10 10 0 0 0-.27-10.44zm-9.79 6.84a2"
            + " 2 0 0 0 2.83 0l5.66-8.49-8.49 5.66a2 2 0 0 0 0 2.83z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a chart icon.
   *
   * @param size icon size
   * @param color icon color
   * @return chart icon node
   */
  public static Node createChartIcon(double size, Color color) {
    var path = new SVGPath();
    path.setContent("M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a sound icon.
   *
   * @param size icon size
   * @param color icon color
   * @param volume volume level
   * @return sound icon node
   */
  public static Node createSoundIcon(double size, Color color, float volume) {
    if (volume <= 0.0f) {
      return createMutedSoundIcon(size, color);
    } else if (volume < 0.33f) {
      return createVolumeLowIcon(size, color);
    } else if (volume < 0.67f) {
      return createVolumeMediumIcon(size, color);
    } else {
      return createVolumeHighIcon(size, color);
    }
  }

  /**
   * Creates a volume low icon.
   *
   * @param size icon size
   * @param color icon color
   * @return volume low icon node
   */
  public static Node createVolumeLowIcon(double size, Color color) {
    SVGPath path = new SVGPath();
    path.setContent(
        "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a volume medium icon.
   *
   * @param size icon size
   * @param color icon color
   * @return volume medium icon node
   */
  public static Node createVolumeMediumIcon(double size, Color color) {
    SVGPath path = new SVGPath();
    path.setContent(
        "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM16"
            + " 3.55v2.19c2.78.47 5 2.94 5 5.86s-2.22 5.39-5 5.86v2.19c4.01-.47 7-3.94"
            + " 7-8.05s-2.99-7.58-7-8.05z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a volume high icon.
   *
   * @param size icon size
   * @param color icon color
   * @return volume high icon node
   */
  public static Node createVolumeHighIcon(double size, Color color) {
    SVGPath path = new SVGPath();
    path.setContent(
        "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM16"
            + " 3.55v2.19c2.78.47 5 2.94 5 5.86s-2.22 5.39-5 5.86v2.19c4.01-.47 7-3.94"
            + " 7-8.05s-2.99-7.58-7-8.05z");
    return configurePath(path, size, color);
  }

  /**
   * Creates a muted sound icon.
   *
   * @param size icon size
   * @param color icon color
   * @return muted sound icon node
   */
  public static Node createMutedSoundIcon(double size, Color color) {
    SVGPath path = new SVGPath();
    path.setContent(
        "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2"
            + " 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21"
            + " 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5"
            + " 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73"
            + " 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z");
    return configurePath(path, size, color);
  }

  /**
   * Configures common properties for SVG paths.
   *
   * @param path SVG path
   * @param size desired size
   * @param color desired color
   * @return configured path
   */
  private static Node configurePath(SVGPath path, double size, Color color) {
    path.setFill(color);
    path.setScaleX(size / 24.0);
    path.setScaleY(size / 24.0);
    return path;
  }
}
