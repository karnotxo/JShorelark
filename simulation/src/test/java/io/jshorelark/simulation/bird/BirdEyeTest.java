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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
      // Rotation is CCW from +Y, so -pi/2 faces +X and brings that food into the middle cell.
      var towards = narrowEye.processVision(new Vector2D(0.5f, 0.5f), (float) (-Math.PI / 2), food);

      assertThat(maxOf(ahead)).isZero();
      assertThat(maxOf(towards)).isPositive();
    }

    @Test
    @DisplayName("distinguishes left from right, as the reference eye does")
    void distinguishesLeftFromRight() {
      // Facing +Y with X to the right, -X is the bird's left. A CCW-positive angle therefore puts
      // left-hand food in the upper half of the fan and right-hand food in the lower half; an
      // implementation using atan2(+vec.x, vec.y) would swap the two.
      var leftOfBird =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.3f, 0.6f))));
      var rightOfBird =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.7f, 0.6f))));

      assertThat(indexOfStrongest(leftOfBird)).isGreaterThan(config.eyeCells() / 2);
      assertThat(indexOfStrongest(rightOfBird)).isLessThan(config.eyeCells() / 2);
    }

    @Test
    @DisplayName("maps food near the fan edge to the last cell instead of overflowing")
    void mapsFanEdgeToLastCell() {
      // Angle +1.90 rad from +Y, just inside the +1.96 rad half-fan: the shifted angle lands in
      // the last bin, which is the case that needs the `cells - 1` cap.
      var vision =
          eye()
              .processVision(
                  new Vector2D(0.5f, 0.5f), 0.0f, List.of(Food.at(new Vector2D(0.311f, 0.435f))));

      assertThat(vision).hasSize(config.eyeCells());
      assertThat(vision[config.eyeCells() - 1]).isPositive();
    }
  }

  /**
   * Ports the reference eye's vision-grid tests, which pin the whole geometry at once.
   *
   * <p>Rust's {@code libs/simulation/src/eye.rs} renders each cell as {@code #} (>= 0.7), {@code +}
   * (>= 0.3), {@code .} (> 0) or a space, over a 13-cell eye. Reproducing those strings is the
   * strongest available check that the angle convention, the wrap and the binning all match.
   */
  @Nested
  @DisplayName("reference vision grids")
  class ReferenceGrids {

    private static final int REFERENCE_CELLS = 13;

    private static String render(float[] vision) {
      var rendered = new StringBuilder(vision.length);
      for (float cell : vision) {
        if (cell >= 0.7f) {
          rendered.append('#');
        } else if (cell >= 0.3f) {
          rendered.append('+');
        } else if (cell > 0.0f) {
          rendered.append('.');
        } else {
          rendered.append(' ');
        }
      }
      return rendered.toString();
    }

    private static String visionAt(
        float fovRange, float fovAngle, float x, float y, float rotation, List<Food> foods) {
      var reference =
          Config.defaults().toBuilder()
              .eyeFovRange(fovRange)
              .eyeFovAngle(fovAngle)
              .eyeCells(REFERENCE_CELLS)
              .build();
      return render(new BirdEye(reference).processVision(new Vector2D(x, y), rotation, foods));
    }

    @ParameterizedTest(name = "range {0} renders \"{1}\"")
    @CsvSource({
      "1.0, '      +      '",
      "0.9, '      +      '",
      "0.8, '      +      '",
      "0.7, '      .      '",
      "0.6, '      .      '",
      "0.5, '             '",
      "0.4, '             '",
      "0.3, '             '",
      "0.2, '             '",
      "0.1, '             '"
    })
    @DisplayName("different fov ranges")
    void differentFovRanges(float fovRange, String expected) {
      var actual =
          visionAt(
              fovRange,
              (float) (Math.PI / 2),
              0.5f,
              0.5f,
              0.0f,
              List.of(Food.at(new Vector2D(0.5f, 1.0f))));

      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "angle {0} renders \"{1}\"")
    @CsvSource({
      "0.25, ' +         + '",
      "0.50, '.  +     +  .'",
      "0.75, '  . +   + .  '",
      "1.00, '   . + + .   '",
      "1.25, '   . + + .   '",
      "1.50, '.   .+ +.   .'",
      "1.75, '.   .+ +.   .'",
      "2.00, '+.  .+ +.  .+'"
    })
    @DisplayName("different fov angles")
    void differentFovAngles(float piMultiple, String expected) {
      var foods =
          List.of(
              Food.at(new Vector2D(0.0f, 0.0f)),
              Food.at(new Vector2D(0.0f, 0.33f)),
              Food.at(new Vector2D(0.0f, 0.66f)),
              Food.at(new Vector2D(0.0f, 1.0f)),
              Food.at(new Vector2D(1.0f, 0.0f)),
              Food.at(new Vector2D(1.0f, 0.33f)),
              Food.at(new Vector2D(1.0f, 0.66f)),
              Food.at(new Vector2D(1.0f, 1.0f)));

      var actual =
          visionAt(
              1.0f,
              (float) (piMultiple * Math.PI),
              0.5f,
              0.5f,
              (float) (3.0 * Math.PI / 2.0),
              foods);

      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "position ({0}, {1}) renders \"{2}\"")
    @CsvSource({
      "0.9, 0.5, '#           #'",
      "0.8, 0.5, '  #       #  '",
      "0.7, 0.5, '   +     +   '",
      "0.6, 0.5, '    +   +    '",
      "0.5, 0.5, '    +   +    '",
      "0.4, 0.5, '     + +     '",
      "0.3, 0.5, '     . .     '",
      "0.2, 0.5, '     . .     '",
      "0.1, 0.5, '     . .     '",
      "0.0, 0.5, '             '",
      "0.5, 0.0, '            +'",
      "0.5, 0.1, '          + .'",
      "0.5, 0.2, '         +  +'",
      "0.5, 0.3, '        + +  '",
      "0.5, 0.4, '      +  +   '",
      "0.5, 0.6, '   +  +      '",
      "0.5, 0.7, '  + +        '",
      "0.5, 0.8, '+  +         '",
      "0.5, 0.9, '. +          '",
      "0.5, 1.0, '+            '"
    })
    @DisplayName("different positions")
    void differentPositions(float x, float y, String expected) {
      var foods = List.of(Food.at(new Vector2D(1.0f, 0.4f)), Food.at(new Vector2D(1.0f, 0.6f)));

      var actual =
          visionAt(1.0f, (float) (Math.PI / 2), x, y, (float) (3.0 * Math.PI / 2.0), foods);

      assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "rotation {0}*pi renders \"{1}\"")
    @CsvSource({
      "0.00, '         +   '",
      "0.25, '        +    '",
      "0.50, '      +      '",
      "0.75, '    +        '",
      "1.00, '   +         '",
      "1.25, ' +           '",
      "1.50, '            +'",
      "1.75, '           + '",
      "2.00, '         +   '",
      "2.25, '        +    '",
      "2.50, '      +      '"
    })
    @DisplayName("different rotations")
    void differentRotations(float piMultiple, String expected) {
      var actual =
          visionAt(
              1.0f,
              (float) (2.0 * Math.PI),
              0.5f,
              0.5f,
              (float) (piMultiple * Math.PI),
              List.of(Food.at(new Vector2D(0.0f, 0.5f))));

      assertThat(actual).isEqualTo(expected);
    }
  }

  /** Returns the index of the strongest cell, or {@code -1} when the vector is all zeros. */
  private static int indexOfStrongest(float[] vision) {
    int best = -1;
    float bestValue = 0.0f;
    for (int i = 0; i < vision.length; i++) {
      if (vision[i] > bestValue) {
        bestValue = vision[i];
        best = i;
      }
    }
    return best;
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
