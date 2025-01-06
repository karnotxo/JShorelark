/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.jshorelark.genetic.Chromosome;

/**
 * A neural network implementation with configurable topology. Matches Rust's Network struct.
 *
 * <p>This class provides a layered neural network implementation that closely follows the original
 * Rust implementation. It supports creating networks with random weights, from existing weights,
 * and propagating inputs through the network.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class NeuralNetwork implements LayeredNetwork {
  /** The layers in this network. */
  private final List<Layer> layers;

  /**
   * Creates a new neural network with the given layers.
   *
   * @param layers the layers to use in this network
   * @throws IllegalArgumentException if layers is null or empty
   */
  private NeuralNetwork(List<Layer> layers) {
    if (layers == null || layers.isEmpty()) {
      throw new IllegalArgumentException("Layers must not be null or empty");
    }
    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));
  }

  /**
   * Creates a new neural network with random weights and biases.
   *
   * @param random the random number generator to use
   * @param topology the topology of the network (number of neurons in each layer)
   * @return a new neural network with random weights and biases
   * @throws java.lang.IllegalArgumentException if topology has less than 2 layers
   */
  public static NeuralNetwork random(Random random, int[] topology) {
    if (topology.length < 2) {
      throw new IllegalArgumentException("Network must have at least 2 layers");
    }

    List<Layer> layers = new ArrayList<>();
    for (int i = 0; i < topology.length - 1; i++) {
      layers.add(Layer.random(random, topology[i], topology[i + 1]));
    }
    return new NeuralNetwork(layers);
  }

  /**
   * Creates a new neural network from the given weights.
   *
   * @param topology the topology of the network (input size, hidden sizes, output size)
   * @param weights the weights to use
   * @return a new NeuralNetwork instance
   * @throws java.lang.IllegalArgumentException if topology has less than 2 layers or weights don't
   *     match topology
   */
  public static NeuralNetwork fromWeights(int[] topology, float[] weights) {
    if (topology.length < 2) {
      throw new IllegalArgumentException("Topology must have at least 2 layers");
    }

    List<Layer> layers = new ArrayList<>();
    int weightIndex = 0;

    for (int i = 0; i < topology.length - 1; i++) {
      int inputSize = topology[i];
      int outputSize = topology[i + 1];
      int weightsNeeded = (inputSize + 1) * outputSize; // +1 for bias

      if (weightIndex + weightsNeeded > weights.length) {
        throw new IllegalArgumentException("Not enough weights for topology");
      }

      float[] layerWeights = new float[weightsNeeded];
      System.arraycopy(weights, weightIndex, layerWeights, 0, weightsNeeded);
      layers.add(Layer.fromWeights(inputSize, outputSize, layerWeights));
      weightIndex += weightsNeeded;
    }

    if (weightIndex < weights.length) {
      throw new IllegalArgumentException("Too many weights for topology");
    }

    return new NeuralNetwork(layers);
  }

  /**
   * Creates a new neural network from a chromosome.
   *
   * @param chromosome the chromosome containing the weights
   * @param topology the network topology
   * @return a new NeuralNetwork instance
   * @throws java.lang.IllegalArgumentException if the chromosome doesn't match the topology
   */
  public static NeuralNetwork fromChromosome(Chromosome chromosome, int[] topology) {
    return fromWeights(topology, chromosome.getGenes());
  }

  /** {@inheritDoc} */
  @Override
  public float[] propagate(float[] inputs) {
    float[] current = inputs.clone(); // Take ownership of inputs
    for (Layer layer : layers) {
      current = layer.propagate(current);
    }
    return current;
  }

  /** {@inheritDoc} */
  @Override
  public float[] weights() {
    int totalWeights = 0;
    for (Layer layer : layers) {
      for (Neuron neuron : layer.getNeurons()) {
        totalWeights += 1 + neuron.getWeights().length; // +1 for bias
      }
    }

    float[] weights = new float[totalWeights];
    int index = 0;

    for (Layer layer : layers) {
      for (Neuron neuron : layer.getNeurons()) {
        weights[index++] = neuron.getBias();
        float[] neuronWeights = neuron.getWeights();
        System.arraycopy(neuronWeights, 0, weights, index, neuronWeights.length);
        index += neuronWeights.length;
      }
    }

    return weights;
  }

  /** {@inheritDoc} */
  @Override
  public List<Layer> getLayers() {
    return layers;
  }

  /** {@inheritDoc} */
  @Override
  public boolean matchesTopology(int[] topology) {
    if (topology.length < 2) {
      return false;
    }

    if (layers.size() != topology.length - 1) {
      return false;
    }

    for (int i = 0; i < layers.size(); i++) {
      Layer layer = layers.get(i);
      if (layer.getNeurons().size() != topology[i + 1]) {
        return false;
      }
      for (Neuron neuron : layer.getNeurons()) {
        if (neuron.getWeights().length != topology[i]) {
          return false;
        }
      }
    }

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public Chromosome toChromosome() {
    return Chromosome.of(weights());
  }
}
