/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.dialogs;

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * The drifting particle background of the About dialog.
 *
 * <p>Decorative only: it owns its nodes and their timelines, so the dialog keeps to layout and text
 * and this class can be changed or dropped without touching the dialog's content.
 */
final class AboutBackground {

  /** Dialog width the particles drift across. */
  private static final double WIDTH = 500.0;

  /** Dialog height the particles drift across. */
  private static final double HEIGHT = 400.0;

  private static final int PARTICLE_COUNT = 30;
  private static final int BIRD_COUNT = 10;
  private static final int FOOD_COUNT = 20;

  /** Bird silhouette. */
  private static final String BIRD_PATH = "M2,2 L6,0 L10,2 L6,4 Z M10,2 L12,1 L14,2 L12,3 Z";

  /** Food dot. */
  private static final String FOOD_PATH = "M0,0 A1,1 0 1,0 2,0 A1,1 0 1,0 0,0";

  private final Random random = new Random();

  /**
   * Builds the animated background.
   *
   * @return a pane holding every drifting node, already animated
   */
  Pane create() {
    var background = new Pane();
    background.getStyleClass().add("about-background");
    background.setPrefSize(WIDTH, HEIGHT);

    for (int i = 0; i < PARTICLE_COUNT; i++) {
      addParticle(background);
    }
    for (int i = 0; i < BIRD_COUNT; i++) {
      addBird(background);
    }
    for (int i = 0; i < FOOD_COUNT; i++) {
      addFood(background);
    }
    return background;
  }

  private void addParticle(Pane background) {
    var particle = new Circle(4);
    particle.getStyleClass().add("about-particle");
    particle.setEffect(new GaussianBlur(4));
    double opacity = random.nextDouble() * 0.3 + 0.7;
    animate(background, particle, Duration.seconds(2 + random.nextDouble() * 2), opacity);
  }

  private void addBird(Pane background) {
    var bird = createShape(BIRD_PATH, "about-bird");
    bird.setScaleX(1.0);
    bird.setScaleY(1.0);
    animate(background, bird, Duration.seconds(4 + random.nextDouble() * 3), 1.0);
  }

  private void addFood(Pane background) {
    var food = createShape(FOOD_PATH, "about-food");
    food.setScaleX(0.8);
    food.setScaleY(0.8);
    animate(background, food, Duration.seconds(2 + random.nextDouble() * 2), 0.9);
  }

  private static Node createShape(String content, String styleClass) {
    var shape = new SVGPath();
    shape.setContent(content);
    shape.getStyleClass().add(styleClass);
    return shape;
  }

  /**
   * Places a node at a random spot and drifts it to another one, forever.
   *
   * @param background the pane that owns the node
   * @param node the node to animate
   * @param duration how long one leg of the drift takes
   * @param opacity the opacity the node keeps throughout
   */
  private void animate(Pane background, Node node, Duration duration, double opacity) {
    node.setTranslateX(random.nextDouble() * WIDTH);
    node.setTranslateY(random.nextDouble() * HEIGHT);
    node.setOpacity(opacity);
    background.getChildren().add(node);

    var timeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(node.translateXProperty(), node.getTranslateX()),
                new KeyValue(node.translateYProperty(), node.getTranslateY()),
                new KeyValue(node.rotateProperty(), random.nextDouble() * 30 - 15)),
            new KeyFrame(
                duration,
                new KeyValue(node.translateXProperty(), random.nextDouble() * WIDTH),
                new KeyValue(node.translateYProperty(), random.nextDouble() * HEIGHT),
                new KeyValue(node.rotateProperty(), random.nextDouble() * 30 - 15)));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.setAutoReverse(true);
    timeline.play();
  }
}
