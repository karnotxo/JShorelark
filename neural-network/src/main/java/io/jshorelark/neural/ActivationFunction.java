/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.neural;

/**
 * Defines the activation function applied by every neuron. The Rust implementation only uses ReLU,
 * which is therefore the production default; the others exist for experimentation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@FunctionalInterface
public interface ActivationFunction {
  /** ReLU (Rectified Linear Unit) activation function. */
  ActivationFunction RELU = x -> Math.max(0.0f, x);

  /** Tanh activation function. */
  ActivationFunction TANH = x -> (float) Math.tanh(x);

  /**
   * Applies the activation function to an input value.
   *
   * @param x input value
   * @return activated value
   */
  float apply(float x);
}
