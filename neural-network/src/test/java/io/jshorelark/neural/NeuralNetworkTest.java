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

@DisplayName("NeuralNetwork")
class NeuralNetworkTest {
  private static final Random RANDOM = new Random(42);
  private static final float EPSILON = 0.0001f;

  @Test
  @DisplayName("creates random network")
  void createsRandomNetwork() {
    var network = NeuralNetwork.random(RANDOM, new int[] {3, 2, 1});

    // Verify network structure
    assertThat(network.getLayers()).hasSize(2);
    assertThat(network.getLayers().get(0).getNeurons()).hasSize(2);
    assertThat(network.getLayers().get(1).getNeurons()).hasSize(1);

    // Verify deterministic output with seed
    var layer0 = network.getLayers().get(0);
    var neuron00 = layer0.getNeurons().get(0);
    var neuron01 = layer0.getNeurons().get(1);

    // First layer values from Java's Random with seed 42
    assertThat(neuron00.getBias()).isCloseTo(-0.9041214f, within(EPSILON));
    assertThat(neuron00.getWeights()).containsExactly(-0.3825612f, 0.88414705f, -0.4458431f);

    assertThat(neuron01.getBias()).isCloseTo(0.41542113f, within(EPSILON));
    assertThat(neuron01.getWeights()).containsExactly(0.33109784f, -0.81735086f, 0.80674446f);

    // Second layer values
    var layer1 = network.getLayers().get(1);
    var neuron10 = layer1.getNeurons().get(0);
    assertThat(neuron10.getBias()).isCloseTo(-0.09748566f, within(EPSILON));
    assertThat(neuron10.getWeights()).containsExactly(-0.26243424f, -0.23671389f);
  }

  @Test
  @DisplayName("creates network from weights")
  void createsNetworkFromWeights() {
    float[] weights = {
      // Layer 0 (2 neurons, 3 inputs each)
      0.1f, 0.2f, 0.3f, 0.4f, // First neuron: bias=0.1, weights=[0.2, 0.3, 0.4]
      0.5f, 0.6f, 0.7f, 0.8f, // Second neuron: bias=0.5, weights=[0.6, 0.7, 0.8]
      // Layer 1 (1 neuron, 2 inputs)
      0.9f, 1.0f, 1.1f // Third neuron: bias=0.9, weights=[1.0, 1.1]
    };
    var network = NeuralNetwork.fromWeights(new int[] {3, 2, 1}, weights);

    // Verify network structure
    assertThat(network.getLayers()).hasSize(2);
    assertThat(network.getLayers().get(0).getNeurons()).hasSize(2);
    assertThat(network.getLayers().get(1).getNeurons()).hasSize(1);

    // Verify first layer
    var layer0 = network.getLayers().get(0);
    var neuron00 = layer0.getNeurons().get(0);
    var neuron01 = layer0.getNeurons().get(1);

    assertThat(neuron00.getBias()).isEqualTo(0.1f);
    assertThat(neuron00.getWeights()).containsExactly(0.2f, 0.3f, 0.4f);
    assertThat(neuron01.getBias()).isEqualTo(0.5f);
    assertThat(neuron01.getWeights()).containsExactly(0.6f, 0.7f, 0.8f);

    // Verify second layer
    var layer1 = network.getLayers().get(1);
    var neuron10 = layer1.getNeurons().get(0);

    assertThat(neuron10.getBias()).isEqualTo(0.9f);
    assertThat(neuron10.getWeights()).containsExactly(1.0f, 1.1f);
  }

  @Test
  @DisplayName("validates fromWeights parameters")
  void validatesFromWeightsParameters() {
    // Test topology with less than 2 layers
    assertThrows(
        IllegalArgumentException.class,
        () -> NeuralNetwork.fromWeights(new int[] {1}, new float[0]));

    // Test not enough weights
    assertThrows(
        IllegalArgumentException.class,
        () -> NeuralNetwork.fromWeights(new int[] {3, 2, 1}, new float[10]));

    // Test too many weights
    assertThrows(
        IllegalArgumentException.class,
        () -> NeuralNetwork.fromWeights(new int[] {3, 2, 1}, new float[12]));
  }

  @Test
  @DisplayName("propagates input correctly")
  void propagatesInputCorrectly() {
    // Create a network with known weights
    float[] weights = {
      // Layer 0 (2 neurons, 2 inputs each)
      0.0f, 1.0f, -1.0f, // First neuron: bias=0, weights=[1, -1]
      0.0f, -1.0f, 1.0f, // Second neuron: bias=0, weights=[-1, 1]
      // Layer 1 (1 neuron, 2 inputs)
      0.0f, 1.0f, -1.0f // Third neuron: bias=0, weights=[1, -1]
    };
    var network = NeuralNetwork.fromWeights(new int[] {2, 2, 1}, weights);

    // Test propagation
    var inputs = new float[] {1.0f, 0.0f};
    var outputs = network.propagate(inputs);

    assertThat(outputs).hasSize(1);
    assertThat(outputs[0]).isEqualTo(1.0f);
  }

  @Test
  @DisplayName("protects internal state")
  void protectsInternalState() {
    // Create test network
    var network = NeuralNetwork.random(RANDOM, new int[] {2, 1});

    // Try to modify returned layers list
    var layers = network.getLayers();
    assertThrows(UnsupportedOperationException.class, () -> layers.clear());

    // Verify weights array is copied
    var weights = network.weights();
    weights[0] = 999.0f;

    // Original weights should be unchanged
    assertThat(network.weights()[0]).isNotEqualTo(999.0f);
  }
}
