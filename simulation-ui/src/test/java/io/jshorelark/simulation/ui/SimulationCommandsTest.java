/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the wiring between the controls panel and the application.
 *
 * <p>The panel used to depend on the whole {@link SimulationApplication} through a private wrapper
 * that forwarded fourteen methods. That wrapper could not be substituted, so the panel could not be
 * exercised without starting the JavaFX toolkit, and adding a command to the application meant
 * editing the panel whether or not it used it.
 *
 * <p>These checks are structural on purpose: constructing the panel would need a toolkit, and what
 * matters here is that the decoupling stays in place.
 */
@DisplayName("SimulationCommands")
class SimulationCommandsTest {

  @Test
  @DisplayName("is implemented by the application, so callers keep passing it")
  void applicationImplementsTheInterface() {
    // Reading the class literal loads the type without initialising it, so no toolkit is started.
    assertThat(SimulationCommands.class.isAssignableFrom(SimulationApplication.class)).isTrue();
  }

  @Test
  @DisplayName("is the only type the controls panel accepts")
  void controlsDependOnTheInterfaceAlone() {
    Constructor<?>[] constructors = SimulationControls.class.getDeclaredConstructors();

    assertThat(constructors).hasSize(1);
    assertThat(Modifier.isPublic(constructors[0].getModifiers())).isTrue();
    assertThat(constructors[0].getParameterTypes()).containsExactly(SimulationCommands.class);
  }

  @Test
  @DisplayName("exposes no method that leaks the application type")
  void interfaceDoesNotLeakTheApplication() {
    assertThat(SimulationCommands.class.getMethods())
        .allSatisfy(
            method -> {
              assertThat(method.getReturnType()).isNotEqualTo(SimulationApplication.class);
              assertThat(method.getParameterTypes()).doesNotContain(SimulationApplication.class);
            });
  }
}
