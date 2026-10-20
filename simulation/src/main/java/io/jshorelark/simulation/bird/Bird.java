/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A single simulated bird: position, heading, speed, eye and brain.
 *
 * <p>Mirrors Rust's {@code Animal}. Rotation follows the Rust convention: {@code 0} faces +Y and
 * positive values turn counter-clockwise. Movement is {@code rotation * (0, speed)}, so the heading
 * is applied through the same axis the eye measures against.
 *
 * <p>This is deliberately a mutable class: it is the per-tick simulation state, not a value object.
 */
public final class Bird {

  private Vector2D position;
  private float rotation;
  private float[] vision;
  private float speed;

  private final BirdEye eye;
  private final BirdBrain brain;
  private final Config config;

  private float satiation;
  private Vector2D previousPosition;

  private Bird(
      Vector2D position, float rotation, float speed, BirdEye eye, BirdBrain brain, Config config) {
    this.position = Objects.requireNonNull(position, "position");
    this.previousPosition = position;
    this.rotation = rotation;
    this.vision = new float[config.eyeCells()];
    this.speed = speed;
    this.eye = Objects.requireNonNull(eye, "eye");
    this.brain = Objects.requireNonNull(brain, "brain");
    this.config = Objects.requireNonNull(config, "config");
    this.satiation = 0.0f;
  }

  /**
   * Creates a bird with a given brain at a given position.
   *
   * <p>Spawn speed is {@code simSpeedMax} and the heading is straight up, matching Rust's {@code
   * Animal::new} defaults for the parts it randomises separately.
   *
   * @param brain the bird's brain
   * @param position the starting position
   * @param config the simulation configuration
   * @return the new bird
   */
  public static Bird create(BirdBrain brain, Vector2D position, Config config) {
    return new Bird(position, 0.0f, config.simSpeedMax(), new BirdEye(config), brain, config);
  }

  /**
   * Creates a bird with a random brain, position and heading.
   *
   * @param config the simulation configuration
   * @param random the random source
   * @return the new bird
   */
  public static Bird random(Config config, Random random) {
    return new Bird(
        Vector2D.random(random),
        random.nextFloat() * (float) (2 * Math.PI),
        config.simSpeedMax(),
        new BirdEye(config),
        BirdBrain.random(random, config),
        config);
  }

  /**
   * Creates a bird whose brain is rebuilt from a chromosome.
   *
   * <p>Position and heading are random, matching Rust's {@code Animal::from_chromosome}.
   *
   * @param config the simulation configuration
   * @param random the random source for position and heading
   * @param chromosome the chromosome to decode into a brain
   * @return the new bird
   */
  public static Bird fromChromosome(Config config, Random random, Chromosome chromosome) {
    return new Bird(
        Vector2D.random(random),
        random.nextFloat() * (float) (2 * Math.PI),
        config.simSpeedMax(),
        new BirdEye(config),
        BirdBrain.fromChromosome(chromosome, config),
        config);
  }

  /**
   * Returns the chromosome that encodes this bird's brain.
   *
   * @return the weight vector of the brain
   */
  public Chromosome toChromosome() {
    return brain.toChromosome();
  }

  /**
   * Returns the field of view rendered from this position, without changing state.
   *
   * @param foods the food items to look at
   * @return one strength value per eye cell
   */
  public float[] vision(List<Food> foods) {
    return eye.processVision(position, rotation, foods);
  }

  /**
   * Refreshes the eye, asks the brain for controls and applies them.
   *
   * <p>Mirrors Rust's {@code Animal::process_brain}: vision first, then the two control values,
   * then the speed clamp and the rotation update.
   *
   * @param foods the food items currently in the world
   */
  public void processBrain(List<Food> foods) {
    vision = eye.processVision(position, rotation, foods);

    float[] controls = brain.processInputs(vision);
    speed = Math.clamp(speed + controls[0], config.simSpeedMin(), config.simSpeedMax());
    rotation = normalizeAngle(rotation + controls[1]);
  }

  /**
   * Moves the bird along its heading and wraps it into the world.
   *
   * <p>Mirrors Rust's {@code Animal::process_movement}, including the fact that the heading is the
   * direction of travel and the wrap keeps the bird inside the unit square.
   */
  public void processMovement() {
    previousPosition = position;
    // Exactly Rust's `self.position += self.rotation * Vector2::new(0.0, self.speed)`.
    Vector2D heading = new Vector2D(0.0f, speed).rotate(rotation);
    position = position.add(heading).wrap(0.0f, 1.0f);
  }

  /** Increases the satiation by one food item. */
  public void eat() {
    satiation++;
  }

  /**
   * Returns the bird's current position.
   *
   * @return the position in model space
   */
  public Vector2D getPosition() {
    return position;
  }

  /**
   * Returns the position at the previous step, used only to draw the trail.
   *
   * @return the previous position
   */
  public Vector2D getPreviousPosition() {
    return previousPosition;
  }

  /**
   * Returns the current heading.
   *
   * <p>Rust stores a {@code Rotation2} and reports {@code angle()}, so the value is always in
   * {@code [-π, π]} with {@code 0} facing +Y; this field follows the same convention.
   *
   * @return the rotation in radians, {@code [-π, π]}, {@code 0} facing +Y, counter-clockwise
   *     positive
   */
  public float getRotation() {
    return rotation;
  }

  /**
   * Returns the current speed.
   *
   * @return the speed in world units per step
   */
  public float getSpeed() {
    return speed;
  }

  /**
   * Returns a copy of the last computed vision vector.
   *
   * @return one strength value per eye cell
   */
  public float[] getVision() {
    return vision.clone();
  }

  /**
   * Returns how much food this bird has eaten in the current generation.
   *
   * @return the satiation, which is also the bird's fitness
   */
  public float getSatiation() {
    return satiation;
  }

  /**
   * Returns the configuration this bird was created with.
   *
   * @return the configuration
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Returns the bird's eye.
   *
   * @return the eye
   */
  public BirdEye getEye() {
    return eye;
  }

  /**
   * Returns the bird's brain.
   *
   * @return the brain
   */
  public BirdBrain getBrain() {
    return brain;
  }

  /**
   * Normalises an angle into {@code [0, 2π)}.
   *
   * <p>Rust stores the heading as a {@code Rotation2}, which is periodic by construction; Java
   * keeps a plain float, so the wrap has to happen explicitly, here and nowhere else. The target
   * range is {@code [-π, π]} because that is what {@code Rotation2::angle()} reports, and keeping
   * it makes the vision binning agree with the reference at the boundary.
   */
  private static float normalizeAngle(float angle) {
    float pi = (float) Math.PI;
    float twoPi = (float) (2 * Math.PI);
    float wrapped = angle;
    while (wrapped < -pi) {
      wrapped += twoPi;
    }
    while (wrapped > pi) {
      wrapped -= twoPi;
    }
    return wrapped;
  }
}
