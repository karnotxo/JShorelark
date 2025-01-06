/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer.simulate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.*;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command to run simulations, matching Rust's implementation with Java optimizations.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
@Command(name = "simulate", description = "Run simulations")
public class SimulateCommand implements Runnable {
  /** Number of iterations per configuration. */
  private static final int SIM_ITERATIONS = 15;

  /** Number of generations per simulation. */
  private static final int SIM_GENERATIONS = 30;

  /** Test mode iterations. */
  private static final int TEST_ITERATIONS = 2;

  /** Test mode generations. */
  private static final int TEST_GENERATIONS = 3;

  /** Buffer size for writers. */
  private static final int BUFFER_SIZE = 8192;

  /** The output file path. */
  @Option(
      names = {"-o", "--output"},
      description = "Output file path",
      required = true)
  Path outputPath;

  /** Whether to run in test mode with reduced iterations. */
  boolean testMode = false;

  /** The object mapper for JSON serialization. */
  private final ObjectMapper mapper = new ObjectMapper();

  /** Statistics for performance monitoring. */
  private final LongAdder processedConfigs = new LongAdder();

  private final LongAdder processedGenerations = new LongAdder();
  private final LongAdder queueWaitTime = new LongAdder();

  /**
   * {@inheritDoc}
   *
   * <p>Runs the simulations.
   */
  @Override
  public void run() {
    Instant startedAt = Instant.now();
    log.info("Starting simulation run at {}", startedAt);

    AtomicBoolean isDone = new AtomicBoolean(false);
    AtomicInteger doneSteps = new AtomicInteger(0);

    // Generate and shuffle configurations
    List<ConfigurationIteration> configurations = generateConfigurations();
    if (testMode) {
      // Use smaller subset for testing
      configurations = configurations.subList(0, Math.min(TEST_ITERATIONS, configurations.size()));
    }
    int totalConfigs = configurations.size();
    log.info("Generated {} configurations", totalConfigs);

    Collections.shuffle(configurations, ThreadLocalRandom.current());
    int generations = testMode ? TEST_GENERATIONS : SIM_GENERATIONS;
    int totalSteps = configurations.size() * generations;
    log.info("Total steps to process: {}", totalSteps);

    // Create unbounded queue for better throughput
    TransferQueue<OptimizationLog> logQueue = new LinkedTransferQueue<>();

    // Create threads for logging and monitoring
    Thread logWriter = createLogWriter(logQueue, isDone);
    Thread progressMonitor = createProgressMonitor(startedAt, isDone, doneSteps, totalSteps);

    try {
      // Start auxiliary threads
      logWriter.start();
      progressMonitor.start();
      log.info("Started auxiliary threads");

      // Process configurations in parallel
      log.info("Starting parallel processing of configurations");
      configurations.parallelStream()
          .forEach(
              config -> {
                MDC.put(
                    "config",
                    String.format(
                        "n%d_r%.2f_a%.2f_c%d_mc%.3f_co%.2f_i%d",
                        config.getConfig().getBrainNeurons(),
                        config.getConfig().getEyeFovRange(),
                        config.getConfig().getEyeFovAngle(),
                        config.getConfig().getEyeCells(),
                        config.getConfig().getGaMutChance(),
                        config.getConfig().getGaMutCoeff(),
                        config.getIteration()));
                try {
                  processConfiguration(config, logQueue, doneSteps, generations);
                  processedConfigs.increment();
                } finally {
                  MDC.remove("config");
                }
              });

    } finally {
      // Cleanup in reverse order
      cleanup(isDone, logWriter, progressMonitor);
    }

    Duration totalTime = Duration.between(startedAt, Instant.now());
    logFinalStatistics(totalTime, totalConfigs);
  }

  private Thread createLogWriter(TransferQueue<OptimizationLog> logQueue, AtomicBoolean isDone) {
    return new Thread(
        () -> {
          try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            int batchSize = 0;
            while (!isDone.get() || !logQueue.isEmpty()) {
              try {
                Instant start = Instant.now();
                OptimizationLog optLog = logQueue.poll();
                if (optLog != null) {
                  writer.write(mapper.writeValueAsString(optLog));
                  writer.newLine();
                  batchSize++;

                  // Batch flush for better I/O performance
                  if (batchSize >= 100) {
                    writer.flush();
                    batchSize = 0;
                    log.debug("Flushed {} log entries", batchSize);
                  }

                  queueWaitTime.add(Duration.between(start, Instant.now()).toMillis());
                } else {
                  Thread.sleep(10); // Small sleep when queue is empty
                }
              } catch (InterruptedException e) {
                log.info("Log writer interrupted, finishing up");
                break;
              }
            }
            // Final flush
            writer.flush();
          } catch (IOException e) {
            log.error("Failed to write results", e);
          }
        },
        "LogWriter");
  }

  private Thread createProgressMonitor(
      Instant startedAt, AtomicBoolean isDone, AtomicInteger doneSteps, int totalSteps) {
    return new Thread(
        () -> {
          while (!isDone.get()) {
            int done = doneSteps.get();
            int remaining = totalSteps - done;

            Duration elapsed = Duration.between(startedAt, Instant.now());
            long eta = done > 0 ? (long) ((elapsed.toSeconds() / (float) done) * remaining) : 0;

            log.info(
                "Progress: {} / {} steps | Configs: {} | Generations: {} | Queue wait: {}ms/op |"
                    + " ETA: {}s",
                done,
                totalSteps,
                processedConfigs.sum(),
                processedGenerations.sum(),
                queueWaitTime.sum() / Math.max(1, processedGenerations.sum()),
                eta);

            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        },
        "ProgressMonitor");
  }

  private void processConfiguration(
      ConfigurationIteration config,
      TransferQueue<OptimizationLog> logQueue,
      AtomicInteger doneSteps,
      int generations) {
    try {
      log.debug("Processing configuration: {}", config);
      Instant start = Instant.now();

      Random random = ThreadLocalRandom.current();
      final var sim = new OptimizingSimulation(config.getConfig(), random);

      for (int gen = 0; gen < generations; gen++) {
        OptimizationStatistics stats = sim.train(random);
        final var optLog =
            OptimizationLog.builder()
                .config(config.getConfig())
                .context(new OptimizationContext(gen, config.getIteration()))
                .statistics(stats)
                .build();

        // Use transfer queue's put operation which will never fail
        logQueue.put(optLog);
        doneSteps.incrementAndGet();
        processedGenerations.increment();
      }

      Duration processingTime = Duration.between(start, Instant.now());
      log.debug("Completed configuration in {}ms", processingTime.toMillis());

    } catch (Exception e) {
      log.error("Failed to process configuration: {}", config, e);
      throw new RuntimeException(e);
    }
  }

  private void cleanup(AtomicBoolean isDone, Thread logWriter, Thread progressMonitor) {
    log.info("Starting cleanup");
    isDone.set(true);

    try {
      // Wait for writer to finish with timeout
      logWriter.join(10000); // 10 seconds timeout
      if (logWriter.isAlive()) {
        log.warn("Log writer did not finish within timeout, interrupting");
        logWriter.interrupt();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while waiting for log writer to finish");
      logWriter.interrupt();
    }

    // Interrupt progress monitor
    progressMonitor.interrupt();
    try {
      progressMonitor.join(1000);
      if (progressMonitor.isAlive()) {
        log.warn("Progress monitor did not finish within timeout");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while waiting for progress monitor to finish");
    }

    // Force interrupt any remaining threads
    if (logWriter.isAlive() || progressMonitor.isAlive()) {
      log.warn("Some threads did not terminate properly");
    }
  }

  private void logFinalStatistics(Duration totalTime, int totalConfigs) {
    log.info("Simulation completed in {}", totalTime);
    log.info("Configurations processed: {}", processedConfigs.sum());
    log.info("Generations processed: {}", processedGenerations.sum());
    log.info("Average time per configuration: {}ms", totalTime.toMillis() / totalConfigs);
    log.info(
        "Average queue wait time: {}ms",
        queueWaitTime.sum() / Math.max(1, processedGenerations.sum()));
  }

  /** Generates all possible configurations using streams for better readability. */
  private List<ConfigurationIteration> generateConfigurations() {
    // Match Rust's parameter combinations exactly
    int[] brainNeurons = {2, 3, 5, 10};
    float[] eyeFovRange = {0.1f, 0.25f, 0.33f, 0.5f};
    float[] eyeFovAngle = {1.0f, 2.0f, 3.14f, 6.0f};
    int[] eyeCells = {2, 3, 6, 9, 12};
    float[] gaMutChance = {0.001f, 0.01f, 0.1f, 0.5f};
    float[] gaMutCoeff = {0.01f, 0.1f, 0.3f, 0.5f, 1.0f};

    List<ConfigurationIteration> configurations = new ArrayList<>();

    // Generate all combinations matching Rust's iproduct! macro behavior
    for (int neurons : brainNeurons) {
      for (float fovRange : eyeFovRange) {
        for (float fovAngle : eyeFovAngle) {
          for (int cells : eyeCells) {
            for (float mutChance : gaMutChance) {
              for (float mutCoeff : gaMutCoeff) {
                // For each configuration, create SIM_ITERATIONS copies with different iteration
                // numbers
                for (int iter = 0; iter < SIM_ITERATIONS; iter++) {
                  final var config =
                      OptimizationConfig.builder()
                          .brainNeurons(neurons)
                          .eyeFovRange(fovRange)
                          .eyeFovAngle(fovAngle)
                          .eyeCells(cells)
                          .gaMutChance(mutChance)
                          .gaMutCoeff(mutCoeff)
                          .build();

                  configurations.add(new ConfigurationIteration(config, iter));
                }
              }
            }
          }
        }
      }
    }

    return configurations;
  }
}
