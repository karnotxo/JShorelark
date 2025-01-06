/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

import java.util.List;

import io.jshorelark.genetic.Chromosome;

/**
 * Interface for layered neural networks. Matches Rust's Network trait.
 *
 * <p>This interface defines the contract for neural networks with configurable topology. It
 * provides methods for propagating inputs through the network, accessing weights and biases, and
 * verifying network topology.
 *
 * <p>The implementation closely follows the Rust code's behavior: - propagate() takes ownership of
 * inputs and returns a new array - weights() returns a new array each time, similar to Rust's
 * iterator - toChromosome() collects weights into a new Chromosome
 *
 * @author Jose
 * @version $Id: $Id
 */
public interface LayeredNetwork {
  /**
   * Propagates inputs through the network and returns outputs. Takes ownership of the inputs array
   * and returns a new array.
   *
   * @param inputs the input values to propagate
   * @return a new array containing the output values
   * @throws java.lang.IllegalArgumentException if inputs don't match the network's input size
   */
  float[] propagate(float[] inputs);

  /**
   * Gets all weights and biases as a flat array. Returns a new array each time, similar to Rust's
   * iterator.
   *
   * @return a new array containing all weights and biases
   */
  float[] weights();

  /**
   * Gets the layers in this network. Returns an unmodifiable list to maintain immutability.
   *
   * @return an unmodifiable list of layers
   */
  List<Layer> getLayers();

  /**
   * Checks if this network matches the given topology.
   *
   * @param topology the topology to check against
   * @return true if the network matches the topology
   */
  boolean matchesTopology(int[] topology);

  /**
   * Gets the chromosome representation of this network. Collects weights into a new Chromosome,
   * matching Rust's collect() behavior.
   *
   * @return a new Chromosome containing the network's weights
   */
  default Chromosome toChromosome() {
    return Chromosome.of(weights());
  }

  /**
   * Creates a new network from a chromosome. Matches Rust's Network::from_weights behavior.
   *
   * @param chromosome the chromosome containing the weights
   * @param topology the network topology
   * @return a new LayeredNetwork instance
   * @throws java.lang.IllegalArgumentException if the chromosome doesn't match the topology
   */
  static LayeredNetwork fromChromosome(Chromosome chromosome, int[] topology) {
    return NeuralNetwork.fromWeights(topology, chromosome.getGenes());
  }
}
