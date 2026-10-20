/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
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

  /** Size of the icon returned by {@link #getImage()}. */
  private static final int DEFAULT_SIZE = 48;

  /**
   * Holds the lazily loaded default icon.
   *
   * <p>The holder idiom gives thread-safe lazy initialisation with no lock and no mutable static
   * field: the class loader guarantees the initialiser runs exactly once.
   */
  private static final class DefaultIconHolder {
    private static final Image ICON = loadIcon(DEFAULT_SIZE);
  }

  /**
   * Returns the application icon at the default size (48px).
   *
   * @return a {@link javafx.scene.image.Image} object, or {@code null} when the resource is missing
   */
  public static Image getImage() {
    Image icon = DefaultIconHolder.ICON;
    // A fresh instance per caller, so nobody can mutate the shared cached image.
    return icon != null ? new Image(icon.getUrl()) : null;
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
      String resourcePath = "/icons/app_icon%d.png".formatted(size);
      var url = AppIcon.class.getResource(resourcePath);
      if (url == null) {
        log.error("Icon resource not found: {}", resourcePath);
        return null;
      }
      return new Image(url.toExternalForm());
    } catch (IllegalArgumentException e) {
      // The URL exists but the image cannot be decoded; the UI keeps its default icon.
      log.error("Failed to load icon of size {}", size, e);
      return null;
    }
  }

  /** Private constructor to prevent instantiation. */
  private AppIcon() {
    // Utility class
  }
}
