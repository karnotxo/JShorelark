/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer.simulate;

import io.jshorelark.optimizer.OptimizationConfig;

import lombok.AllArgsConstructor;
import lombok.Value;

/** A configuration and its iteration number. */
@Value
@AllArgsConstructor
class ConfigurationIteration {
  /** The configuration. */
  OptimizationConfig config;

  /** The iteration number. */
  int iteration;
}
