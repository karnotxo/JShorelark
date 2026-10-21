/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Typed access to the user-visible strings of the UI.
 *
 * <p>Every literal the user can read lives in {@code messages.properties}; this class is the only
 * place that knows the bundle's name and key format. Call sites keep a compile-time key (a constant
 * here) and a real method signature, so a typo is a compilation error instead of a {@code
 * ???key???} at runtime, and a refresh of the locale is a single {@link #useLocale} call.
 *
 * <p>Two formats are in play. Values read with {@link #text} are returned verbatim. Values read
 * with the formatting overloads go through {@link MessageFormat}, which is why the bundle must not
 * contain bare apostrophes.
 */
public final class Messages {

  /** Bundle name, resolved against {@code src/main/resources}. */
  private static final String BUNDLE = "io.jshorelark.simulation.ui.messages";

  // --- Keys -------------------------------------------------------------------

  /** Window title. */
  public static final String WINDOW_TITLE = "window.title";

  /** Splash screen status text. */
  public static final String SPLASH_LOADING = "splash.loading";

  /** File menu title. */
  public static final String MENU_FILE = "menu.file";

  /** View menu title. */
  public static final String MENU_VIEW = "menu.view";

  /** Help menu title. */
  public static final String MENU_HELP = "menu.help";

  /** Reset menu item. */
  public static final String MENU_RESET = "menu.reset";

  /** Exit menu item. */
  public static final String MENU_EXIT = "menu.exit";

  /** Dark theme menu item. */
  public static final String MENU_DARK_THEME = "menu.darkTheme";

  /** Sound effects menu item. */
  public static final String MENU_SOUND_EFFECTS = "menu.soundEffects";

  /** About menu item. */
  public static final String MENU_ABOUT = "menu.about";

  /** Pause tooltip. */
  public static final String CONTROLS_PAUSE = "controls.pause";

  /** Resume tooltip. */
  public static final String CONTROLS_RESUME = "controls.resume";

  /** Reset tooltip. */
  public static final String CONTROLS_RESET = "controls.reset";

  /** Add food tooltip. */
  public static final String CONTROLS_ADD_FOOD = "controls.addFood";

  /** Toggle sound tooltip. */
  public static final String CONTROLS_TOGGLE_SOUND = "controls.toggleSound";

  /** Speed slider tooltip. */
  public static final String CONTROLS_SPEED = "controls.speed";

  /** Volume slider tooltip. */
  public static final String CONTROLS_VOLUME = "controls.volume";

  /** Generation readout, with the generation number as its single argument. */
  public static final String CONTROLS_GENERATION = "controls.generation";

  /** Population readout, with the population size as its single argument. */
  public static final String CONTROLS_POPULATION = "controls.population";

  /** Best fitness readout, with the value as its single argument. */
  public static final String CONTROLS_BEST_FITNESS = "controls.bestFitness";

  /** Progress bar label. */
  public static final String CONTROLS_PROGRESS = "controls.progress";

  /** Inspector position line, with two rounded coordinates. */
  public static final String INSPECTOR_POSITION = "inspector.position";

  /** Inspector rotation line, with the angle already formatted. */
  public static final String INSPECTOR_ROTATION = "inspector.rotation";

  /** Inspector fitness line, with the value already formatted. */
  public static final String INSPECTOR_FITNESS = "inspector.fitness";

  /** Chart horizontal axis title. */
  public static final String CHART_GENERATION = "chart.generation";

  /** Chart vertical axis title. */
  public static final String CHART_FITNESS = "chart.fitness";

  /** Maximum series name. */
  public static final String CHART_MAX = "chart.max";

  /** Average series name. */
  public static final String CHART_AVG = "chart.avg";

  /** Median series name. */
  public static final String CHART_MEDIAN = "chart.median";

  /** Minimum series name. */
  public static final String CHART_MIN = "chart.min";

  /** About dialog title. */
  public static final String ABOUT_TITLE = "about.title";

  /** About dialog version line, with the version string as its argument. */
  public static final String ABOUT_VERSION = "about.version";

  /** About dialog description, with a {@code {0}} marker where the upstream link belongs. */
  public static final String ABOUT_DESCRIPTION = "about.description";

  /** Label of the link to the original project. */
  public static final String ABOUT_LINK_UPSTREAM = "about.link.upstream";

  /** About dialog attribution, with a {@code {0}} marker where the blog link belongs. */
  public static final String ABOUT_BASED_ON = "about.basedOn";

  /** Label of the link to the blog series. */
  public static final String ABOUT_LINK_BLOG = "about.link.blog";

  // --- State ------------------------------------------------------------------

  /** Bundle currently in use; never null, swapped as a whole by {@link #useLocale}. */
  private static ResourceBundle bundle = load(Locale.getDefault());

  private Messages() {}

  /**
   * Reloads the strings for a locale.
   *
   * @param locale the locale to load, typically chosen by the user
   * @throws NullPointerException if {@code locale} is {@code null}
   */
  public static void useLocale(Locale locale) {
    Objects.requireNonNull(locale, "locale");
    // Resolve first, assign second: a failed load leaves the previous bundle in place instead of
    // leaving every later call to fail on a half-initialised class.
    bundle = load(locale);
  }

  /**
   * Looks up a verbatim string.
   *
   * @param key one of the constants in this class
   * @return the value stored for the key
   * @throws MissingResourceException if the bundle has no such key
   */
  public static String text(String key) {
    return bundle.getString(key);
  }

  /**
   * Looks up a string and substitutes one argument.
   *
   * @param key one of the constants in this class
   * @param argument the value for {@code {0}}
   * @return the formatted string
   * @throws MissingResourceException if the bundle has no such key
   */
  public static String formatted(String key, Object argument) {
    return new MessageFormat(bundle.getString(key), bundle.getLocale())
        .format(new Object[] {argument});
  }

  /**
   * Looks up a string and substitutes two arguments.
   *
   * @param key one of the constants in this class
   * @param first the value for {@code {0}}
   * @param second the value for {@code {1}}
   * @return the formatted string
   * @throws MissingResourceException if the bundle has no such key
   */
  public static String formatted(String key, Object first, Object second) {
    return new MessageFormat(bundle.getString(key), bundle.getLocale())
        .format(new Object[] {first, second});
  }

  /**
   * Loads a bundle, failing loudly when it is missing.
   *
   * @param locale the locale to resolve
   * @return the loaded bundle
   */
  private static ResourceBundle load(Locale locale) {
    // getBundle falls back to the base bundle, so a missing translation is not an error; a missing
    // base file is, and it surfaces here rather than as a blank label.
    return ResourceBundle.getBundle(BUNDLE, locale);
  }
}
