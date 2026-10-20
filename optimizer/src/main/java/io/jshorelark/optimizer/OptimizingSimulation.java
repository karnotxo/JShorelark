/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import io.jshorelark.simulation.Simulation;
import java.util.Objects;
import java.util.Random;

/**
 * Adapter that runs a grid-search configuration for a fixed number of generations.
 *
 * <p>Rust's optimizer drives {@code Simulation::train} directly; this class exists only to expose
 * it in terms of the optimizer's persisted value types. All the rules — evolution, selection, food
 * respawn — stay in {@link Simulation}, so the headless search and the JavaFX view cannot drift
 * apart.
 */
public final class OptimizingSimulation {

  /** The simulation being trained. */
  private final Simulation simulation;

  /**
   * Creates a simulation for one grid point.
   *
   * @param config the grid-search configuration
   * @param random the random source
   */
  public OptimizingSimulation(OptimizationConfig config, Random random) {
    Objects.requireNonNull(config, "config");
    this.simulation = new Simulation(config.toSimulationConfig(), Objects.requireNonNull(random));
  }

  /**
   * Trains the simulation until one generation completes.
   *
   * @param random the random source
   * @return the statistics of the generation that just ended
   */
  public OptimizationStatistics train(Random random) {
    var statistics = simulation.train(random).getGa();
    return OptimizationStatistics.from(statistics);
  }

  /**
   * Returns the number of completed generations.
   *
   * @return the generation count
   */
  public int getGeneration() {
    return simulation.getGeneration();
  }
}
