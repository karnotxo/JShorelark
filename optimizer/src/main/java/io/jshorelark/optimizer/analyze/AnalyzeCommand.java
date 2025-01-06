/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.OptimizationConfig;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Command to analyze simulation results.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
@Command(name = "analyze", description = "Analyze simulation results")
public class AnalyzeCommand implements Runnable {
  /** The input file path. */
  @Option(
      names = {"-i", "--input"},
      description = "Input file path",
      required = true)
  Path inputPath;

  /** The object mapper. */
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * {@inheritDoc}
   *
   * <p>Runs the analysis.
   */
  @Override
  public void run() {
    try {
      Map<OptimizationConfig, ConfigurationStats> results = new HashMap<>();

      try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
        String line;
        while ((line = reader.readLine()) != null) {
          OptimizationLog log = mapper.readValue(line, OptimizationLog.class);

          results
              .computeIfAbsent(log.getConfig(), k -> new ConfigurationStats())
              .addStatistics(log.getStatistics());
        }
      }

      // Print CSV header
      System.out.print("brain_neurons");
      System.out.print(",eye_fov_range");
      System.out.print(",eye_fov_angle");
      System.out.print(",eye_cells");
      System.out.print(",ga_mut_chance");
      System.out.print(",ga_mut_coeff");
      System.out.print(",min_fitness");
      System.out.print(",max_fitness");
      System.out.print(",avg_fitness");
      System.out.print(",median_fitness");
      System.out.println();

      // Print results
      for (Map.Entry<OptimizationConfig, ConfigurationStats> entry : results.entrySet()) {
        OptimizationConfig config = entry.getKey();
        ConfigurationStats stats = entry.getValue();
        stats.average();

        System.out.print(config.getBrainNeurons());
        System.out.print("," + config.getEyeFovRange());
        System.out.print("," + config.getEyeFovAngle());
        System.out.print("," + config.getEyeCells());
        System.out.print("," + config.getGaMutChance());
        System.out.print("," + config.getGaMutCoeff());
        System.out.print("," + stats.getMinFitness());
        System.out.print("," + stats.getMaxFitness());
        System.out.print("," + stats.getAvgFitness());
        System.out.print("," + stats.getMedianFitness());
        System.out.println();
      }
    } catch (IOException e) {
      log.error("Failed to analyze results", e);
      throw new RuntimeException(e);
    }
  }
}

/** Statistics for a configuration. */
class ConfigurationStats {
  /** Number of samples. */
  private int samples;

  /** Minimum fitness. */
  private float minFitness;

  /**
   * Maximum fitness.
   *
   * @param stats a {@link io.jshorelark.optimizer.OptimizationStatistics} object
   */
  private float maxFitness;

  /** Average fitness. */
  private float avgFitness;

  /** Median fitness. */
  private float medianFitness;

  /**
   * Adds statistics to this configuration.
   *
   * @param stats a {@link io.jshorelark.optimizer.OptimizationStatistics} object
   */
  public void addStatistics(OptimizationStatistics stats) {
    samples++;
    minFitness += stats.getGa().getMinFitness();
    maxFitness += stats.getGa().getMaxFitness();
    avgFitness += stats.getGa().getAvgFitness();
    medianFitness += stats.getGa().getMedianFitness();
  }

  /** Averages the statistics. */
  public void average() {
    float sampleCount = samples;
    minFitness /= sampleCount;
    maxFitness /= sampleCount;
    avgFitness /= sampleCount;
    medianFitness /= sampleCount;
  }

  /**
   * Gets the minimum fitness.
   *
   * @return a float
   */
  public float getMinFitness() {
    return minFitness;
  }

  /**
   * Gets the maximum fitness.
   *
   * @return a float
   */
  public float getMaxFitness() {
    return maxFitness;
  }

  /**
   * Gets the average fitness.
   *
   * @return a float
   */
  public float getAvgFitness() {
    return avgFitness;
  }

  /**
   * Gets the median fitness.
   *
   * @return a float
   */
  public float getMedianFitness() {
    return medianFitness;
  }
}
