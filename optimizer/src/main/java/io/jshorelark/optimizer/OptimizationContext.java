/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

/**
 * Context for an optimization run. This class is equivalent to the OptContext struct in the Rust
 * implementation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
public class OptimizationContext {
  /** Current generation number. */
  int generation;

  /** Current iteration number. */
  int iteration;

  @JsonCreator
  /**
   * Constructor for OptimizationContext.
   *
   * @param generation a int
   * @param iteration a int
   */
  public OptimizationContext(
      @JsonProperty("generation") int generation, @JsonProperty("iteration") int iteration) {
    this.generation = generation;
    this.iteration = iteration;
  }
}
