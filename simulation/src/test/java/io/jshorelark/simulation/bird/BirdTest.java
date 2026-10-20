/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.bird;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.neural.LayeredNetwork;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Bird")
class BirdTest {

  private Config config;
  private Random random;

  @BeforeEach
  void setUp() {
    // Derived from the reference defaults so every field stays valid; only the values this test
    // asserts on are overridden.
    config =
        Config.defaults().toBuilder()
            .eyeCells(3)
            .brainNeurons(2)
            .simSpeedMax(1.0f)
            .simSpeedMin(0.0f)
            .simSpeedAccel(0.5f)
            .simRotationAccel(0.3f)
            .build();
    // Fixed seed so every run of the suite sees the same weights.
    random = new Random(42);
  }

  @Test
  @DisplayName("builds a random brain that matches the configured topology")
  void randomBrain() {
    final BirdBrain brain = BirdBrain.random(random, config);

    assertThat(brain.getNetwork().matchesTopology(config.brainTopology())).isTrue();
  }

  @Test
  @DisplayName("round-trips a chromosome through a brain")
  void brainFromChromosome() {
    // Topology [3, 2, 2]: layer 1 is (3 + 1) * 2 = 8 weights, layer 2 is (2 + 1) * 2 = 6.
    final float[] genes = {
      0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f
    };

    final BirdBrain brain = BirdBrain.fromChromosome(Chromosome.of(genes), config);

    assertThat(brain.toChromosome().getGenes()).containsExactly(genes);
  }

  @Test
  @DisplayName("maps raw outputs to independent speed and rotation deltas")
  void processInputsClampsBothControls() {
    final float[] vision = {0.5f, -0.6f, 0.7f};
    final LayeredNetwork network = mock(LayeredNetwork.class);
    when(network.matchesTopology(any(int[].class))).thenReturn(true);
    when(network.propagate(vision)).thenReturn(new float[] {0.8f, 0.3f});

    final float[] result = BirdBrain.fromNetwork(network, config).processInputs(vision);

    // Rust: clamp(0.8, 0, 2) - 1 = -0.2, then clamp by speedAccel 0.5 -> -0.2.
    //       clamp(0.3, 0, 2) - 1 = -0.7, then clamp by rotationAccel 0.3 -> -0.3.
    assertThat(result[0]).isCloseTo(-0.2f, within(1e-6f));
    assertThat(result[1]).isCloseTo(-0.3f, within(1e-6f));
  }

  @Test
  @DisplayName("rejects a network whose topology does not match the configuration")
  void fromNetworkRejectsWrongTopology() {
    final LayeredNetwork network = mock(LayeredNetwork.class);
    when(network.matchesTopology(any(int[].class))).thenReturn(false);

    assertThatThrownBy(() -> BirdBrain.fromNetwork(network, config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("topology");
  }

  @Test
  @DisplayName("moves along its heading after thinking")
  void movementChangesPosition() {
    final BirdBrain brain = BirdBrain.random(random, config);
    final Vector2D position = new Vector2D(0.5f, 0.5f);
    final Bird bird = Bird.create(brain, position, config);

    bird.processBrain(List.<Food>of());
    bird.processMovement();

    assertThat(bird.getPosition()).isNotEqualTo(position);
    assertThat(bird.getPreviousPosition()).isEqualTo(position);
  }

  @Test
  @DisplayName("refreshes its vision with one value per eye cell")
  void visionHasOneValuePerCell() {
    final BirdBrain brain = BirdBrain.random(random, config);
    final Bird bird = Bird.create(brain, new Vector2D(0.5f, 0.5f), config);
    final List<Food> foods = List.of(Food.at(new Vector2D(0.7f, 0.7f)));

    bird.processBrain(foods);

    assertThat(bird.getVision()).hasSize(config.eyeCells());
  }

  @Test
  @DisplayName("eating increases satiation, which is also the fitness")
  void eatingIncreasesSatiation() {
    final Bird bird =
        Bird.create(BirdBrain.random(random, config), new Vector2D(0.5f, 0.5f), config);

    bird.eat();
    bird.eat();

    assertThat(bird.getSatiation()).isEqualTo(2.0f);
  }
}
