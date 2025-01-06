/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.optimizer;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jshorelark.neural.NeuralNetwork;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NeuralNetworkIndividual")
class NeuralNetworkIndividualTest {
  private static final int[] TOPOLOGY = {3, 2, 1}; // Match Rust's test topology
  private Random random;
  private NeuralNetwork network;

  @BeforeEach
  void setUp() {
    // Match Rust's ChaCha8Rng deterministic behavior
    random = new Random(0x0000000000000000L);
    network = NeuralNetwork.random(random, TOPOLOGY);
  }

  @Test
  @DisplayName("creates individual with random weights")
  void createsIndividualWithRandomWeights() {
    NeuralNetworkIndividual individual = new NeuralNetworkIndividual(network);

    assertThat(individual.getNetwork()).isSameAs(network);
    assertThat(individual.getFitness()).isZero();

    // Verify weights match expected pattern (from Rust test)
    float[] weights = individual.getChromosome().getGenes();
    assertThat(weights).hasSize(11); // 3*2 + 2 (biases) + 2*1 + 1 (bias)
  }

  @Test
  @DisplayName("creates individual from chromosome")
  void createsIndividualFromChromosome() {
    // Use exact weights from Rust test
    float[] weights = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f};
    network = NeuralNetwork.fromWeights(new int[] {3, 2}, weights); // Match Rust test topology

    NeuralNetworkIndividual individual = new NeuralNetworkIndividual(network);

    assertThat(individual.getChromosome().getGenes()).containsExactly(weights);
    assertThat(individual.getFitness()).isZero();
  }

  @Test
  @DisplayName("returns fitness value")
  void returnsFitness() {
    NeuralNetworkIndividual individual = new NeuralNetworkIndividual(network);
    individual.fitness = 1.0f;

    assertThat(individual.getFitness()).isEqualTo(1.0f);
  }
}
