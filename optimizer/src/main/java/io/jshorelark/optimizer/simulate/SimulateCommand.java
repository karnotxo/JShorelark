/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.OptimizationConfig;
import io.jshorelark.optimizer.OptimizationContext;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;
import io.jshorelark.optimizer.OptimizingSimulation;

import lombok.extern.slf4j.Slf4j;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Grid-searches simulation parameters and writes one JSONL line per generation.
 *
 * <p>Mirrors Rust's {@code SimulateCmd}: the same parameter grid, the same iteration and generation
 * counts, the grid shuffled before it is spread over the available cores, and a single writer thread
 * so the simulations never block on disk.
 */
@Slf4j
@Command(name = "simulate", description = "Run simulations")
public final class SimulateCommand implements Runnable {

  /** Iterations per grid point, matching the reference implementation. */
  private static final int SIM_ITERATIONS = 15;

  /** Generations per run, matching the reference implementation. */
  private static final int SIM_GENERATIONS = 30;

  /** Iterations per grid point in test mode. */
  private static final int TEST_ITERATIONS = 2;

  /** Generations per run in test mode. */
  private static final int TEST_GENERATIONS = 3;

  /** How often the progress line is refreshed. */
  private static final long PROGRESS_INTERVAL_MILLIS = 1_000L;

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
    List<ConfigurationIteration> configurations = generateConfigurations();
    if (testMode) {
      configurations = configurations.subList(0, Math.min(TEST_ITERATIONS, configurations.size()));
    }
    Collections.shuffle(configurations, ThreadLocalRandom.current());

    int generations = testMode ? TEST_GENERATIONS : SIM_GENERATIONS;
    int totalGenerations = configurations.size() * generations;
    log.info(
        "Simulating {} configurations x {} generations = {} steps",
        configurations.size(),
        generations,
        totalGenerations);

    // Unbounded: the writer is the only consumer and must never stall a simulation.
    var queue = new LinkedBlockingQueue<OptimizationLog>();
    var done = new AtomicBoolean(false);
    var writer = startWriter(queue, done);
    var monitor = startProgressMonitor(startedAt, done, totalGenerations);

    try {
      List<ConfigurationIteration> work = configurations;
      work.parallelStream().forEach(configuration -> simulate(configuration, generations, queue));
    } finally {
      done.set(true);
      awaitTermination(writer, "optimizer-writer");
      awaitTermination(monitor, "optimizer-progress");
    }

    log.info("Completed in {}", Duration.between(startedAt, Instant.now()));
  }

  /**
   * Runs every generation of one grid point, pushing each result to the writer.
   *
   * @param configuration the grid point and its iteration index
   * @param generations how many generations to run
   * @param queue the hand-off queue to the writer thread
   */
  private void simulate(
      ConfigurationIteration configuration, int generations, LinkedBlockingQueue<OptimizationLog> queue) {
    Random random = ThreadLocalRandom.current();
    var simulation = new OptimizingSimulation(configuration.config(), random);
    for (int generation = 0; generation < generations; generation++) {
      OptimizationStatistics statistics = simulation.train(random);
      queue.add(
          new OptimizationLog(
              configuration.config(),
              new OptimizationContext(generation, configuration.iteration()),
              statistics));
      writtenGenerations.incrementAndGet();
    }
  }

  /**
   * Starts the thread that serialises results to the output file.
   *
   * @param queue the queue filled by the simulations
   * @param done flipped to {@code true} once no more results will arrive
   * @return the running writer thread
   */
  private Thread startWriter(LinkedBlockingQueue<OptimizationLog> queue, AtomicBoolean done) {
    var writer =
        new Thread(
            () -> {
              try (BufferedWriter out = Files.newBufferedWriter(outputPath)) {
                while (true) {
                  OptimizationLog entry = queue.poll(50, TimeUnit.MILLISECONDS);
                  if (entry == null) {
                    if (done.get() && queue.isEmpty()) {
                      return;
                    }
                    continue;
                  }
                  out.write(mapper.writeValueAsString(entry));
                  out.newLine();
                }
              } catch (IOException e) {
                throw new UncheckedIOException("Failed to write " + outputPath, e);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            "optimizer-writer");
    writer.setDaemon(true);
    writer.start();
    return writer;
  }

  /**
   * Starts the thread that logs progress, mirroring the reference implementation's ETA line.
   *
   * @param startedAt when the run started
   * @param done flipped to {@code true} once the run is over
   * @param totalGenerations how many generations the run will produce
   * @return the running monitor thread
   */
  private Thread startProgressMonitor(
      Instant startedAt, AtomicBoolean done, int totalGenerations) {
    var monitor =
        new Thread(
            () -> {
              while (!done.get()) {
                int written = writtenGenerations.get();
                long elapsedSeconds = Duration.between(startedAt, Instant.now()).toSeconds();
                long etaSeconds =
                    written > 0 ? elapsedSeconds * (totalGenerations - written) / written : 0L;
                log.info("{}/{} generations | eta {}s", written, totalGenerations, etaSeconds);
                try {
                  Thread.sleep(PROGRESS_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return;
                }
              }
            },
            "optimizer-progress");
    monitor.setDaemon(true);
    monitor.start();
    return monitor;
  }

  private static void awaitTermination(Thread thread, String name) {
    try {
      thread.join(10_000L);
      if (thread.isAlive()) {
        log.warn("{} did not finish in time", name);
        thread.interrupt();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** Generates the reference grid, one entry per iteration. */
  private static List<ConfigurationIteration> generateConfigurations() {
    int[] brainNeurons = {2, 3, 5, 10};
    float[] eyeFovRange = {0.1f, 0.25f, 0.33f, 0.5f};
    float[] eyeFovAngle = {1.0f, 2.0f, 3.14f, 6.0f};
    int[] eyeCells = {2, 3, 6, 9, 12};
    float[] gaMutChance = {0.001f, 0.01f, 0.1f, 0.5f};
    float[] gaMutCoeff = {0.01f, 0.1f, 0.3f, 0.5f, 1.0f};

    List<ConfigurationIteration> configurations = new ArrayList<>();
    for (int neurons : brainNeurons) {
      for (float fovRange : eyeFovRange) {
        for (float fovAngle : eyeFovAngle) {
          for (int cells : eyeCells) {
            for (float mutChance : gaMutChance) {
              for (float mutCoeff : gaMutCoeff) {
                var config =
                    new OptimizationConfig(neurons, fovRange, fovAngle, cells, mutChance, mutCoeff);
                for (int iteration = 0; iteration < SIM_ITERATIONS; iteration++) {
                  configurations.add(new ConfigurationIteration(config, iteration));
                }
              }
            }
          }
        }
      }
    }
    return configurations;
  }

  /**
   * A grid point together with its repetition index.
   *
   * @param config the grid point
   * @param iteration the repetition index
   */
  record ConfigurationIteration(OptimizationConfig config, int iteration) {}
}
