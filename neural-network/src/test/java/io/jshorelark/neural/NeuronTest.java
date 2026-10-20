/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.neural;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Neuron")
class NeuronTest {

  @Test
  @DisplayName("applies ReLU to the weighted sum plus bias")
  void appliesRelu() {
    var neuron = Neuron.create(0.5f, new float[] {1.0f, 2.0f});

    assertThat(neuron.propagate(new float[] {1.0f, 1.0f})).isEqualTo(3.5f);
    assertThat(neuron.propagate(new float[] {-1.0f, -1.0f})).isEqualTo(0.0f);
  }

  @Test
  @DisplayName("creates weights and bias in [-1, 1]")
  void createsWeightsWithinBounds() {
    var neuron = Neuron.random(new Random(7), 16);

    assertThat(neuron.getBias()).isBetween(-1.0f, 1.0f);
    float[] weights = neuron.getWeights();
    assertThat(weights).hasSize(16);
    for (float weight : weights) {
      assertThat(weight).isBetween(-1.0f, 1.0f);
    }
  }

  @Test
  @DisplayName("copies the weights it is given and those it returns")
  void copiesWeights() {
    float[] source = {1.0f, 2.0f};
    var neuron = Neuron.create(0.0f, source);
    source[0] = 99.0f;

    float[] returned = neuron.getWeights();
    returned[1] = 99.0f;

    assertThat(neuron.getWeights()).containsExactly(1.0f, 2.0f);
  }

  @Test
  @DisplayName("rejects missing weights")
  void rejectsMissingWeights() {
    assertThatThrownBy(() -> Neuron.create(0.0f, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Neuron.create(0.0f, new float[0]))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("rejects an input vector of the wrong size")
  void rejectsWrongInputSize() {
    var neuron = Neuron.create(0.0f, new float[] {1.0f});

    assertThatThrownBy(() -> neuron.propagate(new float[] {1.0f, 2.0f}))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Expected 1 inputs");
  }

  @Test
  @DisplayName("is deterministic for a given seed")
  void isDeterministic() {
    var first = Neuron.random(new Random(42), 3);
    var second = Neuron.random(new Random(42), 3);

    assertThat(first.getBias()).isEqualTo(second.getBias());
    assertThat(first.getWeights()).containsExactly(second.getWeights());
  }
}
