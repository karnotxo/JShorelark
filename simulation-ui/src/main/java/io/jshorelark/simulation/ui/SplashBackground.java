/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * The animated particle background of the splash screen.
 *
 * <p>Decorative only. It owns the particles and their timelines, including the "warp" effect where
 * the cursor pushes nearby particles away.
 */
final class SplashBackground {

  /** Width the particles drift across. */
  static final double WIDTH = 600.0;

  /** Height the particles drift across. */
  static final double HEIGHT = 400.0;

  private static final int PARTICLE_COUNT = 50;
  private static final double BASE_RADIUS = 3.0;
  private static final double INTERACTION_RADIUS = 100.0;
  private static final double PUSH_STRENGTH = 30.0;

  private final Random random = new Random();

  /**
   * Builds the background with its particles already animating.
   *
   * @return the animated background pane
   */
  Pane create() {
    var background = new Pane();
    background.getStyleClass().add("splash-background");
    background.setMouseTransparent(false);

    for (int i = 0; i < PARTICLE_COUNT; i++) {
      background.getChildren().add(createParticle());
    }
    background.setOnMouseMoved(e -> pushParticlesAway(background, e.getX(), e.getY()));
    return background;
  }

  private Circle createParticle() {
    var particle = new Circle(BASE_RADIUS);
    particle.setFill(Color.rgb(255, 255, 255, 0.3));
    particle.setEffect(new GaussianBlur(2));
    particle.setTranslateX(random.nextDouble() * WIDTH);
    particle.setTranslateY(random.nextDouble() * HEIGHT);
    animate(particle);
    return particle;
  }

  private void animate(Circle particle) {
    var drift =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(particle.translateXProperty(), particle.getTranslateX()),
                new KeyValue(particle.translateYProperty(), particle.getTranslateY()),
                new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.2)),
            new KeyFrame(
                Duration.seconds(3 + random.nextDouble() * 2),
                new KeyValue(particle.translateXProperty(), random.nextDouble() * WIDTH),
                new KeyValue(particle.translateYProperty(), random.nextDouble() * HEIGHT),
                new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.2)));
    drift.setCycleCount(Animation.INDEFINITE);
    drift.setAutoReverse(true);
    drift.play();

    var spin =
        new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(particle.rotateProperty(), 0)),
            new KeyFrame(Duration.seconds(2), new KeyValue(particle.rotateProperty(), 360)));
    spin.setCycleCount(Animation.INDEFINITE);
    spin.play();
  }

  /**
   * Pushes the particles around the cursor away, then lets them settle back.
   *
   * @param background the pane holding the particles
   * @param mouseX the cursor's horizontal position
   * @param mouseY the cursor's vertical position
   */
  private void pushParticlesAway(Pane background, double mouseX, double mouseY) {
    for (var node : background.getChildren()) {
      if (node instanceof Circle particle) {
        push(particle, mouseX, mouseY);
      }
    }
  }

  private void push(Circle particle, double mouseX, double mouseY) {
    double dx = particle.getTranslateX() - mouseX;
    double dy = particle.getTranslateY() - mouseY;
    double distance = Math.sqrt(dx * dx + dy * dy);
    if (distance >= INTERACTION_RADIUS || distance == 0.0) {
      // Outside the influence radius, or exactly on the cursor (no direction to push along).
      return;
    }

    double force = (1 - distance / INTERACTION_RADIUS) * PUSH_STRENGTH;
    new Timeline(
            new KeyFrame(
                Duration.millis(100),
                new KeyValue(
                    particle.translateXProperty(),
                    particle.getTranslateX() + dx / distance * force),
                new KeyValue(
                    particle.translateYProperty(),
                    particle.getTranslateY() + dy / distance * force),
                new KeyValue(particle.radiusProperty(), Math.min(6.0, particle.getRadius() * 1.5)),
                new KeyValue(particle.opacityProperty(), 0.8)))
        .play();

    var restore =
        new Timeline(
            new KeyFrame(
                Duration.millis(500),
                new KeyValue(particle.radiusProperty(), BASE_RADIUS),
                new KeyValue(particle.opacityProperty(), 0.3)));
    restore.setDelay(Duration.millis(100));
    restore.play();
  }
}
