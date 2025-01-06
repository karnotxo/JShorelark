/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

import java.util.Random;

/**
 * A neuron in a neural network. Matches Rust's Neuron struct.
 *
 * <p>This class represents a single neuron in a neural network layer. Each neuron has a bias and
 * weights for each input. The neuron applies a ReLU activation function to its output.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class Neuron {
  /** The bias for this neuron. */
  private final float bias;

  /** The weights for each input. */
  private final float[] weights;

  /**
   * Creates a new neuron with the given bias and weights.
   *
   * @param bias the bias value
   * @param weights the weights for each input
   * @throws IllegalArgumentException if weights is null or empty
   */
  private Neuron(float bias, float[] weights) {
    if (weights == null || weights.length == 0) {
      throw new IllegalArgumentException("Weights must not be null or empty");
    }
    this.bias = bias;
    this.weights = weights.clone(); // Defensive copy
  }

  /**
   * Creates a new neuron with random weights and bias.
   *
   * @param random the random number generator to use
   * @param inputSize the number of inputs for this neuron
   * @return a new neuron with random weights and bias
   */
  public static Neuron random(Random random, int inputSize) {
    float bias = -1.0f + (2.0f * random.nextFloat()); // Range [-1.0, 1.0]
    float[] weights = new float[inputSize];
    for (int i = 0; i < inputSize; i++) {
      weights[i] = -1.0f + (2.0f * random.nextFloat()); // Range [-1.0, 1.0]
    }
    return new Neuron(bias, weights);
  }

  /**
   * Creates a new neuron with the given bias and weights.
   *
   * @param bias the bias value
   * @param weights the weights for each input
   * @return a new Neuron instance
   * @throws java.lang.IllegalArgumentException if weights is null or empty
   */
  public static Neuron create(float bias, float[] weights) {
    return new Neuron(bias, weights);
  }

  /**
   * Propagates inputs through this neuron.
   *
   * @param inputs the inputs to propagate
   * @return the output value after applying the activation function
   * @throws java.lang.IllegalArgumentException if inputs length doesn't match weights length
   */
  public float propagate(float[] inputs) {
    if (inputs.length != weights.length) {
      throw new IllegalArgumentException(
          String.format(
              "Expected %d inputs for neuron with %d weights, but got %d",
              weights.length, weights.length, inputs.length));
    }

    float sum = bias;
    for (int i = 0; i < inputs.length; i++) {
      sum += inputs[i] * weights[i];
    }

    return Math.max(0.0f, sum); // ReLU activation
  }

  /**
   * Gets the bias value.
   *
   * @return a float
   */
  public float getBias() {
    return bias;
  }

  /**
   * Gets a copy of the weights array.
   *
   * @return an array of {@link float} objects
   */
  public float[] getWeights() {
    return weights.clone(); // Defensive copy
  }
}
