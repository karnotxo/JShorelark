/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

import java.util.Random;

import io.jshorelark.genetic.Chromosome;

/**
 * Factory for creating neural networks.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class NetworkFactory {
  private NetworkFactory() {
    // Utility class
  }

  /**
   * Creates a new random neural network with the given topology.
   *
   * @param topology an array of {@link int} objects
   * @param random a {@link java.util.Random} object
   * @return a {@link io.jshorelark.neural.LayeredNetwork} object
   */
  public static LayeredNetwork random(int[] topology, Random random) {
    return NeuralNetwork.random(random, topology);
  }

  /**
   * Creates a new neural network from a chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @param topology an array of {@link int} objects
   * @return a {@link io.jshorelark.neural.LayeredNetwork} object
   */
  public static LayeredNetwork fromChromosome(Chromosome chromosome, int[] topology) {
    return NeuralNetwork.fromWeights(topology, chromosome.getGenes());
  }
}
