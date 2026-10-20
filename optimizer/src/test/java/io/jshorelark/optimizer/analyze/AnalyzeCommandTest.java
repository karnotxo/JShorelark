/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.analyze;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.OptimizationConfig;
import io.jshorelark.optimizer.OptimizationContext;
import io.jshorelark.optimizer.OptimizationLog;
import io.jshorelark.optimizer.OptimizationStatistics;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnalyzeCommand")
class AnalyzeCommandTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir Path tempDir;

  @Test
  @DisplayName("averages samples of the same configuration")
  void averagesSamplesOfTheSameConfiguration() throws IOException {
    var config = new OptimizationConfig(5, 0.25f, 3.14f, 9, 0.01f, 0.3f);
    Path input = tempDir.resolve("input.jsonl");
    Files.write(
        input,
        List.of(
            MAPPER.writeValueAsString(log(config, 1.0f, 2.0f, 1.5f, 1.5f)),
            MAPPER.writeValueAsString(log(config, 3.0f, 4.0f, 3.5f, 3.5f))));

    String output = runAndCapture(input);

    assertThat(output)
        .contains("brain_neurons,eye_fov_range,eye_fov_angle,eye_cells,ga_mut_chance,ga_mut_coeff")
        .contains("5,0.25,3.14,9,0.01,0.3,2.0,3.0,2.5,2.5");
  }

  @Test
  @DisplayName("keeps configurations sorted for a deterministic CSV")
  void keepsConfigurationsSorted() throws IOException {
    var second = new OptimizationConfig(10, 0.5f, 6.0f, 12, 0.5f, 1.0f);
    var first = new OptimizationConfig(2, 0.1f, 1.0f, 2, 0.001f, 0.01f);
    Path input = tempDir.resolve("sorted.jsonl");
    Files.write(
        input,
        List.of(
            MAPPER.writeValueAsString(log(second, 1.0f, 1.0f, 1.0f, 1.0f)),
            MAPPER.writeValueAsString(log(first, 1.0f, 1.0f, 1.0f, 1.0f))));

    List<String> rows = runAndCapture(input).lines().toList();

    assertThat(rows).hasSize(3);
    assertThat(rows.get(1)).startsWith("2,0.1,1.0,2");
    assertThat(rows.get(2)).startsWith("10,0.5,6.0,12");
  }

  private static OptimizationLog log(
      OptimizationConfig config, float min, float max, float avg, float median) {
    return new OptimizationLog(
        config,
        new OptimizationContext(0, 0),
        new OptimizationStatistics(min, max, avg, median));
  }

  private String runAndCapture(Path input) {
    var command = new AnalyzeCommand();
    command.inputPath = input;

    PrintStream originalOut = System.out;
    var captured = new ByteArrayOutputStream();
    System.setOut(new PrintStream(captured));
    try {
      command.run();
    } finally {
      System.setOut(originalOut);
    }
    return captured.toString();
  }
}
