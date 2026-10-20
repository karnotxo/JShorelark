/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.neural;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ActivationFunction")
class ActivationFunctionTest {

  @Test
  @DisplayName("returns zero for negative input")
  void returnsZeroForNegativeInput() {
    assertEquals(0.0f, ActivationFunction.RELU.apply(-1.0f));
    assertEquals(0.0f, ActivationFunction.RELU.apply(-0.5f));
  }

  @Test
  @DisplayName("returns input for positive input")
  void returnsInputForPositiveInput() {
    assertEquals(1.0f, ActivationFunction.RELU.apply(1.0f));
    assertEquals(0.5f, ActivationFunction.RELU.apply(0.5f));
  }

  @Test
  @DisplayName("returns zero for zero input")
  void returnsZeroForZeroInput() {
    assertEquals(0.0f, ActivationFunction.RELU.apply(0.0f));
  }
}
