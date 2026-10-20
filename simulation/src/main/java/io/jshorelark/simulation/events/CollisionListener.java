/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.events;

/**
 * Port for observers interested in the moment a bird eats a food item.
 *
 * <p>The simulation calls this from its own step, on the thread that is stepping it, so an
 * implementation must be cheap and must not block. Adapters that need another thread (for example
 * the JavaFX application thread) are responsible for marshalling.
 *
 * <p>This replaces the earlier reactive-streams sink: the domain must not depend on a reactive
 * runtime to notify a listener.
 */
@FunctionalInterface
public interface CollisionListener {

  /**
   * Called after a bird has consumed a food item and the item has been repositioned.
   *
   * @param event an immutable snapshot of the collision
   */
  void onCollision(CollisionEvent event);

  /** A listener that ignores every event. */
  CollisionListener NONE = event -> {};
}
