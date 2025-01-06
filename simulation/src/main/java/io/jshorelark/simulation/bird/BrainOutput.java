/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import io.soabase.recordbuilder.core.RecordBuilder;

/** Represents the output of a bird's brain. */
@RecordBuilder
record BrainOutput(float speed, float rotation) implements BrainOutputBuilder.With {}
