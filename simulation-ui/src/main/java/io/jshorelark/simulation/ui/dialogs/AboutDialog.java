/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.dialogs;

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import io.jshorelark.simulation.ui.controls.ControlBuilder;

/**
 * About dialog showing version information and credits.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class AboutDialog {

  /** Version number. */
  private static final String VERSION = "1.0.0";

  /** Original Rust project URL. */
  private static final String ORIGINAL_PROJECT_URL = "https://github.com/patryk27/shorelark";

  /** Blog post URL. */
  private static final String BLOG_URL = "https://pwy.io/posts/learning-to-fly-pt1/";

  /** Bird SVG path data - more stylized bird shape. */
  private static final String BIRD_PATH = "M2,2 L6,0 L10,2 L6,4 Z M10,2 L12,1 L14,2 L12,3 Z";

  /** Food SVG path data - simple dot. */
  private static final String FOOD_PATH = "M0,0 A1,1 0 1,0 2,0 A1,1 0 1,0 0,0";

  /** Random number generator. */
  private static final Random random = new Random();

  /**
   * Shows the about dialog.
   *
   * @param owner owner window
   */
  public static void show(Stage owner) {
    // Create dialog stage
    var dialog = new Stage();
    dialog.initOwner(owner);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.TRANSPARENT);
    dialog.setTitle("JShorelark");
    dialog.setResizable(false);
    dialog.getIcons().addAll(owner.getIcons());

    // Create animated background
    final var background = createAnimatedBackground();

    // Create content
    var content = new VBox(20);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(30));
    content.getStyleClass().add("about-content");

    // Add title
    var title = new Text("JShorelark");
    title.getStyleClass().add("about-title");
    content.getChildren().add(title);

    // Add version
    var version = new Text("Version " + VERSION);
    version.getStyleClass().add("about-version");
    content.getChildren().add(version);

    // Add description
    var description =
        new TextFlow(
            createStyledText("A Java port of "),
            createLink("Shorelark", ORIGINAL_PROJECT_URL),
            createStyledText(", a bird evolution simulation originally written in Rust.\n\n"),
            createStyledText("Based on the blog post series "),
            createLink("Learning to Fly", BLOG_URL),
            createStyledText(" by Patryk Wychowaniec."));
    description.setTextAlignment(TextAlignment.CENTER);
    description.getStyleClass().add("about-description");
    description.setMaxWidth(360);
    content.getChildren().add(description);

    // Add close button
    Button closeButton = ControlBuilder.button().text("×").build();

    closeButton.getStyleClass().add("about-close-button");
    closeButton.setOnAction(e -> dialog.close());

    // Create root layout with animated background
    var root = new StackPane();
    root.getStyleClass().add("about-dialog");
    root.getChildren().addAll(background, content, closeButton);
    StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
    StackPane.setMargin(closeButton, new Insets(10));

    // Make the window draggable
    var dragDelta = new DragDelta();
    root.setOnMousePressed(
        e -> {
          dragDelta.x = dialog.getX() - e.getScreenX();
          dragDelta.y = dialog.getY() - e.getScreenY();
        });
    root.setOnMouseDragged(
        e -> {
          dialog.setX(e.getScreenX() + dragDelta.x);
          dialog.setY(e.getScreenY() + dragDelta.y);
        });

    // Create scene and inherit styles from main window
    var scene = new Scene(root, 500, 400);
    scene.setFill(null);
    scene.getStylesheets().addAll(owner.getScene().getStylesheets());

    dialog.setScene(scene);
    dialog.show();
  }

  /** Helper class for window dragging. */
  private static class DragDelta {
    double x;
    double y;
  }

  /** Creates a bird shape for animation. */
  private static Node createBird() {
    var bird = new SVGPath();
    bird.setContent(BIRD_PATH);
    bird.getStyleClass().add("about-bird");
    return bird;
  }

  /** Creates a food particle for animation. */
  private static Node createFood() {
    var food = new SVGPath();
    food.setContent(FOOD_PATH);
    food.getStyleClass().add("about-food");
    return food;
  }

  /** Creates the animated background. */
  private static Node createAnimatedBackground() {
    var background = new Pane();
    background.getStyleClass().add("about-background");
    background.setPrefSize(500, 400);

    // Create glowing particles
    for (int i = 0; i < 30; i++) {
      var particle = new Circle(4);
      particle.getStyleClass().add("about-particle");
      particle.setEffect(new GaussianBlur(4));
      particle.setTranslateX(random.nextDouble() * 500);
      particle.setTranslateY(random.nextDouble() * 400);
      background.getChildren().add(particle);

      // Create particle animation with higher opacity
      var timeline =
          new Timeline(
              new KeyFrame(
                  Duration.ZERO,
                  new KeyValue(particle.translateXProperty(), particle.getTranslateX()),
                  new KeyValue(particle.translateYProperty(), particle.getTranslateY()),
                  new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.7)),
              new KeyFrame(
                  Duration.seconds(2 + random.nextDouble() * 2),
                  new KeyValue(particle.translateXProperty(), random.nextDouble() * 500),
                  new KeyValue(particle.translateYProperty(), random.nextDouble() * 400),
                  new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.7)));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.setAutoReverse(true);
      timeline.play();
    }

    // Create birds with enhanced animation
    for (int i = 0; i < 10; i++) {
      var bird = createBird();
      bird.setTranslateX(random.nextDouble() * 500);
      bird.setTranslateY(random.nextDouble() * 400);
      bird.setScaleX(1.0);
      bird.setScaleY(1.0);
      background.getChildren().add(bird);

      // Create enhanced bird animation with higher opacity
      var timeline =
          new Timeline(
              new KeyFrame(
                  Duration.ZERO,
                  new KeyValue(bird.translateXProperty(), bird.getTranslateX()),
                  new KeyValue(bird.translateYProperty(), bird.getTranslateY()),
                  new KeyValue(bird.rotateProperty(), random.nextDouble() * 30 - 15),
                  new KeyValue(bird.opacityProperty(), 1.0)),
              new KeyFrame(
                  Duration.seconds(4 + random.nextDouble() * 3),
                  new KeyValue(bird.translateXProperty(), random.nextDouble() * 500),
                  new KeyValue(bird.translateYProperty(), random.nextDouble() * 400),
                  new KeyValue(bird.rotateProperty(), random.nextDouble() * 30 - 15),
                  new KeyValue(bird.opacityProperty(), 1.0)));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.setAutoReverse(true);
      timeline.play();
    }

    // Create food particles with enhanced animation
    for (int i = 0; i < 20; i++) {
      var food = createFood();
      food.setTranslateX(random.nextDouble() * 500);
      food.setTranslateY(random.nextDouble() * 400);
      food.setScaleX(0.8);
      food.setScaleY(0.8);
      background.getChildren().add(food);

      // Create enhanced food animation with higher opacity
      var timeline =
          new Timeline(
              new KeyFrame(
                  Duration.ZERO,
                  new KeyValue(food.translateXProperty(), food.getTranslateX()),
                  new KeyValue(food.translateYProperty(), food.getTranslateY()),
                  new KeyValue(food.rotateProperty(), random.nextDouble() * 360),
                  new KeyValue(food.opacityProperty(), 0.9)),
              new KeyFrame(
                  Duration.seconds(2 + random.nextDouble() * 2),
                  new KeyValue(food.translateXProperty(), random.nextDouble() * 500),
                  new KeyValue(food.translateYProperty(), random.nextDouble() * 400),
                  new KeyValue(food.rotateProperty(), random.nextDouble() * 360),
                  new KeyValue(food.opacityProperty(), 0.9)));
      timeline.setCycleCount(Animation.INDEFINITE);
      timeline.setAutoReverse(true);
      timeline.play();
    }

    return background;
  }

  /** Creates a styled text node that respects the current theme. */
  private static Text createStyledText(String content) {
    var text = new Text(content);
    text.getStyleClass().addAll("about-description", "text");
    return text;
  }

  /** Creates a hyperlink with proper styling. */
  private static Hyperlink createLink(String text, String url) {
    var link = new Hyperlink(text);
    link.getStyleClass().addAll("about-description", "hyperlink");
    link.setOnAction(
        e -> {
          try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
          } catch (Exception ex) {
            // Ignore errors
          }
        });
    return link;
  }
}
