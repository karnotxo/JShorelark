/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import io.jshorelark.simulation.ui.icons.AppIcon;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Splash screen shown during application startup.
 *
 * <p>The animated background lives in {@link SplashBackground}; this class owns the window, the
 * title, the progress bar and the status line.
 *
 * @author Jose
 * @version $Id: $Id
 */
public final class SplashScreen extends Preloader {

  /** Minimum time the splash stays visible, so it cannot flash by on a fast start. */
  private static final long MINIMUM_VISIBLE_MILLIS = 3_000L;

  /** Hard limit on how long the splash can linger if the application never reports ready. */
  private static final Duration AUTO_CLOSE_DELAY = Duration.seconds(15);

  private Stage stage;
  private ProgressBar progress;
  private Text status;
  private long startTime;
  private Timeline autoCloseTimer;

  /** {@inheritDoc} */
  @Override
  public void start(Stage stage) {
    this.stage = stage;
    this.startTime = System.currentTimeMillis();

    var layout = createLayout();
    var root = new StackPane(new SplashBackground().create(), layout);
    root.getStyleClass().add("splash-root");
    root.setOnMouseClicked(e -> tryClose());

    var scene = new Scene(root, SplashBackground.WIDTH, SplashBackground.HEIGHT);
    scene
        .getStylesheets()
        .add(SplashScreen.class.getResource("/css/dark-theme.css").toExternalForm());

    stage.initStyle(StageStyle.UNDECORATED);
    stage.setScene(scene);
    centerOnScreen(stage, scene);
    stage.show();

    autoCloseTimer = new Timeline(new KeyFrame(AUTO_CLOSE_DELAY, e -> tryClose()));
    autoCloseTimer.play();
  }

  private VBox createLayout() {
    var iconView = new ImageView(AppIcon.createIcons().getLast());
    iconView.setEffect(new DropShadow(20, Color.BLACK));

    var title = new Text("JShorelark");
    title.getStyleClass().addAll("splash-title", "splash-title-large");
    title.setEffect(new DropShadow(10, Color.BLACK));

    progress = new ProgressBar(0);
    progress.setPrefWidth(400);
    progress.getStyleClass().add("splash-progress");

    status = new Text("Loading...");
    status.getStyleClass().addAll("splash-status", "splash-status-large");

    var layout = new VBox(40, iconView, title, progress, status);
    layout.setAlignment(Pos.CENTER);
    layout.getStyleClass().add("splash-screen");
    return layout;
  }

  private static void centerOnScreen(Stage stage, Scene scene) {
    var bounds = Screen.getPrimary().getVisualBounds();
    stage.setX((bounds.getWidth() - scene.getWidth()) / 2);
    stage.setY((bounds.getHeight() - scene.getHeight()) / 2);
  }

  private void tryClose() {
    long elapsed = System.currentTimeMillis() - startTime;
    if (elapsed < MINIMUM_VISIBLE_MILLIS || stage == null) {
      return;
    }
    if (autoCloseTimer != null) {
      autoCloseTimer.stop();
    }
    stage.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void handleProgressNotification(ProgressNotification pn) {
    if (progress != null) {
      progress.setProgress(pn.getProgress());
    }
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
    if (info instanceof StatusNotification notification && status != null) {
      status.setText(notification.status());
    }
  }

  /**
   * Status notification for updating splash screen text.
   *
   * @param status the message shown under the progress bar
   */
  public record StatusNotification(String status) implements PreloaderNotification {}
}
