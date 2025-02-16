/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.util.FastMath;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bird in the simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class Bird implements Cloneable {

  /** The bird's position. */
  @Getter @Setter private Vector2D position;

  /** The bird's rotation in radians. */
  @Getter @Setter private float rotation;

  /** The bird's vision array. */
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private float[] vision;

  /** The bird's current speed. */
  @Getter @Setter private float speed;

  /** The bird's eye. */
  @Getter private final BirdEye eye;

  /** The bird's brain. */
  @Getter private final BirdBrain brain;

  /** The bird's satiation level. */
  @Getter private float satiation;

  /** The bird's configuration. */
  @Getter private final Config config;

  /** The bird's previous position. */
  @Getter private Vector2D previousPosition;

  /** Creates a new bird with the given parameters. */
  private Bird(
      Vector2D position,
      float rotation,
      float[] vision,
      float speed,
      BirdEye eye,
      BirdBrain brain,
      Config config) {
    this.position = position;
    this.previousPosition = position;
    this.rotation = rotation;
    this.vision = vision;
    this.speed = speed;
    this.eye = eye;
    this.brain = brain;
    this.satiation = 0.0f;
    this.config = config;
  }

  /**
   * Creates a new bird with the given brain and position.
   *
   * @param brain a {@link io.jshorelark.simulation.bird.BirdBrain} object
   * @param position a {@link io.jshorelark.simulation.physics.Vector2D} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @return a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public static Bird create(BirdBrain brain, Vector2D position, Config config) {
    return new Bird(
        position,
        0.0f,
        new float[config.getEyeCells()],
        config.getSimSpeedMax(),
        new BirdEye(config),
        brain,
        config);
  }

  /**
   * Creates a new random bird.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public static Bird random(Config config, Random random) {
    BirdBrain brain = BirdBrain.random(random, config);
    return new Bird(
        Vector2D.random(random),
        random.nextFloat() * (float) (2 * Math.PI),
        new float[config.getEyeCells()],
        config.getSimSpeedMax(),
        new BirdEye(config),
        brain,
        config);
  }

  /**
   * Creates a new bird from a chromosome.
   *
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @param random a {@link java.util.Random} object
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @return a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public static Bird fromChromosome(Config config, Random random, Chromosome chromosome) {
    BirdBrain brain = BirdBrain.fromChromosome(chromosome, config);
    return new Bird(
        Vector2D.random(random),
        random.nextFloat() * (float) (2 * Math.PI),
        new float[config.getEyeCells()],
        config.getSimSpeedMax(),
        new BirdEye(config),
        brain,
        config);
  }

  /**
   * Creates a new bird with the given chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @return a {@link io.jshorelark.simulation.bird.Bird} object
   */
  public Bird withChromosome(final Chromosome chromosome) {
    BirdBrain newBrain = BirdBrain.fromChromosome(chromosome, config);
    return new Bird(position, rotation, vision, speed, eye, newBrain, config);
  }

  /**
   * Converts this bird to a chromosome.
   *
   * @return a {@link io.jshorelark.genetic.Chromosome} object
   */
  public Chromosome toChromosome() {
    return brain.toChromosome();
  }

  /**
   * Gets the bird's vision of the given food items.
   *
   * @param foods a {@link java.util.List} object
   * @return an array of {@link float} objects
   */
  public float[] vision(List<Food> foods) {
    return eye.processVision(position, rotation, foods);
  }

  /**
   * Process the bird's brain based on visual input.
   *
   * @param foods a {@link java.util.List} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   */
  public void processBrain(List<Food> foods, Config config) {
    // Update vision
    vision = eye.processVision(position, rotation, foods);

    // Process brain inputs to get outputs
    float[] outputs = brain.processInputs(vision);

    // Update speed - clamp between min and max
    speed =
        Math.min(
            config.getSimSpeedMax(),
            Math.max(config.getSimSpeedMin(), speed + outputs[0] * config.getSimSpeedAccel()));

    // Update rotation - clamp change within acceleration limits
    float rotationChange = outputs[1] * config.getSimRotationAccel();
    rotation += rotationChange;

    // Normalize rotation to stay within [0, 2π]
    rotation = (float) ((rotation + 2 * Math.PI) % (2 * Math.PI));
  }

  /** Processes the bird's movement. */
  public void processMovement() {
    // Store previous position for trail drawing
    previousPosition = position;

    // Convert from model space angle (CCW from Y) to standard math angle (CCW from X)
    // by adding π/2 (90°) from our rotation
    float mathAngle = (float) Math.PI / 2 - rotation;

    // In Rust, 0 radians points right (positive X) and rotation is CCW
    // Calculate movement vector based on rotation
    float dx = (float) FastMath.cos(mathAngle) * speed;
    float dy = (float) FastMath.sin(mathAngle) * speed;
    Vector2D moveVector = new Vector2D(dx, dy);

    // Add the movement vector to current position
    position = position.add(moveVector);

    // Wrap position around world boundaries
    position = new Vector2D(wrap(position.x()), wrap(position.y()));
  }

  /**
   * Checks if this bird collides with the given food.
   *
   * @param food a {@link io.jshorelark.simulation.food.Food} object
   * @return a boolean
   */
  public boolean collidesWith(Food food) {
    return position.distance(food.getPosition()) <= 0.01f;
  }

  /** Increases the bird's satiation level. */
  public void eat() {
    satiation++;
  }

  /** Wraps a coordinate around world boundaries. */
  private float wrap(float value) {
    if (value < 0) {
      return 1 + (value % 1);
    } else if (value > 1) {
      return value % 1;
    }
    return value;
  }

  /**
   * Gets a copy of the bird's vision array.
   *
   * @return an array of {@link float} objects
   */
  public float[] getVision() {
    return vision.clone();
  }

  /**
   * Sets the bird's vision array.
   *
   * @param vision an array of {@link float} objects
   */
  public void setVision(float[] vision) {
    this.vision = vision.clone();
  }

  /** {@inheritDoc} */
  @Override
  public Bird clone() {
    try {
      Bird clone = (Bird) super.clone();
      clone.vision = vision.clone();
      clone.position = (Vector2D) position.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Bird should be cloneable", e);
    }
  }
}
