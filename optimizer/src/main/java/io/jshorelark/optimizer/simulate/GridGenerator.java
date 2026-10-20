/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import io.jshorelark.optimizer.OptimizationConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the parameter grid the optimizer sweeps.
 *
 * <p>Mirrors Rust's {@code SimulateCmd} grid value for value: the same four hidden-neuron counts,
 * four vision ranges, four field-of-view angles, five cell counts and two mutation parameters,
 * repeated {@code SIM_ITERATIONS} times per point. Keeping the numbers in one place is what makes a
 * benchmark run reproducible and comparable with the reference implementation.
 */
final class GridGenerator {

  /** Iterations per grid point, matching the reference implementation. */
  static final int SIM_ITERATIONS = 15;

  /** Generations per run, matching the reference implementation. */
  static final int SIM_GENERATIONS = 30;

  /** Iterations per grid point in test mode. */
  static final int TEST_ITERATIONS = 2;

  /** Generations per run in test mode. */
  static final int TEST_GENERATIONS = 3;

  private static final int[] BRAIN_NEURONS = {2, 3, 5, 10};
  private static final float[] EYE_FOV_RANGES = {0.1f, 0.25f, 0.33f, 0.5f};

  /**
   * The reference sweep samples 3.14 rather than {@code Math.PI}; substituting the exact constant
   * would benchmark different configurations than the upstream project did.
   */
  private static final float[] EYE_FOV_ANGLES = {1.0f, 2.0f, 3.14f, 6.0f};

  private static final int[] EYE_CELLS = {2, 3, 6, 9, 12};
  private static final float[] GA_MUT_CHANCES = {0.001f, 0.01f, 0.1f, 0.5f};
  private static final float[] GA_MUT_COEFFS = {0.01f, 0.1f, 0.3f, 0.5f, 1.0f};

  private GridGenerator() {
    // Utility class.
  }

  /**
   * Returns the full grid, one entry per iteration.
   *
   * @return every grid point repeated {@code SIM_ITERATIONS} times
   */
  static List<ConfigurationIteration> generate() {
    List<ConfigurationIteration> configurations = new ArrayList<>();
    for (int neurons : BRAIN_NEURONS) {
      for (float fovRange : EYE_FOV_RANGES) {
        for (float fovAngle : EYE_FOV_ANGLES) {
          for (int cells : EYE_CELLS) {
            for (float mutChance : GA_MUT_CHANCES) {
              for (float mutCoeff : GA_MUT_COEFFS) {
                addIterations(
                    configurations, neurons, fovRange, fovAngle, cells, mutChance, mutCoeff);
              }
            }
          }
        }
      }
    }
    return configurations;
  }

  private static void addIterations(
      List<ConfigurationIteration> configurations,
      int neurons,
      float fovRange,
      float fovAngle,
      int cells,
      float mutChance,
      float mutCoeff) {
    var config = new OptimizationConfig(neurons, fovRange, fovAngle, cells, mutChance, mutCoeff);
    for (int iteration = 0; iteration < SIM_ITERATIONS; iteration++) {
      configurations.add(new ConfigurationIteration(config, iteration));
    }
  }
}
