/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer.analyze;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.genetic.statistics.Statistics;
import io.jshorelark.optimizer.OptimizationConfig;
import io.jshorelark.optimizer.OptimizationContext;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("AnalyzeCommand")
class AnalyzeCommandTest {
  @TempDir Path tempDir;

  @Test
  @DisplayName("analyzes simulation results")
  void analyzesSimulationResults() throws IOException {
    // Create test input file
    Path inputPath = tempDir.resolve("test_input.jsonl");
    log.info("Creating test input at: {}", inputPath);

    // Create sample data
    var config =
        OptimizationConfig.builder()
            .brainNeurons(5)
            .eyeFovRange(0.25f)
            .eyeFovAngle(3.14f)
            .eyeCells(9)
            .gaMutChance(0.01f)
            .gaMutCoeff(0.3f)
            .build();

    var context = new OptimizationContext(0, 0);

    var stats =
        OptimizationStatistics.builder()
            .minFitness(1.0f)
            .maxFitness(2.0f)
            .avgFitness(1.5f)
            .medianFitness(1.5f)
            .generation(0)
            .ga(new Statistics(1.0f, 2.0f, 1.5f, 1.5f))
            .build();

    var log = OptimizationLog.builder().config(config).context(context).statistics(stats).build();

    // Write sample data
    ObjectMapper mapper = new ObjectMapper();
    Files.writeString(inputPath, mapper.writeValueAsString(log));

    // Capture stdout
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    try {
      // Run command
      var cmd = new AnalyzeCommand();
      cmd.inputPath = inputPath;
      cmd.run();

      // Verify output
      String output = outContent.toString();
      assertThat(output)
          .contains("brain_neurons")
          .contains("eye_fov_range")
          .contains("eye_fov_angle")
          .contains("eye_cells")
          .contains("ga_mut_chance")
          .contains("ga_mut_coeff")
          .contains("min_fitness")
          .contains("max_fitness")
          .contains("avg_fitness")
          .contains("median_fitness");
    } finally {
      System.setOut(originalOut);
    }
  }
}
