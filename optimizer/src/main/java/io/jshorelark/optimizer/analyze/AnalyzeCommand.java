/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.OptimizationConfig;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;

import lombok.extern.slf4j.Slf4j;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Averages the JSONL produced by {@code simulate} per configuration and prints it as CSV.
 *
 * <p>Mirrors Rust's {@code AnalyzeCmd}. Configurations are kept in a sorted map so two runs over the
 * same input produce byte-identical CSV.
 */
@Slf4j
@Command(name = "analyze", description = "Analyze simulation results")
public final class AnalyzeCommand implements Runnable {

  /** The CSV header, in the same column order as the Rust implementation. */
  private static final String[] COLUMNS = {
    "brain_neurons",
    "eye_fov_range",
    "eye_fov_angle",
    "eye_cells",
    "ga_mut_chance",
    "ga_mut_coeff",
    "min_fitness",
    "max_fitness",
    "avg_fitness",
    "median_fitness"
  };

  /** The input file path. */
  @Option(
      names = {"-i", "--input"},
      description = "Input file path",
      required = true)
  Path inputPath;

  /** The object mapper. */
  private final ObjectMapper mapper = new ObjectMapper();

  /** {@inheritDoc} */
  @Override
  public void run() {
    var results = readResults();
    PrintStream out = System.out;
    out.println(String.join(",", COLUMNS));
    results.forEach((config, stats) -> printRow(out, config, stats.average()));
  }

  private Map<OptimizationConfig, FitnessAccumulator> readResults() {
    Map<OptimizationConfig, FitnessAccumulator> results = new TreeMap<>(AnalyzeCommand::compare);
    try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        OptimizationLog entry = mapper.readValue(line, OptimizationLog.class);
        results
            .computeIfAbsent(entry.config(), key -> new FitnessAccumulator())
            .add(entry.statistics());
      }
    } catch (IOException e) {
      log.error("Failed to analyze results", e);
      throw new UncheckedIOException(e);
    }
    return results;
  }

  private static void printRow(
      PrintStream out, OptimizationConfig config, OptimizationStatistics stats) {
    out.printf(
        "%d,%s,%s,%d,%s,%s,%s,%s,%s,%s%n",
        config.brainNeurons(),
        config.eyeFovRange(),
        config.eyeFovAngle(),
        config.eyeCells(),
        config.gaMutChance(),
        config.gaMutCoeff(),
        stats.minFitness(),
        stats.maxFitness(),
        stats.avgFitness(),
        stats.medianFitness());
  }

  /** Total ordering over grid points so the CSV is deterministic. */
  private static int compare(OptimizationConfig left, OptimizationConfig right) {
    int result = Integer.compare(left.brainNeurons(), right.brainNeurons());
    if (result == 0) {
      result = Float.compare(left.eyeFovRange(), right.eyeFovRange());
    }
    if (result == 0) {
      result = Float.compare(left.eyeFovAngle(), right.eyeFovAngle());
    }
    if (result == 0) {
      result = Integer.compare(left.eyeCells(), right.eyeCells());
    }
    if (result == 0) {
      result = Float.compare(left.gaMutChance(), right.gaMutChance());
    }
    if (result == 0) {
      result = Float.compare(left.gaMutCoeff(), right.gaMutCoeff());
    }
    return result;
  }
}

/** Sums the fitness values of every sample of one configuration. */
final class FitnessAccumulator {

  /** Number of samples folded in so far. */
  private int samples;

  /** Running sums, divided by {@link #samples} when averaging. */
  private float minFitness;

  private float maxFitness;
  private float avgFitness;
  private float medianFitness;

  /**
   * Folds one generation's statistics into the running sums.
   *
   * @param stats the statistics of one generation
   */
  void add(OptimizationStatistics stats) {
    samples++;
    minFitness += stats.minFitness();
    maxFitness += stats.maxFitness();
    avgFitness += stats.avgFitness();
    medianFitness += stats.medianFitness();
  }

  /**
   * Divides the running sums by the sample count.
   *
   * @return the averaged statistics, or all zeros when nothing was accumulated
   */
  OptimizationStatistics average() {
    if (samples == 0) {
      return new OptimizationStatistics(0.0f, 0.0f, 0.0f, 0.0f);
    }
    float count = samples;
    return new OptimizationStatistics(
        minFitness / count, maxFitness / count, avgFitness / count, medianFitness / count);
  }
}
