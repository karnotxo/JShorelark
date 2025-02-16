/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.icons;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages application icons at different sizes.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public final class AppIcon {
  /** Available icon sizes. */
  private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 128};

  /** The cached application icon. */
  private static Image cachedIcon = null;

  /**
   * Returns the application icon at the default size (48px).
   *
   * @return a {@link javafx.scene.image.Image} object
   */
  public static Image getImage() {
    if (cachedIcon == null) {
      cachedIcon = loadIcon(48);
    }
    // Return a new Image instance using the same URL to prevent modification of the cached instance
    return cachedIcon != null ? new Image(cachedIcon.getUrl()) : null;
  }

  /**
   * Creates a set of application icons at different sizes.
   *
   * @return a {@link javafx.collections.ObservableList} object
   */
  public static ObservableList<Image> createIcons() {
    var icons = FXCollections.<Image>observableArrayList();
    for (int size : ICON_SIZES) {
      var icon = loadIcon(size);
      if (icon != null) {
        icons.add(icon);
      }
    }
    return icons;
  }

  /** Loads an icon at the specified size. */
  private static Image loadIcon(int size) {
    try {
      String resourcePath = String.format("/icons/app_icon%d.png", size);
      var url = AppIcon.class.getResource(resourcePath);
      if (url == null) {
        log.error("Icon resource not found: {}", resourcePath);
        return null;
      }
      return new Image(url.toExternalForm());
    } catch (Exception e) {
      log.error("Failed to load icon of size " + size, e);
      return null;
    }
  }

  /** Private constructor to prevent instantiation. */
  private AppIcon() {
    // Utility class
  }
}
