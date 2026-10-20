/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import io.jshorelark.optimizer.OptimizationContext;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;
import io.jshorelark.optimizer.OptimizingSimulation;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import tools.jackson.databind.ObjectMapper;

/**
 * Grid-searches simulation parameters and writes one JSONL line per generation.
 *
 * <p>Mirrors Rust's {@code SimulateCmd}: the same parameter grid, the same iteration and generation
 * counts, the grid shuffled before it is spread over the available cores, and a single writer
 * thread so the simulations never block on disk.
 *
 * <p>The command only orchestrates: the grid lives in {@link GridGenerator}, the file sink in
 * {@link JsonlLogWriter} and the ETA line in {@link ProgressMonitor}.
 */
@Slf4j
@Command(name = "simulate", description = "Run simulations")
@SuppressWarnings("PMD.DoNotUseThreads") // Adapter: the optimizer owns the only worker threads.
public final class SimulateCommand implements Runnable {

  /** The output file path. */
  @Option(
      names = {"-o", "--output"},
      description = "Output file path",
      required = true)
  Path outputPath;

  /** Whether to run a reduced grid, used by the smoke test. */
  @Option(
      names = {"-t", "--test"},
      description = "Run a reduced grid for smoke testing")
  boolean testMode;

  /** The object mapper for JSON serialisation. */
  private final ObjectMapper mapper = new ObjectMapper();

  /** Generations written so far; drives the progress line. */
  private final AtomicInteger writtenGenerations = new AtomicInteger();

  /** {@inheritDoc} */
  @Override
  public void run() {
    Instant startedAt = Instant.now();
    List<ConfigurationIteration> configurations = configurationsToRun();
    int generations = testMode ? GridGenerator.TEST_GENERATIONS : GridGenerator.SIM_GENERATIONS;
    int totalGenerations = configurations.size() * generations;
    log.info(
        "Simulating {} configurations x {} generations = {} steps",
        configurations.size(),
        generations,
        totalGenerations);

    // The writer is closed first and only then the monitor, so every queued entry reaches the file
    // before the progress line stops reporting.
    var writer = JsonlLogWriter.start(outputPath, mapper);
    var monitor = ProgressMonitor.start(writtenGenerations, totalGenerations, startedAt);
    try {
      configurations.parallelStream()
          .forEach(configuration -> simulate(configuration, generations, writer));
    } finally {
      writer.close();
      monitor.close();
    }

    log.info("Completed in {}", Duration.between(startedAt, Instant.now()));
  }

  /**
   * Returns the grid points this run must cover.
   *
   * <p>The grid is shuffled before it is spread over the cores, so an interrupted run still leaves
   * a representative sample behind; test mode keeps only the first few points.
   *
   * @return the grid points, shuffled
   */
  private List<ConfigurationIteration> configurationsToRun() {
    List<ConfigurationIteration> configurations = GridGenerator.generate();
    if (testMode) {
      configurations =
          configurations.subList(0, Math.min(GridGenerator.TEST_ITERATIONS, configurations.size()));
    }
    Collections.shuffle(configurations, ThreadLocalRandom.current());
    return configurations;
  }

  /**
   * Runs every generation of one grid point, handing each result to the writer.
   *
   * @param configuration the grid point and its iteration index
   * @param generations how many generations to run
   * @param writer the sink for the produced entries
   */
  private void simulate(
      ConfigurationIteration configuration, int generations, JsonlLogWriter writer) {
    Random random = ThreadLocalRandom.current();
    var simulation = new OptimizingSimulation(configuration.config(), random);
    for (int generation = 0; generation < generations; generation++) {
      OptimizationStatistics statistics = simulation.train(random);
      writer.submit(
          new OptimizationLog(
              configuration.config(),
              new OptimizationContext(generation, configuration.iteration()),
              statistics));
      writtenGenerations.incrementAndGet();
    }
  }
}
