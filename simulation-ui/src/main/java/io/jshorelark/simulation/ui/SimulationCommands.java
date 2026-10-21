/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 * The slice of {@link SimulationApplication} that {@link SimulationControls} needs.
 *
 * <p>The controls panel is a view: it reads state to label itself and calls a handful of commands.
 * Depending on the whole application would expose menus, the terminal, the scene and the renderer
 * to it, and a wrapper that forwards fourteen methods would only hide that dependency without
 * reducing it. This interface names the actual contract, so the panel can be driven by anything
 * that implements it — including a test double.
 */
public interface SimulationCommands {

  /**
   * Returns whether the simulation is currently paused.
   *
   * @return {@code true} when paused
   */
  boolean isPaused();

  /**
   * Pauses or resumes the simulation.
   *
   * @param paused {@code true} to pause
   */
  void setPaused(boolean paused);

  /**
   * Returns the observable pause state, so a button can follow it instead of polling the value.
   *
   * @return the read-only pause property
   */
  ReadOnlyBooleanProperty pausedProperty();

  /** Resets the simulation to the reference configuration. */
  void resetSimulation();

  /**
   * Adds food items at random positions.
   *
   * @param count how many items to add
   */
  void addFood(int count);

  /**
   * Sets the simulation speed multiplier.
   *
   * @param speed the multiplier, where 1.0 is real time
   */
  void setSimulationSpeed(double speed);

  /**
   * Returns the number of completed generations.
   *
   * @return the generation count
   */
  int getGeneration();

  /**
   * Returns how many birds are alive.
   *
   * @return the population size
   */
  int getPopulationSize();

  /**
   * Returns the best fitness of the last finished generation.
   *
   * @return the best fitness, or zero before the first generation ends
   */
  double getBestFitness();

  /**
   * Returns how far the current generation has progressed.
   *
   * @return a fraction in {@code [0, 1]}
   */
  double getGenerationProgress();

  /** Toggles sound on and off. */
  void toggleSound();

  /**
   * Returns whether sound is enabled.
   *
   * @return {@code true} when sound is on
   */
  boolean isSoundEnabled();

  /**
   * Sets the sound volume.
   *
   * @param volume a level in {@code [0, 1]}
   */
  void setVolume(float volume);

  /**
   * Returns the current sound volume.
   *
   * @return a level in {@code [0, 1]}
   */
  float getVolume();
}
