/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.bird.BirdBrain;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.events.CollisionListener;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Simulation")
class SimulationTest {

  private Random random;
  private Config config;
  private List<CollisionEvent> collisions;

  @BeforeEach
  void setUp() {
    random = new Random(42);
    collisions = new ArrayList<>();
    // A tiny, short generation keeps the suite fast; the step rules are unchanged.
    config =
        Config.defaults().toBuilder().simGenerationLength(3).worldAnimals(4).worldFoods(6).build();
  }

  private Simulation simulation() {
    return new Simulation(config, random, collisions::add);
  }

  @Nested
  @DisplayName("stepping")
  class Stepping {

    @Test
    @DisplayName("starts at age zero and generation zero")
    void startsFresh() {
      var simulation = simulation();

      assertThat(simulation.getAge()).isZero();
      assertThat(simulation.getGeneration()).isZero();
      assertThat(simulation.getBirds()).hasSize(config.worldAnimals());
      assertThat(simulation.getFoods()).hasSize(config.worldFoods());
      assertThat(simulation.getConfig()).isEqualTo(config);
    }

    @Test
    @DisplayName("returns no statistics while the generation is still running")
    void returnsNullBeforeGenerationEnd() {
      var simulation = simulation();

      // Rust evolves on `age > sim_generation_length`, so the first `length` steps return null.
      for (int i = 0; i < config.simGenerationLength(); i++) {
        assertThat(simulation.step(random)).isNull();
      }
      assertThat(simulation.getAge()).isEqualTo(config.simGenerationLength());

      assertThat(simulation.step(random)).isNotNull();
    }

    @Test
    @DisplayName("reports the generation that just finished and resets the clock")
    void evolvesAtGenerationBoundary() {
      var simulation = simulation();

      Statistics statistics = null;
      for (int i = 0; i <= config.simGenerationLength(); i++) {
        statistics = simulation.step(random);
      }

      // Statistics describe the generation that ended, hence 0 for the first one.
      assertThat(statistics).isNotNull();
      assertThat(statistics.generation()).isZero();
      assertThat(simulation.getAge()).isZero();
      assertThat(simulation.getGeneration()).isEqualTo(1);
      assertThat(simulation.getBirds()).hasSize(config.worldAnimals());
      assertThat(simulation.getFoods()).hasSize(config.worldFoods());
    }

    @Test
    @DisplayName("keeps the food count constant while birds eat")
    void keepsFoodCountConstant() {
      var simulation = simulation();
      var initialFoods = List.copyOf(simulation.getFoods());

      for (int i = 0; i <= config.simGenerationLength(); i++) {
        simulation.step(random);
      }

      assertThat(simulation.getFoods()).hasSize(initialFoods.size());
    }

    @Test
    @DisplayName("moves every bird on each step")
    void movesBirds() {
      var simulation = simulation();
      var before = simulation.getBirds().stream().map(Bird::getPosition).toList();

      simulation.step(random);

      var after = simulation.getBirds().stream().map(Bird::getPosition).toList();
      assertThat(after).isNotEqualTo(before);
    }
  }

  @Nested
  @DisplayName("training")
  class Training {

    @Test
    @DisplayName("runs until a generation completes")
    void trainsUntilGenerationEnds() {
      var simulation = simulation();

      var statistics = simulation.train(random);

      assertThat(statistics).isNotNull();
      assertThat(simulation.getGeneration()).isEqualTo(1);
    }

    @Test
    @DisplayName("fails fast when the population is empty")
    void rejectsEmptyPopulation() {
      var simulation = simulation();
      simulation.clearBirds();

      assertThatThrownBy(() -> simulation.train(random))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("empty");
    }
  }

  @Nested
  @DisplayName("collisions")
  class Collisions {

    @Test
    @DisplayName("notifies the listener and raises satiation")
    void notifiesListener() {
      var simulation = new Simulation(config, random, collisions::add);
      simulation.clearBirds();
      simulation.clearFoods();
      // Food directly under the bird: the distance is zero, inside `foodSize`.
      var bird = Bird.create(BirdBrain.random(random, config), new Vector2D(0.5f, 0.5f), config);
      simulation.addBird(bird);
      simulation.addFood(Food.at(new Vector2D(0.5f, 0.5f)));

      simulation.step(random);

      assertThat(collisions).hasSize(1);
      assertThat(bird.getSatiation()).isEqualTo(1.0f);
      assertThat(collisions.getFirst().distance()).isZero();
    }

    @Test
    @DisplayName("ignores food outside the collision radius")
    void ignoresDistantFood() {
      var simulation = new Simulation(config, random, collisions::add);
      simulation.clearBirds();
      simulation.clearFoods();
      var bird = Bird.create(BirdBrain.random(random, config), new Vector2D(0.5f, 0.5f), config);
      simulation.addBird(bird);
      // Far outside `foodSize`, which is the only radius the Rust code compares against.
      simulation.addFood(Food.at(new Vector2D(0.9f, 0.9f)));

      simulation.step(random);

      assertThat(collisions).isEmpty();
      assertThat(bird.getSatiation()).isZero();
    }
  }

  @Nested
  @DisplayName("population editing")
  class PopulationEditing {

    @Test
    @DisplayName("adds and clears birds and food")
    void editsPopulation() {
      var simulation = simulation();
      simulation.clearBirds();
      simulation.clearFoods();

      simulation.addBird(Bird.random(config, random));
      simulation.addFoods(2, random);

      assertThat(simulation.getBirds()).hasSize(1);
      assertThat(simulation.getFoods()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("fitness reversal")
  class FitnessReversal {

    @Test
    @DisplayName("honours gaReverse so the least fit survive")
    void reversesSelection() {
      var reversed = config.toBuilder().gaReverse(1).build();
      var simulation = new Simulation(reversed, random, CollisionListener.NONE);

      assertThat(simulation.train(random)).isNotNull();
      assertThat(simulation.getGeneration()).isEqualTo(1);
    }
  }
}
