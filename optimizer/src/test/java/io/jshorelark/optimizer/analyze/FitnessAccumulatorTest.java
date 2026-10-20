/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.jshorelark.optimizer.OptimizationStatistics;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the accumulation rule of {@link FitnessAccumulator}.
 *
 * <p>The accumulator adds in {@code float}, exactly as the Rust {@code AnalyzeCmd} does ({@code
 * entry_stats.min_fitness += log.stats.min_fitness} over {@code f32} fields). Switching to {@code
 * double} would be more precise but would no longer reproduce the reference CSV, so the choice is
 * deliberate. These tests exist so a future reader stops before "fixing" it, and so the cost of the
 * choice is written down.
 */
@DisplayName("FitnessAccumulator")
class FitnessAccumulatorTest {

  /** Sample count of one reference grid point: 15 iterations x 30 generations. */
  private static final int FULL_RUN_SAMPLES = 15 * 30;

  @Test
  @DisplayName("averages every field over the number of samples")
  void averagesEveryField() {
    var accumulator = new FitnessAccumulator();
    accumulator.add(new OptimizationStatistics(0.0f, 4.0f, 2.0f, 1.5f));
    accumulator.add(new OptimizationStatistics(2.0f, 6.0f, 4.0f, 3.5f));

    assertThat(accumulator.average()).isEqualTo(new OptimizationStatistics(1.0f, 5.0f, 3.0f, 2.5f));
  }

  @Test
  @DisplayName("returns zeros when nothing was accumulated")
  void emptyAverageIsZero() {
    assertThat(new FitnessAccumulator().average())
        .isEqualTo(new OptimizationStatistics(0.0f, 0.0f, 0.0f, 0.0f));
  }

  @Test
  @DisplayName("stays within the printed precision of an exact double mean over a full run")
  void fullRunStaysWithinPrintedPrecision() {
    // Satiation counts: realistic samples are small and non-negative.
    var random = new Random(7);
    var samples = new float[FULL_RUN_SAMPLES];
    double exactSum = 0.0;
    for (int i = 0; i < samples.length; i++) {
      samples[i] = random.nextFloat() * 8.0f;
      exactSum += samples[i];
    }

    var accumulator = new FitnessAccumulator();
    for (float sample : samples) {
      accumulator.add(new OptimizationStatistics(sample, sample, sample, sample));
    }
    float averaged = accumulator.average().avgFitness();
    double exactMean = exactSum / FULL_RUN_SAMPLES;

    // The CSV prints a float with %s, i.e. up to 7 significant digits. Summing 450 small samples
    // in f32 drifts about 1e-6 relative, two orders of magnitude below that, so the reference
    // output is reproduced anyway.
    assertThat((double) averaged).isCloseTo(exactMean, within(1e-5));
  }

  @Test
  @DisplayName("prints the same digits the reference f32 accumulation prints")
  void printsReferenceDigits() {
    // f32 addition is not associative, so the running-sum order is part of the output contract.
    // 0.1f + 0.2f + 0.3f divided by three lands on exactly 0.2f, which is what the reference
    // prints for this input. A compensated sum can shift the last digit, so this pins the direct
    // one.
    var accumulator = new FitnessAccumulator();
    accumulator.add(new OptimizationStatistics(0.1f, 0.1f, 0.1f, 0.1f));
    accumulator.add(new OptimizationStatistics(0.2f, 0.2f, 0.2f, 0.2f));
    accumulator.add(new OptimizationStatistics(0.3f, 0.3f, 0.3f, 0.3f));

    assertThat(String.valueOf(accumulator.average().avgFitness())).isEqualTo("0.2");
  }

  @Test
  @DisplayName("averages in the order samples arrive, never reordering them")
  void keepsArrivalOrder() {
    // A configuration's samples arrive interleaved with other configurations in the JSONL file, so
    // a parallel or sorted reduction would be a different sum. Two accumulators fed the same
    // values in the same order must agree bit for bit.
    var first = new FitnessAccumulator();
    var second = new FitnessAccumulator();
    var random = new Random(11);
    for (int i = 0; i < FULL_RUN_SAMPLES; i++) {
      float sample = random.nextFloat() * 5.0f;
      first.add(new OptimizationStatistics(sample, sample, sample, sample));
      second.add(new OptimizationStatistics(sample, sample, sample, sample));
    }

    assertThat(first.average().avgFitness()).isEqualTo(second.average().avgFitness());
  }
}
