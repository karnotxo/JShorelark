/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the message bundle.
 *
 * <p>Externalising the literals only pays off if the keys stay honest: a key with no entry throws
 * when the label is first shown, which is exactly the failure a test suite should catch instead of
 * a user. Reflection reads the {@code public static final String} key constants, so adding a key
 * without a value (or a value without a constant) fails here.
 */
@DisplayName("Messages")
class MessagesTest {

  @Test
  @DisplayName("every key constant resolves to a value")
  void everyKeyResolves() throws IllegalAccessException {
    List<String> missing = new ArrayList<>();
    for (String key : declaredKeys()) {
      try {
        assertThat(Messages.text(key)).as("value of %s", key).isNotBlank();
      } catch (MissingResourceException e) {
        missing.add(key);
      }
    }

    assertThat(missing).as("keys declared but absent from messages.properties").isEmpty();
  }

  @Test
  @DisplayName("every placeholder in the bundle matches the arguments its key declares")
  void placeholdersMatchArguments() {
    for (String key : declaredKeys()) {
      String value = Messages.text(key);
      if (value.contains("{0}")) {
        // A link marker or a single argument: both are satisfiable with one value.
        assertThat(Messages.formatted(key, "x")).as("formatting %s", key).doesNotContain("{0}");
      }
      if (value.contains("{1}")) {
        assertThat(Messages.formatted(key, "x", "y"))
            .as("formatting %s", key)
            .doesNotContain("{1}");
      }
    }
  }

  @Test
  @DisplayName("reports a missing key instead of rendering a placeholder")
  void missingKeyThrows() {
    assertThatThrownBy(() -> Messages.text("no.such.key"))
        .isInstanceOf(MissingResourceException.class);
  }

  @Test
  @DisplayName("falls back to the base bundle for a locale that has no translation")
  void localeFallsBackToBaseBundle() {
    Locale original = Locale.getDefault();
    try {
      // No translation ships yet, so a foreign locale must still resolve to the base file rather
      // than leave every label blank.
      Messages.useLocale(Locale.forLanguageTag("es-ES"));

      assertThat(Messages.text(Messages.WINDOW_TITLE)).isEqualTo("JShorelark");
    } finally {
      Messages.useLocale(original);
    }
  }

  @Test
  @DisplayName("bundle and key constants agree on what exists")
  void bundleHasNoOrphanKeys() {
    ResourceBundle bundle =
        ResourceBundle.getBundle("io.jshorelark.simulation.ui.messages", Locale.ROOT);
    List<String> declared = declaredKeys();

    assertThat(bundle.keySet())
        .as("entries in messages.properties with no constant in Messages")
        .allSatisfy(key -> assertThat(declared).contains(key));
  }

  /** Returns the values of the {@code public static final String} key constants. */
  private static List<String> declaredKeys() {
    return java.util.Arrays.stream(Messages.class.getDeclaredFields())
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .filter(field -> field.getType() == String.class)
        // Key constants are upper snake case; the bundle name is also a String constant, so it is
        // the one name to skip rather than relying on the pattern to tell them apart.
        .filter(field -> field.getName().matches("[A-Z][A-Z0-9_]*"))
        .filter(field -> !field.getName().equals("BUNDLE"))
        .map(MessagesTest::readKey)
        .toList();
  }

  private static String readKey(Field field) {
    try {
      field.setAccessible(true);
      return (String) field.get(null);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Cannot read " + field.getName(), e);
    }
  }
}
