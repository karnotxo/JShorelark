/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import io.jshorelark.optimizer.analyze.AnalyzeCommand;
import io.jshorelark.optimizer.simulate.SimulateCommand;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main entry point for the optimizer.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
@Command(
    name = "optimizer",
    subcommands = {AnalyzeCommand.class, SimulateCommand.class},
    mixinStandardHelpOptions = true)
public class Main implements Runnable {
  /**
   * Main entry point.
   *
   * @param args an array of {@link java.lang.String} objects
   */
  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Default command when no subcommand is specified.
   */
  @Override
  public void run() {
    throw new CommandLine.ParameterException(new CommandLine(this), "Missing required subcommand");
  }
}
