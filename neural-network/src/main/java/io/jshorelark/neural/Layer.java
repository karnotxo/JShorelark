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

/**
 * A layer in a neural network. Matches Rust's Layer struct.
 *
 * <p>This class represents a layer of neurons in a neural network. Each layer contains a list of
 * neurons and can propagate inputs through those neurons.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class Layer {
  /** The neurons in this layer. */
  private final List<Neuron> neurons;

  /**
   * Creates a new layer with the given neurons.
   *
   * @param neurons the neurons in this layer
   * @throws IllegalArgumentException if neurons is null or empty
   */
  private Layer(List<Neuron> neurons) {
    if (neurons == null || neurons.isEmpty()) {
      throw new IllegalArgumentException("Neurons must not be null or empty");
    }
    this.neurons = Collections.unmodifiableList(new ArrayList<>(neurons));
  }

  /**
   * Creates a new layer with random weights and biases.
   *
   * @param random the random number generator to use
   * @param inputSize the number of inputs for each neuron
   * @param outputSize the number of neurons in this layer
   * @return a new layer with random weights and biases
   */
  public static Layer random(Random random, int inputSize, int outputSize) {
    List<Neuron> neurons = new ArrayList<>(outputSize);
    for (int i = 0; i < outputSize; i++) {
      neurons.add(Neuron.random(random, inputSize));
    }
    return new Layer(neurons);
  }

  /**
   * Creates a new layer from weights. Matches Rust's Layer::from_weights.
   *
   * @param inputSize the number of inputs to this layer
   * @param outputSize the number of neurons in this layer
   * @param weights the weights and biases for this layer, in a flat array
   * @return a new Layer instance
   * @throws java.lang.IllegalArgumentException if weights array doesn't match the expected size
   */
  public static Layer fromWeights(int inputSize, int outputSize, float[] weights) {
    int weightsPerNeuron = inputSize + 1; // +1 for bias
    int expectedWeights = weightsPerNeuron * outputSize;
    if (weights.length != expectedWeights) {
      throw new IllegalArgumentException(
          String.format(
              "Expected %d weights for layer with %d inputs and %d outputs, but got %d",
              expectedWeights, inputSize, outputSize, weights.length));
    }

    List<Neuron> neurons = new ArrayList<>();
    int weightIndex = 0;
    for (int i = 0; i < outputSize; i++) {
      float bias = weights[weightIndex++];
      float[] neuronWeights = new float[inputSize];
      System.arraycopy(weights, weightIndex, neuronWeights, 0, inputSize);
      neurons.add(Neuron.create(bias, neuronWeights));
      weightIndex += inputSize;
    }

    return new Layer(neurons);
  }

  /**
   * Propagates inputs through this layer.
   *
   * @param inputs the inputs to propagate
   * @return the outputs from each neuron
   */
  public float[] propagate(float[] inputs) {
    float[] outputs = new float[neurons.size()];
    for (int i = 0; i < neurons.size(); i++) {
      outputs[i] = neurons.get(i).propagate(inputs);
    }
    return outputs;
  }

  /**
   * Gets the neurons in this layer.
   *
   * @return a {@link java.util.List} object
   */
  public List<Neuron> getNeurons() {
    return neurons;
  }
}
