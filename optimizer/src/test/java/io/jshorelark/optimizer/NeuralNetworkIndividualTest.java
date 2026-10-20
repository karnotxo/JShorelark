/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.optimizer;

import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jshorelark.neural.NeuralNetwork;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NeuralNetworkIndividual")
class NeuralNetworkIndividualTest {

  private static final int[] TOPOLOGY = {3, 2, 1};

  @Test
  @DisplayName("starts with zero fitness")
  void startsWithZeroFitness() {
    var network = NeuralNetwork.random(new Random(0L), TOPOLOGY);

    var individual = new NeuralNetworkIndividual(network);

    assertThat(individual.getNetwork()).isSameAs(network);
    assertThat(individual.getFitness()).isZero();
  }

  @Test
  @DisplayName("exposes the network weights as its chromosome")
  void exposesWeightsAsChromosome() {
    // Layer 1: (3+1)*2 = 8 weights; layer 2: (2+1)*1 = 3 weights.
    float[] weights = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f};
    var network = NeuralNetwork.fromWeights(TOPOLOGY, weights);

    var individual = new NeuralNetworkIndividual(network);

    assertThat(individual.getChromosome().getGenes()).containsExactly(weights);
  }
}
