/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.bird.BirdBrain;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CollisionEvent")
class CollisionEventTest {

  @Test
  @DisplayName("snapshots the bird and food state at the collision")
  void snapshotsState() {
    var config = Config.defaults();
    var random = new Random(42);
    var bird = Bird.create(BirdBrain.random(random, config), new Vector2D(0.25f, 0.75f), config);
    bird.eat();
    var food = Food.at(new Vector2D(0.26f, 0.75f));

    var event = CollisionEvent.create(bird, food, 0.01f);

    assertThat(event.birdPosition()).isEqualTo(new Vector2D(0.25f, 0.75f));
    assertThat(event.birdRotation()).isEqualTo(bird.getRotation());
    assertThat(event.birdSatiation()).isEqualTo(1.0f);
    assertThat(event.foodPosition()).isEqualTo(new Vector2D(0.26f, 0.75f));
    assertThat(event.distance()).isEqualTo(0.01f);
  }

  @Test
  @DisplayName("provides a no-op listener")
  void ignoreListenerIsNoOp() {
    var event = new CollisionEvent(Vector2D.ZERO, 0.0f, 0.0f, Vector2D.ZERO, 0.0f);

    assertThatCode(() -> CollisionListener.NONE.onCollision(event)).doesNotThrowAnyException();
  }
}
