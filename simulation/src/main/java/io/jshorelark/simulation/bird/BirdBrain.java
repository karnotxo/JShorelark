/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.Objects;
import java.util.Random;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.neural.LayeredNetwork;
import io.jshorelark.neural.NeuralNetwork;
import io.jshorelark.simulation.Config;

/**
 * Turns vision into the two control values a bird uses: speed and rotation deltas.
 *
 * <p>Mirrors Rust's {@code Brain}. The network has topology {@code [eyeCells, brainNeurons, 2]};
 * this class owns the mapping from the two raw network outputs to bounded control values.
 */
public final class BirdBrain {

  private final LayeredNetwork network;
  private final float speedAccel;
  private final float rotationAccel;

  private BirdBrain(LayeredNetwork network, Config config) {
    this.network = Objects.requireNonNull(network, "network");
    this.speedAccel = config.getSimSpeedAccel();
    this.rotationAccel = config.getSimRotationAccel();
  }

  /**
   * Creates a brain with a random network.
   *
   * @param random the random source for the weights
   * @param config the simulation configuration
   * @return a randomly initialised brain
   */
  public static BirdBrain random(Random random, Config config) {
    return new BirdBrain(NeuralNetwork.random(random, config.getBrainTopology()), config);
  }

  /**
   * Creates a brain by decoding a chromosome into network weights.
   *
   * @param chromosome the chromosome holding the weights
   * @param config the simulation configuration
   * @return the decoded brain
   * @throws IllegalArgumentException if the chromosome does not match the configured topology
   */
  public static BirdBrain fromChromosome(Chromosome chromosome, Config config) {
    return new BirdBrain(
        NeuralNetwork.fromWeights(config.getBrainTopology(), chromosome.getGenes()), config);
  }

  /**
   * Creates a brain around an existing network.
   *
   * @param network the network to use
   * @param config the simulation configuration
   * @return the brain
   * @throws IllegalArgumentException if the network topology does not match the configuration
   */
  public static BirdBrain fromNetwork(LayeredNetwork network, Config config) {
    if (!network.matchesTopology(config.getBrainTopology())) {
      throw new IllegalArgumentException(
          "Network topology does not match the configured brain topology");
    }
    return new BirdBrain(network, config);
  }

  /**
   * Converts a vision vector into a speed delta and a rotation delta.
   *
   * <p>Mirrors Rust's {@code Brain::propagate} exactly. Output 0 is the speed control and output 1
   * is the rotation control; each is clamped to {@code [0, 2]} and shifted by {@code -1} to obtain
   * {@code [-1, 1]}, then clamped once more by its acceleration limit. The two controls are
   * independent: an earlier port added and subtracted them, which is not what the reference does and
   * changes the meaning of the network's outputs.
   *
   * @param vision one strength value per eye cell
   * @return a two-element array: {@code [speedDelta, rotationDelta]}
   */
  public float[] processInputs(float[] vision) {
    float[] response = network.propagate(vision);

    float speed = Math.clamp(response[0], 0.0f, 2.0f) - 1.0f;
    float rotation = Math.clamp(response[1], 0.0f, 2.0f) - 1.0f;

    speed = Math.clamp(speed, -speedAccel, speedAccel);
    rotation = Math.clamp(rotation, -rotationAccel, rotationAccel);
    return new float[] {speed, rotation};
  }

  /**
   * Returns the weight vector that encodes this brain.
   *
   * @return the chromosome
   */
  public Chromosome toChromosome() {
    return network.toChromosome();
  }

  /**
   * Returns the underlying network.
   *
   * @return the network
   */
  public LayeredNetwork getNetwork() {
    return network;
  }
}
