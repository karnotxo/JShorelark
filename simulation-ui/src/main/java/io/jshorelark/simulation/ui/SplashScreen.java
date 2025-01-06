/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui;

import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import io.jshorelark.simulation.ui.icons.AppIcon;

/**
 * Splash screen shown during application startup.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class SplashScreen extends Preloader {
  private StageWrapper stage;
  private ProgressBar progress;
  private Text status;
  private static final Random random = new Random();
  private long startTime;
  private Timeline autoCloseTimer;

  /** Wrapper for Stage to prevent external modification. */
  private static class StageWrapper {
    private final Stage stage;

    /**
     * Creates a new stage wrapper.
     *
     * @param stage a {@link javafx.stage.Stage} object
     */
    private StageWrapper(Stage stage) {
      this.stage = stage;
    }

    /**
     * Sets the scene for the stage.
     *
     * @param scene a {@link javafx.scene.Scene} object
     */
    public void setScene(Scene scene) {
      stage.setScene(scene);
    }

    /**
     * Initializes the style for the stage.
     *
     * @param style a {@link javafx.stage.StageStyle} object
     */
    public void initStyle(StageStyle style) {
      stage.initStyle(style);
    }

    /**
     * Sets the x position of the stage.
     *
     * @param x a double
     */
    public void setX(double x) {
      stage.setX(x);
    }

    /**
     * Sets the y position of the stage.
     *
     * @param y a double
     */
    public void setY(double y) {
      stage.setY(y);
    }

    /** Shows the stage. */
    public void show() {
      stage.show();
    }

    /** Hides the stage. */
    public void hide() {
      stage.hide();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void start(Stage stage) {
    this.stage = new StageWrapper(stage);
    this.startTime = System.currentTimeMillis();

    // Create background with animated particles
    var background = createAnimatedBackground();

    // Create main layout
    var layout = new VBox(40);
    layout.setAlignment(Pos.CENTER);
    layout.getStyleClass().add("splash-screen");

    // Add app icon (using larger icon)
    var iconView = new ImageView(AppIcon.createIcons().get(5)); // 128x128 icon
    iconView.setEffect(new DropShadow(20, Color.BLACK));
    layout.getChildren().add(iconView);

    // Add title with larger font
    var title = new Text("JShorelark");
    title.getStyleClass().addAll("splash-title", "splash-title-large");
    title.setEffect(new DropShadow(10, Color.BLACK));
    layout.getChildren().add(title);

    // Add progress bar
    progress = new ProgressBar(0);
    progress.setPrefWidth(400);
    progress.getStyleClass().add("splash-progress");
    layout.getChildren().add(progress);

    // Add status text with larger font
    status = new Text("Loading...");
    status.getStyleClass().addAll("splash-status", "splash-status-large");
    layout.getChildren().add(status);

    // Create root layout with background
    var root = new StackPane(background, layout);
    root.getStyleClass().add("splash-root");

    // Create scene with doubled size
    var scene = new Scene(root, 600, 400);
    scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

    // Configure stage
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setScene(scene);

    // Center on screen
    var screenBounds = Screen.getPrimary().getVisualBounds();
    stage.setX((screenBounds.getWidth() - scene.getWidth()) / 2);
    stage.setY((screenBounds.getHeight() - scene.getHeight()) / 2);

    // Add click handler to close after minimum time
    root.setOnMouseClicked(e -> tryClose());

    // Set up auto-close timer
    autoCloseTimer = new Timeline(new KeyFrame(Duration.seconds(15), e -> tryClose()));
    autoCloseTimer.play();

    stage.show();
  }

  private void tryClose() {
    long elapsed = System.currentTimeMillis() - startTime;
    if (elapsed >= 3000) { // 3 seconds minimum
      if (autoCloseTimer != null) {
        autoCloseTimer.stop();
      }
      stage.hide();
    }
  }

  private Pane createAnimatedBackground() {
    var background = new Pane();
    background.getStyleClass().add("splash-background");
    background.setMouseTransparent(false); // Allow mouse events

    // Create animated particles
    for (int i = 0; i < 50; i++) { // More particles
      var particle = createParticle();
      background.getChildren().add(particle);

      // Create animation
      animateParticle(particle);
    }

    // Add mouse move handler for particle interaction
    background.setOnMouseMoved(
        e -> {
          // Create warp effect by moving particles away from mouse
          for (var node : background.getChildren()) {
            if (node instanceof Circle particle) {
              double dx = particle.getTranslateX() - e.getX();
              double dy = particle.getTranslateY() - e.getY();
              double distance = Math.sqrt(dx * dx + dy * dy);

              // Only affect particles within range
              if (distance < 100) {
                // Calculate repulsion force (stronger when closer)
                double force = (1 - distance / 100) * 30;

                // Apply force to move particle away from mouse
                Timeline repulsion =
                    new Timeline(
                        new KeyFrame(
                            Duration.millis(100),
                            new KeyValue(
                                particle.translateXProperty(),
                                particle.getTranslateX() + (dx / distance) * force),
                            new KeyValue(
                                particle.translateYProperty(),
                                particle.getTranslateY() + (dy / distance) * force),
                            new KeyValue(
                                particle.radiusProperty(),
                                Math.min(6, particle.getRadius() * 1.5)), // Grow particle
                            new KeyValue(particle.opacityProperty(), 0.8))); // Make brighter

                repulsion.play();

                // Return to normal size after repulsion
                Timeline restore =
                    new Timeline(
                        new KeyFrame(
                            Duration.millis(500),
                            new KeyValue(particle.radiusProperty(), 3),
                            new KeyValue(particle.opacityProperty(), 0.3)));
                restore.setDelay(Duration.millis(100));
                restore.play();
              }
            }
          }
        });

    return background;
  }

  private Circle createParticle() {
    var particle = new Circle(3);
    particle.setFill(Color.rgb(255, 255, 255, 0.3));
    particle.setEffect(new GaussianBlur(2));
    particle.setTranslateX(random.nextDouble() * 600);
    particle.setTranslateY(random.nextDouble() * 400);
    return particle;
  }

  private void animateParticle(Circle particle) {
    // Base movement animation
    var baseMovement =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(particle.translateXProperty(), particle.getTranslateX()),
                new KeyValue(particle.translateYProperty(), particle.getTranslateY()),
                new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.2)),
            new KeyFrame(
                Duration.seconds(3 + random.nextDouble() * 2),
                new KeyValue(particle.translateXProperty(), random.nextDouble() * 600),
                new KeyValue(particle.translateYProperty(), random.nextDouble() * 400),
                new KeyValue(particle.opacityProperty(), random.nextDouble() * 0.3 + 0.2)));
    baseMovement.setCycleCount(Animation.INDEFINITE);
    baseMovement.setAutoReverse(true);

    // Add subtle rotation for more organic movement
    var rotation =
        new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(particle.rotateProperty(), 0)),
            new KeyFrame(Duration.seconds(2), new KeyValue(particle.rotateProperty(), 360)));
    rotation.setCycleCount(Animation.INDEFINITE);

    // Play both animations
    baseMovement.play();
    rotation.play();
  }

  /** {@inheritDoc} */
  @Override
  public void handleProgressNotification(ProgressNotification pn) {
    progress.setProgress(pn.getProgress());
  }

  /** {@inheritDoc} */
  @Override
  public void handleStateChangeNotification(StateChangeNotification evt) {
    if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
      tryClose();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void handleApplicationNotification(PreloaderNotification info) {
    if (info instanceof StatusNotification notification) {
      status.setText(notification.getStatus());
    }
  }

  /** Status notification for updating splash screen text. */
  public static class StatusNotification implements PreloaderNotification {
    private final String status;

    public StatusNotification(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }
  }
}
