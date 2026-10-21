/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.jshorelark.simulation.physics.Vector2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the model-to-screen mapping.
 *
 * <p>These conversions were previously duplicated across every drawing method, so a single flipped
 * axis could hide in the renderer. They are pure arithmetic over a record, which is why the
 * extraction is testable without a JavaFX toolkit.
 */
@DisplayName("SceneTransform")
class SceneTransformTest {

  @Test
  @DisplayName("flips the vertical axis and keeps the origin at the top left")
  void flipsVerticalAxis() {
    var transform = new SceneTransform(500.0, 500.0);

    // Model y = 1 is the top of the world, which is pixel row 0.
    assertThat(transform.toScreen(new Vector2D(0.0f, 1.0f)).y()).isZero();
    assertThat(transform.toScreen(new Vector2D(0.0f, 0.0f)).y()).isEqualTo(500.0f);
    // x is not flipped.
    assertThat(transform.toScreen(new Vector2D(0.0f, 0.5f)).x()).isZero();
    assertThat(transform.toScreen(new Vector2D(1.0f, 0.5f)).x()).isEqualTo(500.0f);
  }

  @Test
  @DisplayName("scales both axes by the smaller dimension so the world stays square")
  void keepsTheWorldSquare() {
    var wide = new SceneTransform(800.0, 400.0);

    // The height is the smaller dimension, so it scales both axes.
    assertThat(wide.scale()).isEqualTo(400.0);
    assertThat(wide.toScreen(new Vector2D(1.0f, 0.0f)).x()).isEqualTo(400.0f);
    assertThat(wide.toScreenLength(0.5)).isEqualTo(200.0);
  }

  @Test
  @DisplayName("round-trips a point through both directions")
  void roundTrips() {
    var transform = new SceneTransform(640.0, 480.0);
    var model = new Vector2D(0.25f, 0.75f);

    var roundTripped = transform.toWorld(transform.toScreen(model));

    assertThat(roundTripped.x()).isCloseTo(model.x(), within(1e-6f));
    assertThat(roundTripped.y()).isCloseTo(model.y(), within(1e-6f));
  }

  @Test
  @DisplayName("reports an unusable transform for an unmeasured canvas")
  void detectsUnmeasuredCanvas() {
    assertThat(new SceneTransform(0.0, 0.0).isUsable()).isFalse();
    assertThat(new SceneTransform(Double.NaN, 100.0).isUsable()).isFalse();
    assertThat(new SceneTransform(100.0, 0.0).hasPositiveScale()).isFalse();
    assertThat(new SceneTransform(100.0, 100.0).isUsable()).isTrue();
  }

  @Test
  @DisplayName("returns the origin instead of dividing by zero")
  void avoidsDivisionByZero() {
    var transform = new SceneTransform(0.0, 0.0);

    // Without the guard this would be Infinity, which turns every later canvas call into a no-op
    // or a hang rather than an empty frame.
    assertThat(transform.toWorld(new Vector2D(10.0f, 10.0f))).isEqualTo(Vector2D.ZERO);
  }
}
