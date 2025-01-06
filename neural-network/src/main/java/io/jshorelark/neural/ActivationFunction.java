/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

/**
 * Defines the ReLU activation function used in the neural network. This matches the Rust
 * implementation which only uses ReLU.
 *
 * @author Jose
 * @version $Id: $Id
 */
@FunctionalInterface
public interface ActivationFunction {
  /**
   * Applies the ReLU activation function to an input value.
   *
   * @param x input value
   * @return max(0, x)
   */
  float apply(float x);

  /** ReLU (Rectified Linear Unit) activation function. */
  ActivationFunction RELU = x -> Math.max(0.0f, x);

  /** Tanh activation function. */
  ActivationFunction TANH = x -> (float) Math.tanh(x);

  /**
   * Applies the activation function to the input.
   *
   * @param x input value
   * @return activated value
   */
}
