/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Config")
class ConfigTest {

  @Test
  @DisplayName("matches the Rust reference defaults")
  void matchesRustDefaults() {
    var config = Config.defaults();

    assertThat(config.brainNeurons()).isEqualTo(9);
    assertThat(config.eyeFovRange()).isEqualTo(0.25f);
    assertThat(config.eyeFovAngle()).isEqualTo((float) (Math.PI + Math.PI / 4));
    assertThat(config.eyeCells()).isEqualTo(9);
    assertThat(config.foodSize()).isEqualTo(0.01f);
    assertThat(config.birdSize()).isEqualTo(0.01f);
    assertThat(config.gaReverse()).isZero();
    assertThat(config.gaMutChance()).isEqualTo(0.01f);
    assertThat(config.gaMutCoeff()).isEqualTo(0.3f);
    assertThat(config.simSpeedMin()).isEqualTo(0.001f);
    assertThat(config.simSpeedMax()).isEqualTo(0.005f);
    assertThat(config.simSpeedAccel()).isEqualTo(0.2f);
    assertThat(config.simRotationAccel()).isEqualTo((float) (Math.PI / 2));
    assertThat(config.simGenerationLength()).isEqualTo(2500);
    assertThat(config.worldAnimals()).isEqualTo(40);
    assertThat(config.worldFoods()).isEqualTo(60);
  }

  @Test
  @DisplayName("derives the brain topology from the eye and hidden-neuron counts")
  void derivesTopology() {
    var config = Config.defaults().toBuilder().eyeCells(5).brainNeurons(7).build();

    assertThat(config.brainTopology()).containsExactly(5, 7, 2);
  }

  @Test
  @DisplayName("returns a fresh topology array so callers cannot share state")
  void topologyIsDefensiveCopy() {
    var config = Config.defaults();
    var first = config.brainTopology();
    first[0] = 999;

    assertThat(config.brainTopology()).containsExactly(9, 9, 2);
  }

  @Test
  @DisplayName("with-methods derive a new configuration without mutating the original")
  void withDerivesNewInstance() {
    var base = Config.defaults();
    var derived = base.withSimGenerationLength(10);

    assertThat(derived.simGenerationLength()).isEqualTo(10);
    assertThat(base.simGenerationLength()).isEqualTo(2500);
  }

  @Test
  @DisplayName("rejects non-positive counts and ranges")
  void rejectsNonPositiveValues() {
    assertThatThrownBy(() -> Config.defaults().toBuilder().brainNeurons(0).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("brainNeurons");
    assertThatThrownBy(() -> Config.defaults().toBuilder().eyeFovRange(0.0f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("eyeFovRange");
    assertThatThrownBy(() -> Config.defaults().toBuilder().eyeFovAngle(-1.0f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("eyeFovAngle");
    assertThatThrownBy(() -> Config.defaults().toBuilder().eyeCells(0).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("eyeCells");
    assertThatThrownBy(() -> Config.defaults().toBuilder().foodSize(-1.0f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("foodSize");
    assertThatThrownBy(() -> Config.defaults().toBuilder().simGenerationLength(0).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("simGenerationLength");
  }

  @Test
  @DisplayName("rejects out-of-range probabilities and speed bounds")
  void rejectsInvalidRanges() {
    assertThatThrownBy(() -> Config.defaults().toBuilder().gaMutChance(1.5f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("gaMutChance");
    assertThatThrownBy(() -> Config.defaults().toBuilder().gaMutChance(-0.1f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("gaMutChance");
    assertThatThrownBy(
            () -> Config.defaults().toBuilder().simSpeedMin(0.5f).simSpeedMax(0.1f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("simSpeedMin");
    assertThatThrownBy(() -> Config.defaults().toBuilder().simRotationAccel(-1.0f).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("simRotationAccel");
    assertThatThrownBy(() -> Config.defaults().toBuilder().worldAnimals(-1).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("worldAnimals");
    assertThatThrownBy(() -> Config.defaults().toBuilder().worldFoods(-1).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("worldFoods");
  }
}
