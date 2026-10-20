/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GridGenerator")
class GridGeneratorTest {

  @Test
  @DisplayName("covers the reference grid: 4 x 4 x 4 x 5 x 4 x 5 points x 15 iterations")
  void coversReferenceGrid() {
    var grid = GridGenerator.generate();

    int points = 4 * 4 * 4 * 5 * 4 * 5;
    assertThat(grid).hasSize(points * GridGenerator.SIM_ITERATIONS);
  }

  @Test
  @DisplayName("repeats each grid point once per iteration, numbered from zero")
  void numbersIterations() {
    var grid = GridGenerator.generate();
    var firstPoint =
        grid.stream().filter(entry -> entry.iteration() == 0).findFirst().orElseThrow();

    List<ConfigurationIteration> repetitions =
        grid.stream().filter(entry -> entry.config().equals(firstPoint.config())).toList();

    assertThat(repetitions)
        .hasSize(GridGenerator.SIM_ITERATIONS)
        .extracting(ConfigurationIteration::iteration)
        .containsExactlyElementsOf(
            IntStream.range(0, GridGenerator.SIM_ITERATIONS).boxed().toList());
  }

  @Test
  @DisplayName("keeps the reference value 3.14 for the field-of-view angle")
  void keepsRoughPi() {
    // The upstream sweep samples 3.14, not Math.PI; benchmark comparability depends on it.
    assertThat(GridGenerator.generate())
        .extracting(entry -> entry.config().eyeFovAngle())
        .contains(3.14f);
  }
}
