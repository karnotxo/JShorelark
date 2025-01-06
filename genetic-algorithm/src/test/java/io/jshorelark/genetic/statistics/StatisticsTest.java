/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.genetic.statistics;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.genetic.Individual;

import lombok.Value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests for {@link Statistics}. */
class StatisticsTest {
  /** Tests statistics calculation with an even number of individuals. */
  @Test
  void testEven() {
    var stats =
        Statistics.of(
            List.of(
                TestIndividual.withFitness(30.0f),
                TestIndividual.withFitness(10.0f),
                TestIndividual.withFitness(20.0f),
                TestIndividual.withFitness(40.0f)));

    assertThat(stats.getMinFitness()).isEqualTo(10.0f);
    assertThat(stats.getMaxFitness()).isEqualTo(40.0f);
    assertThat(stats.getAvgFitness()).isEqualTo((10.0f + 20.0f + 30.0f + 40.0f) / 4.0f);
    assertThat(stats.getMedianFitness()).isEqualTo((20.0f + 30.0f) / 2.0f);
  }

  /** Tests statistics calculation with an odd number of individuals. */
  @Test
  void testOdd() {
    var stats =
        Statistics.of(
            List.of(
                TestIndividual.withFitness(30.0f),
                TestIndividual.withFitness(20.0f),
                TestIndividual.withFitness(40.0f)));

    assertThat(stats.getMinFitness()).isEqualTo(20.0f);
    assertThat(stats.getMaxFitness()).isEqualTo(40.0f);
    assertThat(stats.getAvgFitness()).isEqualTo((20.0f + 30.0f + 40.0f) / 3.0f);
    assertThat(stats.getMedianFitness()).isEqualTo(30.0f);
  }

  /** Tests that empty population throws exception. */
  @Test
  void testEmpty() {
    assertThatThrownBy(() -> Statistics.of(List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Population must not be empty");
  }

  /** Test individual implementation that matches Rust's TestIndividual behavior. */
  private sealed interface TestIndividual extends Individual {
    /** Creates a TestIndividual with just a fitness value. */
    static TestIndividual withFitness(float fitness) {
      return new WithFitness(fitness);
    }

    /** Variant with just fitness value. */
    @Value
    final class WithFitness implements TestIndividual {
      float fitness;

      @Override
      public float getFitness() {
        return fitness;
      }

      @Override
      public Chromosome getChromosome() {
        throw new UnsupportedOperationException("not supported for TestIndividual.WithFitness");
      }
    }

    /** Variant with chromosome. */
    @Value
    final class WithChromosome implements TestIndividual {
      Chromosome chromosome;

      @Override
      public float getFitness() {
        float sum = 0;
        for (float gene : chromosome.toArray()) {
          sum += gene;
        }
        return sum;
      }

      @Override
      public Chromosome getChromosome() {
        return chromosome;
      }
    }
  }
}
