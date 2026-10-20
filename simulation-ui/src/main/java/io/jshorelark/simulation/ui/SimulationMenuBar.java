/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import io.jshorelark.simulation.ui.dialogs.AboutDialog;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

/**
 * The application's menu bar.
 *
 * <p>Layout only: every item delegates to a callback supplied by {@link SimulationApplication}, so
 * the menus never reach back into the application's state. Splitting it out keeps the application
 * class focused on orchestrating the simulation and the window.
 */
final class SimulationMenuBar extends MenuBar {

  /**
   * The actions the menu items trigger.
   *
   * @param reset restores the default configuration
   * @param toggleTheme switches between the dark and light stylesheets
   * @param setSoundEnabled enables or disables sound effects
   * @param about opens the About dialog
   */
  record Actions(
      Runnable reset, Runnable toggleTheme, Consumer<Boolean> setSoundEnabled, Runnable about) {

    /** Validates the callbacks. */
    Actions {
      Objects.requireNonNull(reset, "reset");
      Objects.requireNonNull(toggleTheme, "toggleTheme");
      Objects.requireNonNull(setSoundEnabled, "setSoundEnabled");
      Objects.requireNonNull(about, "about");
    }
  }

  /**
   * Builds the menu bar.
   *
   * @param owner the window that owns the About dialog
   * @param darkTheme whether the dark theme is currently active
   * @param soundEnabled whether sound effects are currently enabled
   * @param actions what each item does
   */
  SimulationMenuBar(Stage owner, boolean darkTheme, boolean soundEnabled, Actions actions) {
    var resetItem = new MenuItem("Reset");
    resetItem.setOnAction(e -> actions.reset().run());
    var exitItem = new MenuItem("Exit");
    exitItem.setOnAction(e -> Platform.exit());

    var themeItem = new CheckMenuItem("Dark Theme");
    themeItem.setSelected(darkTheme);
    themeItem.setOnAction(e -> actions.toggleTheme().run());

    var soundItem = new CheckMenuItem("Sound Effects");
    soundItem.setSelected(soundEnabled);
    soundItem.setOnAction(e -> actions.setSoundEnabled().accept(soundItem.isSelected()));

    var aboutItem = new MenuItem("About");
    aboutItem.setOnAction(e -> AboutDialog.show(owner));

    getMenus()
        .addAll(
            new Menu("File", null, resetItem, new SeparatorMenuItem(), exitItem),
            new Menu("View", null, themeItem, soundItem),
            new Menu("Help", null, aboutItem));
  }
}
