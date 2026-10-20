/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.evolution;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.Simulation;
import io.jshorelark.simulation.Statistics;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.food.Food;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Adapts the pure {@link Simulation} to the needs of the JavaFX view.
 *
 * <p>The generation clock lives in the domain: this class only steps the simulation and converts
 * the domain {@link Statistics} into the UI value type. It used to derive "generation finished"
 * from a bird's satiation, which made the UI disagree with the reference implementation; that rule
 * now has exactly one owner, {@code Simulation#step}.
 */
public final class EvolutionManager {

  /** The simulation being managed. */
  private final Simulation simulation;

  /** Observer for food consumption; never {@code null}. */
  private Consumer<CollisionEvent> collisionHandler = event -> {};

  /** Statistics of the last completed generation, or {@code null} before the first one. */
  private EvolutionStatistics statistics;

  /**
   * Creates a manager with the reference configuration.
   *
   * @param random the random source
   */
  public EvolutionManager(Random random) {
    this(random, Config.defaults());
  }

  /**
   * Creates a manager with the given configuration.
   *
   * @param random the random source
   * @param config the simulation configuration
   */
  public EvolutionManager(Random random, Config config) {
    Objects.requireNonNull(random, "random");
    this.simulation = new Simulation(config, random, this::forwardCollision);
  }

  /**
   * Registers the observer notified on every food consumption.
   *
   * <p>The callback runs on the thread that steps the simulation; the UI marshals it to the JavaFX
   * application thread.
   *
   * @param handler the observer, or {@code null} to ignore events
   */
  public void setCollisionHandler(Consumer<CollisionEvent> handler) {
    this.collisionHandler = handler == null ? event -> {} : handler;
  }

  /**
   * Advances the simulation by one step.
   *
   * @param random the random source used to respawn consumed food
   * @return the statistics of the generation that just ended, or {@code null} if it is still
   *     running
   */
  public EvolutionStatistics update(Random random) {
    Statistics completed = simulation.step(random);
    if (completed == null) {
      return null;
    }
    statistics = EvolutionStatistics.from(completed);
    return statistics;
  }

  /**
   * Returns the statistics of the last completed generation.
   *
   * @return the statistics, or {@code null} before the first generation ends
   */
  public EvolutionStatistics getStatistics() {
    return statistics;
  }

  /**
   * Steps the simulation until a generation completes.
   *
   * <p>Mirrors Rust's {@code Simulation::train}, so the terminal's {@code train} command reports
   * exactly one finished generation instead of a half-stepped one.
   *
   * @param random the random source
   * @return the statistics of the generation that just ended
   */
  public EvolutionStatistics train(Random random) {
    statistics = EvolutionStatistics.from(simulation.train(random));
    return statistics;
  }

  /**
   * Returns the completed generation count.
   *
   * @return the generation number
   */
  public int getGeneration() {
    return simulation.getGeneration();
  }

  /**
   * Returns the progress of the current generation, in {@code [0, 1]}.
   *
   * @return the fraction of the generation clock already elapsed
   */
  public double getGenerationProgress() {
    return Math.min(
        (double) simulation.getAge() / simulation.getConfig().simGenerationLength(), 1.0);
  }

  /**
   * Returns the foods in the simulation.
   *
   * @return an unmodifiable view of the food list
   */
  public List<Food> getFoods() {
    return simulation.getFoods();
  }

  /**
   * Returns the birds in the simulation.
   *
   * @return an unmodifiable view of the population
   */
  public List<Bird> getBirds() {
    return simulation.getBirds();
  }

  /**
   * Adds food items at random positions.
   *
   * @param count how many items to add
   * @param random the random source
   */
  public void addFoods(int count, Random random) {
    simulation.addFoods(count, random);
  }

  /**
   * Returns the simulation configuration.
   *
   * @return the immutable configuration
   */
  public Config getConfig() {
    return simulation.getConfig();
  }

  /**
   * Returns the food collision radius.
   *
   * @return the food size in world units
   */
  public float getFoodSize() {
    return simulation.getConfig().foodSize();
  }

  /**
   * Returns the bird drawing size.
   *
   * @return the bird size in world units
   */
  public double getBirdSize() {
    return simulation.getConfig().birdSize();
  }

  /**
   * Returns the total field of view.
   *
   * @return the field of view in radians
   */
  public double getEyeFovAngle() {
    return simulation.getConfig().eyeFovAngle();
  }

  /**
   * Returns the number of vision cells.
   *
   * @return the cell count
   */
  public int getEyeCells() {
    return simulation.getConfig().eyeCells();
  }

  /**
   * Returns the maximum vision distance.
   *
   * @return the field of view range in world units
   */
  public double getEyeFovRange() {
    return simulation.getConfig().eyeFovRange();
  }

  private void forwardCollision(CollisionEvent event) {
    collisionHandler.accept(event);
  }
}
