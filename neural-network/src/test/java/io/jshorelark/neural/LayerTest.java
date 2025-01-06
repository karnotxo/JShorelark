/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.neural;

import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Layer")
class LayerTest {
  private static final Random RANDOM = new Random(42);
  private static final float EPSILON = 0.0001f;

  @Test
  @DisplayName("creates random layer")
  void createsRandomLayer() {
    var layer = Layer.random(RANDOM, 3, 2);

    // Verify layer structure
    assertThat(layer.getNeurons()).hasSize(2);
    assertThat(layer.getNeurons().get(0).getWeights()).hasSize(3);
    assertThat(layer.getNeurons().get(1).getWeights()).hasSize(3);

    // Verify deterministic output with seed
    var neuron0 = layer.getNeurons().get(0);
    var neuron1 = layer.getNeurons().get(1);

    // Values from Java's Random with seed 42
    assertThat(neuron0.getBias()).isCloseTo(0.45512736f, within(EPSILON));
    assertThat(neuron0.getWeights()).containsExactly(-0.8906696f, 0.36644685f, -0.9041214f);

    assertThat(neuron1.getBias()).isCloseTo(-0.3825612f, within(EPSILON));
    assertThat(neuron1.getWeights()).containsExactly(0.88414705f, -0.4458431f, 0.41542113f);
  }

  @Test
  @DisplayName("creates layer from weights")
  void createsLayerFromWeights() {
    float[] weights = {
      0.1f, 0.2f, 0.3f, 0.4f, // First neuron: bias=0.1, weights=[0.2, 0.3, 0.4]
      0.5f, 0.6f, 0.7f, 0.8f // Second neuron: bias=0.5, weights=[0.6, 0.7, 0.8]
    };
    var layer = Layer.fromWeights(3, 2, weights);

    // Verify layer structure
    assertThat(layer.getNeurons()).hasSize(2);

    // Verify first neuron
    var neuron0 = layer.getNeurons().get(0);
    assertThat(neuron0.getBias()).isEqualTo(0.1f);
    assertThat(neuron0.getWeights()).containsExactly(0.2f, 0.3f, 0.4f);

    // Verify second neuron
    var neuron1 = layer.getNeurons().get(1);
    assertThat(neuron1.getBias()).isEqualTo(0.5f);
    assertThat(neuron1.getWeights()).containsExactly(0.6f, 0.7f, 0.8f);
  }

  @Test
  @DisplayName("validates fromWeights parameters")
  void validatesFromWeightsParameters() {
    // Test not enough weights
    assertThrows(IllegalArgumentException.class, () -> Layer.fromWeights(3, 2, new float[7]));

    // Test too many weights
    assertThrows(IllegalArgumentException.class, () -> Layer.fromWeights(3, 2, new float[9]));
  }

  @Test
  @DisplayName("propagates input correctly")
  void propagatesInputCorrectly() {
    // Create a layer with known weights
    float[] weights = {
      0.0f, 1.0f, -1.0f, // First neuron: bias=0, weights=[1, -1]
      0.0f, -1.0f, 1.0f // Second neuron: bias=0, weights=[-1, 1]
    };
    var layer = Layer.fromWeights(2, 2, weights);

    // Test propagation
    var inputs = new float[] {1.0f, 0.0f};
    var outputs = layer.propagate(inputs);

    assertThat(outputs).hasSize(2);
    assertThat(outputs[0]).isEqualTo(1.0f); // ReLU(1.0 * 1.0 + -1.0 * 0.0 + 0.0) = 1.0
    assertThat(outputs[1]).isEqualTo(0.0f); // ReLU(-1.0 * 1.0 + 1.0 * 0.0 + 0.0) = 0.0
  }

  @Test
  @DisplayName("protects internal state")
  void protectsInternalState() {
    // Create test layer
    float[] weights = {0.5f, 1.0f, 2.0f, 0.6f, 3.0f, 4.0f};
    var layer = Layer.fromWeights(2, 2, weights);

    // Try to modify returned neurons list
    var neurons = layer.getNeurons();
    assertThrows(UnsupportedOperationException.class, () -> neurons.clear());

    // Verify weights arrays are copied
    var neuron = layer.getNeurons().get(0);
    var neuronWeights = neuron.getWeights();
    neuronWeights[0] = 999.0f;

    // Original weights should be unchanged
    assertThat(layer.getNeurons().get(0).getWeights()).containsExactly(1.0f, 2.0f);
  }
}
