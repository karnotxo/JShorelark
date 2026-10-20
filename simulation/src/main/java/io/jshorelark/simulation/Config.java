/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import lombok.Builder;
import lombok.With;

/**
 * Every tunable parameter of a simulation.
 *
 * <p>Mirrors Rust's {@code Config} field for field; the defaults are the reference values. All
 * spatial quantities are expressed in world units, where the world is the unit square.
 *
 * <p>Accessors deliberately follow record style ({@code brainNeurons()}), because this is a value
 * type, not a bean. {@code with…} methods exist so the UI can derive a variant configuration
 * without losing the validation performed by the canonical constructor.
 *
 * @param brainNeurons number of hidden neurons in each bird's brain
 * @param eyeFovRange maximum vision distance, in world units
 * @param eyeFovAngle total field of view, in radians
 * @param eyeCells number of bins the field of view is split into
 * @param foodSize collision radius of a food item, and its drawn size
 * @param birdSize drawn size of a bird (no collision role)
 * @param gaReverse {@code 1} selects the least fit birds instead of the fittest
 * @param gaMutChance per-gene probability of mutation
 * @param gaMutCoeff mutation magnitude coefficient
 * @param simSpeedMin lower bound of the per-step speed
 * @param simSpeedMax upper bound of the per-step speed, also each bird's spawn speed
 * @param simSpeedAccel maximum speed change produced by one brain response
 * @param simRotationAccel maximum rotation change produced by one brain response, in radians
 * @param simGenerationLength number of steps a generation lasts
 * @param worldAnimals number of birds in the world
 * @param worldFoods number of food items in the world
 */
@Builder(toBuilder = true)
@With
public record Config(
    int brainNeurons,
    float eyeFovRange,
    float eyeFovAngle,
    int eyeCells,
    float foodSize,
    float birdSize,
    int gaReverse,
    float gaMutChance,
    float gaMutCoeff,
    float simSpeedMin,
    float simSpeedMax,
    float simSpeedAccel,
    float simRotationAccel,
    int simGenerationLength,
    int worldAnimals,
    int worldFoods) {

  /**
   * Validates the configuration.
   *
   * <p>Rust asserts the eye invariants in {@code Eye::new_ex}; an invalid configuration must fail
   * here rather than silently produce a bird that cannot see.
   */
  public Config {
    if (brainNeurons <= 0) {
      throw new IllegalArgumentException("brainNeurons must be positive: " + brainNeurons);
    }
    if (eyeFovRange <= 0.0f) {
      throw new IllegalArgumentException("eyeFovRange must be positive: " + eyeFovRange);
    }
    if (eyeFovAngle <= 0.0f) {
      throw new IllegalArgumentException("eyeFovAngle must be positive: " + eyeFovAngle);
    }
    if (eyeCells <= 0) {
      throw new IllegalArgumentException("eyeCells must be positive: " + eyeCells);
    }
    if (foodSize <= 0.0f) {
      throw new IllegalArgumentException("foodSize must be positive: " + foodSize);
    }
    if (gaMutChance < 0.0f || gaMutChance > 1.0f) {
      throw new IllegalArgumentException("gaMutChance must be in [0, 1]: " + gaMutChance);
    }
    if (simSpeedMin < 0.0f || simSpeedMin > simSpeedMax) {
      throw new IllegalArgumentException("simSpeedMin must be in [0, simSpeedMax]: " + simSpeedMin);
    }
    if (simRotationAccel < 0.0f) {
      throw new IllegalArgumentException(
          "simRotationAccel must not be negative: " + simRotationAccel);
    }
    if (simGenerationLength <= 0) {
      throw new IllegalArgumentException(
          "simGenerationLength must be positive: " + simGenerationLength);
    }
    if (worldAnimals < 0 || worldFoods < 0) {
      throw new IllegalArgumentException(
          "worldAnimals and worldFoods must not be negative: " + worldAnimals + ", " + worldFoods);
    }
  }

  /**
   * Returns the reference configuration, matching Rust's {@code Config::default}.
   *
   * @return the default configuration
   */
  public static Config defaults() {
    return builder()
        .brainNeurons(9)
        .eyeFovRange(0.25f)
        .eyeFovAngle((float) (Math.PI + Math.PI / 4))
        .eyeCells(9)
        .foodSize(0.01f)
        .birdSize(0.01f)
        .gaReverse(0)
        .gaMutChance(0.01f)
        .gaMutCoeff(0.3f)
        .simSpeedMin(0.001f)
        .simSpeedMax(0.005f)
        .simSpeedAccel(0.2f)
        .simRotationAccel((float) (Math.PI / 2))
        .simGenerationLength(2500)
        .worldAnimals(40)
        .worldFoods(60)
        .build();
  }

  /**
   * Returns the network topology implied by this configuration.
   *
   * <p>A fresh array is returned on every call so callers cannot mutate shared state.
   *
   * @return {@code [eyeCells, brainNeurons, 2]}
   */
  public int[] brainTopology() {
    return new int[] {eyeCells, brainNeurons, 2};
  }
}
