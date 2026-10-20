/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One JSONL line of an optimization run: the configuration, where it sits in the run, and how the
 * generation performed.
 *
 * <p>Mirrors Rust's {@code OptLog}. Property names are part of the persisted format; {@code
 * analyze} reads files produced by either implementation.
 *
 * @param config the grid point that was simulated
 * @param context the generation and iteration indices
 * @param statistics the fitness summary of that generation
 */
public record OptimizationLog(
    @JsonProperty("cfg") OptimizationConfig config,
    @JsonProperty("ctxt") OptimizationContext context,
    @JsonProperty("stats") OptimizationStatistics statistics) {}
