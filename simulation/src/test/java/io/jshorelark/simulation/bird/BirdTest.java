/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.bird;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jshorelark.genetic.Chromosome;
import io.jshorelark.neural.LayeredNetwork;
import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for the Bird class. */
class BirdTest {
  private Config config;
  private Random random;
  private LayeredNetwork network;

  @BeforeEach
  void setUp() {
    config =
        Config.builder()
            .eyeCells(3)
            .brainNeurons(2)
            .simSpeedMax(1.0f)
            .simSpeedMin(0.0f)
            .simSpeedAccel(0.5f)
            .simRotationAccel(0.3f)
            .build();

    random = new Random(42); // Fixed seed for reproducible tests
    network = mock(LayeredNetwork.class);
  }

  @Test
  void testRandomBirdBrain() {
    // Given
    int[] expectedTopology = {config.getEyeCells(), config.getBrainNeurons(), 2};
    when(network.matchesTopology(expectedTopology)).thenReturn(true);

    // When
    BirdBrain brain = BirdBrain.random(random, config);

    // Then
    assertThat(brain).isNotNull();
  }

  @Test
  void testBirdBrainFromChromosome() {
    // Given
    // For topology [3, 2, 2], we need:
    // Layer 1: 3 inputs -> 2 neurons = (3+1)*2 = 8 weights (including biases)
    // Layer 2: 2 inputs -> 2 neurons = (2+1)*2 = 6 weights (including biases)
    // Total: 14 weights
    float[] genes = {
      // Layer 1 (3 inputs -> 2 neurons)
      0.1f,
      0.2f,
      0.3f,
      0.4f, // First neuron (bias + 3 weights)
      0.5f,
      0.6f,
      0.7f,
      0.8f, // Second neuron (bias + 3 weights)
      // Layer 2 (2 inputs -> 2 neurons)
      0.9f,
      1.0f,
      1.1f, // First neuron (bias + 2 weights)
      1.2f,
      1.3f,
      1.4f // Second neuron (bias + 2 weights)
    };
    Chromosome chromosome = Chromosome.of(genes);
    int[] expectedTopology = {config.getEyeCells(), config.getBrainNeurons(), 2};
    when(network.matchesTopology(expectedTopology)).thenReturn(true);

    // When
    BirdBrain brain = BirdBrain.fromChromosome(chromosome, config);

    // Then
    assertThat(brain).isNotNull();
    assertThat(brain.toChromosome().getGenes()).containsExactly(genes);
  }

  @Test
  void testProcessInputs() {
    // Given
    float[] vision = {0.5f, -0.6f, 0.7f};
    float[] networkOutput = {0.8f, 0.3f}; // Will be clamped and shifted
    int[] topology = {config.getEyeCells(), config.getBrainNeurons(), 2};
    when(network.matchesTopology(topology)).thenReturn(true);
    when(network.propagate(vision)).thenReturn(networkOutput);
    BirdBrain brain = BirdBrain.fromNetwork(network, config, topology);

    // When
    float[] result = brain.processInputs(vision);

    // Then
    // r0 = 0.8 - 0.5 = 0.3
    // r1 = 0.3 - 0.5 = -0.2
    // speed = clamp(0.3 + (-0.2), -0.5, 0.5) = 0.1
    // rotation = clamp(0.3 - (-0.2), -0.3, 0.3) = clamp(0.5, -0.3, 0.3) = 0.3
    assertThat(result[0]).isCloseTo(0.1f, within(0.001f));
    assertThat(result[1]).isCloseTo(0.3f, within(0.001f));
  }

  @Test
  void testBirdMovement() {
    // Given
    BirdBrain brain = BirdBrain.random(random, config);
    Vector2D position = new Vector2D(0.5f, 0.5f);
    Bird bird = Bird.create(brain, position, config);

    // When
    bird.processBrain(List.<Food>of(), config);
    bird.processMovement();

    // Then
    assertThat(bird.getPosition()).isNotEqualTo(position);
  }

  @Test
  void testBirdVision() {
    // Given
    BirdBrain brain = BirdBrain.random(random, config);
    Vector2D position = new Vector2D(0.5f, 0.5f);
    Bird bird = Bird.create(brain, position, config);
    List<Food> foods = List.<Food>of(Food.at(new Vector2D(0.7f, 0.7f)));

    // When
    bird.processBrain(foods, config);

    // Then
    assertThat(bird.getVision()).hasSize(config.getEyeCells());
  }
}
