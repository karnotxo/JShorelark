/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.genetic.Individual;
import io.jshorelark.neural.LayeredNetwork;

import lombok.Getter;

/**
 * Individual that wraps a neural network.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Getter
public class NeuralNetworkIndividual implements Individual {
  /** The neural network. */
  private final LayeredNetwork network;

  /** The fitness value. */
  float fitness;

  /**
   * Creates a new individual with the given network.
   *
   * @param network a {@link io.jshorelark.neural.LayeredNetwork} object
   */
  public NeuralNetworkIndividual(LayeredNetwork network) {
    this.network = network;
    this.fitness = 0.0f;
  }

  /** {@inheritDoc} */
  @Override
  public Chromosome getChromosome() {
    return network.toChromosome();
  }

  /** {@inheritDoc} */
  @Override
  public float getFitness() {
    return fitness;
  }

  /**
   * Creates a new individual from a chromosome.
   *
   * @param chromosome a {@link io.jshorelark.genetic.Chromosome} object
   * @param topology an array of {@link int} objects
   * @return a {@link io.jshorelark.genetic.Individual} object
   */
  public static Individual create(Chromosome chromosome, int[] topology) {
    return new NeuralNetworkIndividual(LayeredNetwork.fromChromosome(chromosome, topology));
  }
}
