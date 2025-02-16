/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.physics;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("World")
class WorldTest {
  private Random random;
  private Config config;

  @BeforeEach
  void setUp() {
    random = new Random(42); // Fixed seed for reproducibility
    config = Config.getDefault();
  }

  @Nested
  @DisplayName("collections")
  class Collections {
    private World world;

    @BeforeEach
    void setUp() {
      world = World.builder().config(config).build();
    }

    @Test
    @DisplayName("manages birds collection")
    void managesBirdsCollection() {
      var bird = Bird.random(config, random);
      world.addBird(bird);

      assertThat(world.getBirds()).hasSize(1).containsExactly(bird);

      world.clearBirds();
      assertThat(world.getBirds()).isEmpty();
    }

    @Test
    @DisplayName("manages food collection")
    void managesFoodCollection() {
      var food = Food.random(random);
      world.addFood(food);

      assertThat(world.getFoods()).hasSize(1).containsExactly(food);

      world.clearFoods();
      assertThat(world.getFoods()).isEmpty();
    }
  }

  @Nested
  @DisplayName("factory")
  class Factory {
    @Test
    @DisplayName("creates random world")
    void createsRandomWorld() {
      var world = World.random(config, random);

      assertThat(world.getBirds()).hasSize(config.getWorldAnimals());
      assertThat(world.getFoods()).hasSize(config.getWorldFoods());
      assertThat(world.getConfig()).isEqualTo(config);
    }
  }
}
