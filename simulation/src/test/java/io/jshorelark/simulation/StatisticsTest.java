/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import io.jshorelark.genetic.statistics.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Simulation Statistics")
class StatisticsTest {

  private static Statistics gaStatistics() {
    return new Statistics(1.0f, 4.0f, 2.5f, 2.5f);
  }

  @Test
  @DisplayName("delegates every fitness aggregate to the GA statistics")
  void delegatesAggregates() {
    var ga = gaStatistics();
    var statistics = new io.jshorelark.simulation.Statistics(7, ga);

    assertThat(statistics.generation()).isEqualTo(7);
    assertThat(statistics.ga()).isSameAs(ga);
    assertThat(statistics.minFitness()).isEqualTo(1.0f);
    assertThat(statistics.maxFitness()).isEqualTo(4.0f);
    assertThat(statistics.avgFitness()).isEqualTo(2.5f);
    assertThat(statistics.medianFitness()).isEqualTo(2.5f);
  }

  @Test
  @DisplayName("renders the four fitness aggregates")
  void rendersAggregates() {
    var statistics = new io.jshorelark.simulation.Statistics(7, gaStatistics());

    // Locale-dependent separators are not asserted; the labels are the contract.
    assertThat(statistics.toString())
        .contains("minFitness=", "maxFitness=", "avgFitness=", "medianFitness=");
  }
}
