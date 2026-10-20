/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.jshorelark.simulation.Config;

/**
 * One grid-search configuration, as persisted in the optimizer's JSONL log.
 *
 * <p>Mirrors Rust's {@code OptConfig}: only the six parameters the search varies are stored. The JSON
 * property names are single letters and are a persisted contract, so they are annotated explicitly
 * rather than derived from the component names.
 *
 * @param brainNeurons number of hidden neurons in each bird's brain
 * @param eyeFovRange maximum vision distance, in world units
 * @param eyeFovAngle total field of view, in radians
 * @param eyeCells number of vision bins
 * @param gaMutChance per-gene probability of mutation
 * @param gaMutCoeff mutation magnitude coefficient
 */
public record OptimizationConfig(
    @JsonProperty("c") int brainNeurons,
    @JsonProperty("d") float eyeFovRange,
    @JsonProperty("e") float eyeFovAngle,
    @JsonProperty("f") int eyeCells,
    @JsonProperty("g") float gaMutChance,
    @JsonProperty("h") float gaMutCoeff) {

  /**
   * Derives the full simulation configuration.
   *
   * <p>Starts from {@link Config#defaults()} so every parameter the search does not vary keeps the
   * reference value; building a {@code Config} from only the six searched parameters would leave
   * required fields at zero and fail validation.
   *
   * @return the simulation configuration for this grid point
   */
  public Config toSimulationConfig() {
    return Config.defaults().toBuilder()
        .brainNeurons(brainNeurons)
        .eyeFovRange(eyeFovRange)
        .eyeFovAngle(eyeFovAngle)
        .eyeCells(eyeCells)
        .gaMutChance(gaMutChance)
        .gaMutCoeff(gaMutCoeff)
        .build();
  }
}
