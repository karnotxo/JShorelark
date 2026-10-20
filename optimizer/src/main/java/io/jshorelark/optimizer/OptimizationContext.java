/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Where a logged generation belongs in the grid search.
 *
 * <p>Mirrors Rust's {@code OptContext}, including its single-letter JSON property names.
 *
 * @param generation generation index inside the run, from {@code 0}
 * @param iteration repetition index of this configuration, from {@code 0}
 */
public record OptimizationContext(
    @JsonProperty("g") int generation, @JsonProperty("i") int iteration) {}
