/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimulateCommand")
class SimulateCommandTest {

  @TempDir Path tempDir;

  @Test
  @Timeout(60)
  @DisplayName("writes one JSONL line per simulated generation")
  void writesOneJsonlLinePerGeneration() throws IOException {
    Path outputPath = tempDir.resolve("results.jsonl");
    var command = new SimulateCommand();
    command.outputPath = outputPath;
    command.testMode = true;

    command.run();

    assertThat(outputPath).exists().isRegularFile();
    List<String> lines = Files.readAllLines(outputPath);
    assertThat(lines).isNotEmpty();

    var mapper = new ObjectMapper();
    assertThat(lines)
        .allSatisfy(
            line -> {
              OptimizationLog entry = mapper.readValue(line, OptimizationLog.class);
              assertThat(entry.config()).isNotNull();
              assertThat(entry.context()).isNotNull();
              assertThat(entry.statistics()).isNotNull();
            });
  }
}
