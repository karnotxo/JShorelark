/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.Random;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.neural.LayeredNetwork;
import io.jshorelark.neural.NeuralNetwork;
import io.jshorelark.simulation.Config;

/**
 * The brain of a bird, controlling its movement based on visual input.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class BirdBrain {
  private final LayeredNetwork network;
  private final Config config;

  /** Creates a new bird brain with the given network and config. */
  private BirdBrain(LayeredNetwork network, Config config) {
    this.network = network;
    this.config = config;
  }

  /**
   * Creates a new random bird brain.
   *
   * @param random a {@link java.util.Random} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @return a {@link io.jshorelark.simulation.bird.BirdBrain} object
   */
  public static BirdBrain random(Random random, Config config) {
    int[] topology = {config.getEyeCells(), config.getBrainNeurons(), 2};
    return new BirdBrain(NeuralNetwork.random(random, topology), config);
  }

  /**
   * Creates a new bird brain from a chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @return a {@link io.jshorelark.simulation.bird.BirdBrain} object
   */
  public static BirdBrain fromChromosome(Chromosome chromosome, Config config) {
    int[] topology = {config.getEyeCells(), config.getBrainNeurons(), 2};
    return new BirdBrain(NeuralNetwork.fromWeights(topology, chromosome.getGenes()), config);
  }

  /**
   * Creates a new bird brain from a neural network.
   *
   * @param network a {@link io.jshorelark.neural.LayeredNetwork} object
   * @param config a {@link io.jshorelark.simulation.Config} object
   * @param topology an array of {@link int} objects
   * @return a {@link io.jshorelark.simulation.bird.BirdBrain} object
   */
  public static BirdBrain fromNetwork(LayeredNetwork network, Config config, int[] topology) {
    if (!network.matchesTopology(topology)) {
      throw new IllegalArgumentException("Network topology does not match expected topology");
    }
    return new BirdBrain(network, config);
  }

  /**
   * Processes the visual input and returns speed and rotation adjustments.
   *
   * @param vision an array of {@link float} objects
   * @return an array of {@link float} objects
   */
  public float[] processInputs(float[] vision) {
    float[] response = network.propagate(vision);

    // Clamp responses between 0 and 1, then shift to -0.5 to 0.5 range
    float r0 = Math.min(1.0f, Math.max(0.0f, response[0])) - 0.5f;
    float r1 = Math.min(1.0f, Math.max(0.0f, response[1])) - 0.5f;

    // Calculate speed and rotation adjustments
    float speed =
        Math.min(config.getSimSpeedAccel(), Math.max(-config.getSimSpeedAccel(), r0 + r1));
    float rotation =
        Math.min(config.getSimRotationAccel(), Math.max(-config.getSimRotationAccel(), r0 - r1));

    return new float[] {speed, rotation};
  }

  /**
   * Gets the chromosome representation of this brain.
   *
   * @return a {@link io.jshorelark.genetic.Chromosome} object
   */
  public Chromosome toChromosome() {
    return Chromosome.of(network.weights());
  }
}
