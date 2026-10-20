/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BirdEye")
class BirdEyeTest {

  private Config config;

  @BeforeEach
  void setUp() {
    // Nine cells over pi + pi/4, matching the reference configuration.
    config = Config.defaults();
  }

  private BirdEye eye() {
    return new BirdEye(config);
  }

  private static float maxOf(float[] values) {
    float max = 0.0f;
    for (float value : values) {
      max = Math.max(max, value);
    }
    return max;
  }

  private static int countPositive(float[] values) {
    int count = 0;
    for (float value : values) {
      if (value > 0.0f) {
        count++;
      }
    }
    return count;
  }

  @Nested
  @DisplayName("vision")
  class Vision {

    @Test
    @DisplayName("returns one value per cell and no signal without food")
    void emptyWorldSeesNothing() {
      var vision = eye().processVision(new Vector2D(0.5f, 0.5f), 0.0f, List.of());

      assertThat(vision).hasSize(config.eyeCells()).containsOnly(0.0f);
    }

    @Test
    @DisplayName("sees food straight ahead in the central cell")
    void seesFoodAhead() {
      // Heading 0 is +Y, so food directly above falls in the middle of the fan.
      var vision =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.5f, 0.6f))));

      int centre = config.eyeCells() / 2;
      // Rust: (fov_range - distance) / fov_range with distance 0.1 and range 0.25.
      assertThat(vision[centre]).isCloseTo((0.25f - 0.1f) / 0.25f, within(1e-5f));
      assertThat(countPositive(vision)).isEqualTo(1);
    }

    @Test
    @DisplayName("ignores food beyond the field of view range")
    void ignoresFoodOutOfRange() {
      var vision =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.5f, 0.9f))));

      assertThat(vision).containsOnly(0.0f);
    }

    @Test
    @DisplayName("ignores food behind the bird")
    void ignoresFoodBehind() {
      // Directly below is pi away from a heading of 0, outside even the wide default fan.
      var vision =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.5f, 0.4f))));

      assertThat(vision).containsOnly(0.0f);
    }

    @Test
    @DisplayName("accumulates several items in the same cell")
    void accumulatesSameCell() {
      var single =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.5f, 0.6f))));
      var doubled =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f),
                  0.0f,
                  List.of(Food.at(new Vector2D(0.5f, 0.6f)), Food.at(new Vector2D(0.5f, 0.6f))));

      assertThat(maxOf(doubled)).isCloseTo(2.0f * maxOf(single), within(1e-5f));
    }

    @Test
    @DisplayName("follows the heading when the bird turns")
    void followsHeading() {
      // A narrow fan keeps the food outside the default 225-degree field of view.
      var narrow = config.toBuilder().eyeFovAngle((float) (Math.PI / 3)).build();
      var narrowEye = new BirdEye(narrow);
      var food = List.of(Food.at(new Vector2D(0.6f, 0.5f)));

      var ahead = narrowEye.processVision(new Vector2D(0.5f, 0.5f), 0.0f, food);
      // A heading of +pi/2 turns the bird towards +X, putting the same food in the centre.
      var towards = narrowEye.processVision(new Vector2D(0.5f, 0.5f), (float) (Math.PI / 2), food);

      assertThat(maxOf(ahead)).isZero();
      assertThat(maxOf(towards)).isPositive();
    }

    @Test
    @DisplayName("maps food near the fan edge to the last cell instead of overflowing")
    void mapsFanEdgeToLastCell() {
      // Angle ~1.90 rad from +Y, just inside the +1.96 rad half-fan: the shifted angle lands in
      // the last bin, which is the case that needs the `cells - 1` cap.
      var vision =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.689f, 0.435f))));

      assertThat(vision).hasSize(config.eyeCells());
      assertThat(vision[config.eyeCells() - 1]).isPositive();
    }
  }

  @Nested
  @DisplayName("accessors")
  class Accessors {

    @Test
    @DisplayName("exposes the configured geometry")
    void exposesGeometry() {
      var eye = eye();

      assertThat(eye.getCells()).isEqualTo(config.eyeCells());
      assertThat(eye.getFovAngle()).isEqualTo(config.eyeFovAngle());
      assertThat(eye.getFovRange()).isEqualTo(config.eyeFovRange());
    }
  }
}
