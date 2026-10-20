/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer.simulate;

import io.jshorelark.optimizer.OptimizationConfig;

/**
 * A grid point together with its repetition index.
 *
 * @param config the grid point
 * @param iteration the repetition index, so repeated runs stay distinguishable in the log
 */
record ConfigurationIteration(OptimizationConfig config, int iteration) {}
