/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Value;

/**
 * Log entry for optimization runs.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Value
@Builder
@JsonDeserialize(builder = OptimizationLog.OptimizationLogBuilder.class)
public class OptimizationLog {
  /** The configuration used. */
  @JsonProperty("cfg")
  OptimizationConfig config;

  /** The context of the run. */
  @JsonProperty("ctxt")
  OptimizationContext context;

  /** The statistics from the run. */
  @JsonProperty("stats")
  OptimizationStatistics statistics;

  @JsonPOJOBuilder(withPrefix = "")
  public static class OptimizationLogBuilder {
    // Builder will be generated by Lombok
  }
}
