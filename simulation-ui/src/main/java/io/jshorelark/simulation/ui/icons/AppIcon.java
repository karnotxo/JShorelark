/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.icons;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages application icons at different sizes.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public final class AppIcon {
  /** Bird SVG path data - simple, bold design. */
  private static final String BIRD_PATH =
      // Main body (centered triangle)
      "M24,8 L40,24 L24,40 L8,24 Z "
          +
          // Wings (on sides)
          "M8,24 L2,16 L8,24 M40,24 L46,16 L40,24 "
          +
          // Tail (at bottom)
          "M24,40 L28,46 L24,44 L20,46 Z";

  /** Primary color for the bird. */
  private static final Color PRIMARY_COLOR = Color.web("#3498db");

  /** Secondary color for the bird outline. */
  private static final Color SECONDARY_COLOR = Color.web("#2980b9");

  /** Available icon sizes. */
  private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128, 256};

  /** The cached application icon. */
  private static Image cachedIcon = null;

  /**
   * Returns the application icon at the default size (48px).
   *
   * @return a {@link javafx.scene.image.Image} object
   */
  public static Image getImage() {
    if (cachedIcon == null) {
      cachedIcon = createIcon(48);
    }
    // Return a new snapshot of the cached icon to prevent modification
    var params = new SnapshotParameters();
    params.setFill(Color.TRANSPARENT);
    var copy = new WritableImage((int) cachedIcon.getWidth(), (int) cachedIcon.getHeight());
    var group = new Group(new javafx.scene.image.ImageView(cachedIcon));
    group.snapshot(params, copy);
    return copy;
  }

  /**
   * Creates a set of application icons at different sizes.
   *
   * @return a {@link javafx.collections.ObservableList} object
   */
  public static ObservableList<Image> createIcons() {
    var icons = javafx.collections.FXCollections.<Image>observableArrayList();
    for (int size : ICON_SIZES) {
      icons.add(createIcon(size));
    }
    return icons;
  }

  /** Creates an icon at the specified size. */
  private static Image createIcon(int size) {
    try {
      // Create bird shape
      var bird = new SVGPath();
      bird.setContent(BIRD_PATH);
      bird.setFill(PRIMARY_COLOR);
      bird.setStroke(SECONDARY_COLOR);
      bird.setStrokeWidth(1.5);

      // Create group and apply scaling
      var group = new Group(bird);

      // Calculate scale to fit desired size (with padding)
      double baseSize = 48.0; // SVG viewport size
      double padding = size * 0.1; // 10% padding
      double scale = (size - 2 * padding) / baseSize;

      // Apply scale and center in viewport
      group.setScaleX(scale);
      group.setScaleY(scale);
      group.setTranslateX(padding);
      group.setTranslateY(padding);

      // Create snapshot parameters
      var params = new SnapshotParameters();
      params.setFill(Color.TRANSPARENT);

      // Create snapshot at desired size
      var snapshot = new WritableImage(size, size);
      group.snapshot(params, snapshot);

      return snapshot;
    } catch (Exception e) {
      log.error("Failed to create icon at size " + size, e);
      // Return a 1x1 transparent image as fallback
      return new WritableImage(1, 1);
    }
  }

  /** Private constructor to prevent instantiation. */
  private AppIcon() {
    // Utility class
  }
}
