/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer.simulate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jshorelark.optimizer.OptimizationLog;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("SimulateCommand")
class SimulateCommandTest {
  @TempDir Path tempDir;

  @Test
  @Timeout(value = 30)
  @DisplayName("runs simulations and generates output")
  void runsSimulationsAndGeneratesOutput() throws IOException {
    // Setup
    Path outputPath = tempDir.resolve("simulation_results.jsonl");
    log.info("Running simulation test with output path: {}", outputPath);

    // Run command with test configuration
    var cmd = new SimulateCommand();
    cmd.outputPath = outputPath;
    cmd.testMode = true;
    cmd.run();

    // Verify output
    assertThat(outputPath).exists().isRegularFile().isReadable();
    long fileSize = Files.size(outputPath);
    assertThat(fileSize).as("Output file should not be empty").isGreaterThan(0);

    // Verify content
    List<String> lines = Files.readAllLines(outputPath);
    ObjectMapper mapper = new ObjectMapper();
    assertThat(lines)
        .isNotEmpty()
        .allSatisfy(
            line -> {
              try {
                OptimizationLog log = mapper.readValue(line, OptimizationLog.class);
                assertThat(log.getConfig()).isNotNull();
                assertThat(log.getContext()).isNotNull();
                assertThat(log.getStatistics()).isNotNull();
              } catch (IOException e) {
                throw new AssertionError("Failed to parse JSON line: " + line, e);
              }
            });
  }
}
