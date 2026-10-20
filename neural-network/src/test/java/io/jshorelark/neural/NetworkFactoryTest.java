/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.neural;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jshorelark.genetic.Chromosome;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NetworkFactory")
class NetworkFactoryTest {
  private static final int[] TOPOLOGY = {3, 4, 2};

  @Test
  @DisplayName("creates a random network matching the topology")
  void createsRandomNetwork() {
    var network = NetworkFactory.random(TOPOLOGY, new Random(42));

    assertThat(network.matchesTopology(TOPOLOGY)).isTrue();
    assertThat(network.weights()).hasSize(4 * (3 + 1) + 2 * (4 + 1));
  }

  @Test
  @DisplayName("round-trips a chromosome through the factory")
  void roundTripsChromosome() {
    var original = NetworkFactory.random(TOPOLOGY, new Random(42)).toChromosome();
    var restored = NetworkFactory.fromChromosome(original, TOPOLOGY);

    assertThat(restored.toChromosome()).isEqualTo(original);
  }

  @Test
  @DisplayName("builds a network from a chromosome through the port")
  void buildsFromChromosomeThroughPort() {
    var chromosome = Chromosome.of(new float[4 * 4 + 2 * 5]);
    var network = LayeredNetwork.fromChromosome(chromosome, TOPOLOGY);

    assertThat(network.matchesTopology(TOPOLOGY)).isTrue();
    assertThat(network.toChromosome()).isEqualTo(chromosome);
  }

  @Test
  @DisplayName("rejects a chromosome that does not fit the topology")
  void rejectsMismatchedChromosome() {
    var chromosome = Chromosome.of(new float[3]);

    assertThatThrownBy(() -> NetworkFactory.fromChromosome(chromosome, TOPOLOGY))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("rejects a single-layer topology")
  void rejectsSingleLayerTopology() {
    assertThatThrownBy(() -> NetworkFactory.random(new int[] {3}, new Random(42)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("reports a mismatched topology as false")
  void detectsMismatchedTopology() {
    var network = NeuralNetwork.random(new Random(42), new int[] {2, 1});

    assertThat(network.matchesTopology(new int[] {2, 1})).isTrue();
    assertThat(network.matchesTopology(new int[] {2, 3, 1})).isFalse();
    assertThat(network.matchesTopology(new int[] {5, 1})).isFalse();
    assertThat(network.matchesTopology(new int[] {2})).isFalse();
  }
}
