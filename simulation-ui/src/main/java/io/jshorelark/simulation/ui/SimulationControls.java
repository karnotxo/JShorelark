/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import io.jshorelark.simulation.ui.controls.ControlBuilder;
import io.jshorelark.simulation.ui.icons.SvgIcons;

import lombok.extern.slf4j.Slf4j;

/**
 * Controls panel for the simulation.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public class SimulationControls extends VBox {

  /** Wrapper for SimulationApplication to prevent external modification. */
  private static class SimulationApplicationWrapper {
    private final SimulationApplication app;

    private SimulationApplicationWrapper(SimulationApplication app) {
      this.app = app;
    }

    public boolean isPaused() {
      return app.isPaused();
    }

    public void setPaused(boolean paused) {
      app.setPaused(paused);
    }

    public void resetSimulation() {
      app.resetSimulation();
    }

    public void addFood(int amount) {
      app.addFood(amount);
    }

    public void setSimulationSpeed(double speed) {
      app.setSimulationSpeed(speed);
    }

    public int getGeneration() {
      return app.getGeneration();
    }

    public int getPopulationSize() {
      return app.getPopulationSize();
    }

    public double getBestFitness() {
      return app.getBestFitness();
    }

    public ReadOnlyBooleanProperty pausedProperty() {
      return app.pausedProperty();
    }

    public void toggleSound() {
      app.toggleSound();
    }

    public boolean isSoundEnabled() {
      return app.isSoundEnabled();
    }

    public double getGenerationProgress() {
      return app.getGenerationProgress();
    }

    public void setVolume(float volume) {
      app.setVolume(volume);
    }

    public float getVolume() {
      return app.getVolume();
    }
  }

  /** The wrapped main application. */
  private final SimulationApplicationWrapper app;

  /** Play/pause button. */
  private final Button playButton;

  /** Reset button. */
  private final Button resetButton;

  /** Mute button. */
  private final Button muteButton;

  /** Volume icon. */
  private final Label volumeIcon;

  /** Volume slider. */
  private final Slider volumeSlider;

  /** Speed slider. */
  private final Slider speedSlider;

  /** Generation label. */
  private final Label generationLabel;

  /** Population label. */
  private final Label populationLabel;

  /** Best fitness label. */
  private final Label bestFitnessLabel;

  /** Generation progress bar. */
  private final ProgressBar generationProgress;

  /**
   * Creates a new controls panel.
   *
   * @param app the main application
   */
  public SimulationControls(SimulationApplication app) {
    this.app = new SimulationApplicationWrapper(app);

    // Set up layout
    setSpacing(10);
    setPadding(new Insets(10));
    getStyleClass().add("controls-box");

    // Create controls box
    var controlsBox = new HBox(10);
    controlsBox.setAlignment(Pos.CENTER_LEFT);

    // Create controls
    playButton = createPlayPauseButton();
    resetButton = createResetButton();
    muteButton = createMuteButton();
    volumeIcon = new Label();
    volumeSlider = createVolumeSlider();
    speedSlider = createSpeedSlider();

    // Add food button
    var addFoodButton = createAddFoodButton();

    // Speed icon
    var speedIcon = SvgIcons.createSpeedIcon(16, Color.GRAY);

    // Add controls to box
    controlsBox
        .getChildren()
        .addAll(
            playButton,
            resetButton,
            addFoodButton,
            muteButton,
            volumeIcon,
            volumeSlider,
            new Region(),
            speedIcon,
            speedSlider);
    HBox.setHgrow(speedSlider, Priority.ALWAYS);

    // Create stats box
    var statsBox = new HBox(20);
    statsBox.setAlignment(Pos.CENTER);
    statsBox.getStyleClass().add("stats-box");

    // Stats labels
    generationLabel = new Label("Generation: 0");
    populationLabel = new Label("Population: 0");
    bestFitnessLabel = new Label("Best Fitness: 0.0");

    // Generation progress
    generationProgress = new ProgressBar(0);
    generationProgress.setPrefWidth(100);
    generationProgress.setMaxHeight(10);
    generationProgress.getStyleClass().add("generation-progress");

    statsBox
        .getChildren()
        .addAll(generationLabel, generationProgress, populationLabel, bestFitnessLabel);

    // Add everything to main layout
    getChildren().addAll(controlsBox, statsBox);

    // Set up keyboard shortcuts after scene is available
    sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null) {
                setupKeyboardShortcuts(newScene);
              }
            });

    // Start stats update timer
    startStatsUpdateTimer();

    // Initialize play button state
    updatePlayPauseButton();

    // Initialize volume icon
    updateVolumeIcon();

    log.debug("Created simulation controls");
  }

  private Button createPlayPauseButton() {
    var button =
        ControlBuilder.button()
            .tooltip("Play/Pause simulation (Space)")
            .onAction(e -> togglePlayPause())
            .build();
    button.setGraphic(SvgIcons.createPlayIcon(16, Color.GRAY));

    // Update button icon based on pause state
    app.pausedProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              button.setGraphic(
                  newVal
                      ? SvgIcons.createPlayIcon(16, Color.GRAY)
                      : SvgIcons.createPauseIcon(16, Color.GRAY));
            });

    return button;
  }

  private Button createResetButton() {
    final var result =
        ControlBuilder.button()
            .tooltip("Reset simulation (R)")
            .onAction(e -> Platform.runLater(app::resetSimulation))
            .build();
    result.setGraphic(SvgIcons.createResetIcon(16, Color.GRAY));
    return result;
  }

  private Button createAddFoodButton() {
    final var result =
        ControlBuilder.button()
            .tooltip("Add more food (F)")
            .onAction(e -> Platform.runLater(() -> app.addFood(10)))
            .build();
    result.setGraphic(SvgIcons.createFoodIcon(16, Color.GRAY));
    result.setMinWidth(32);
    result.setMinHeight(32);
    return result;
  }

  private Button createMuteButton() {
    final var result =
        ControlBuilder.button()
            .tooltip("Toggle sound (M)")
            .onAction(
                e -> {
                  Platform.runLater(
                      () -> {
                        app.toggleSound();
                        updateMuteButton();
                      });
                })
            .build();
    result.setGraphic(
        app.isSoundEnabled()
            ? SvgIcons.createSoundIcon(16, Color.GRAY, app.getVolume())
            : SvgIcons.createMutedSoundIcon(16, Color.GRAY));
    result.setMinWidth(32);
    result.setMinHeight(32);
    return result;
  }

  /** Updates the mute button icon. */
  private void updateMuteButton() {
    muteButton.setGraphic(
        app.isSoundEnabled()
            ? SvgIcons.createSoundIcon(16, Color.GRAY, app.getVolume())
            : SvgIcons.createMutedSoundIcon(16, Color.GRAY));
    // Also update volume icon when muting/unmuting
    updateVolumeIcon();
  }

  private Slider createSpeedSlider() {
    return ControlBuilder.slider()
        .min(0.1)
        .max(5.0)
        .value(1.0)
        .tooltip("Simulation speed")
        .onValueChanged(
            (v, o, n) -> Platform.runLater(() -> app.setSimulationSpeed(n.doubleValue())))
        .build();
  }

  private Slider createVolumeSlider() {
    var slider =
        ControlBuilder.slider()
            .min(0.0)
            .max(1.0)
            .value(app.getVolume())
            .tooltip("Sound volume")
            .onValueChanged(
                (v, o, n) -> {
                  Platform.runLater(
                      () -> {
                        app.setVolume(n.floatValue());
                        updateVolumeIcon();
                      });
                })
            .build();
    slider.setPrefWidth(100);
    return slider;
  }

  /** Updates the volume icon based on current volume. */
  private void updateVolumeIcon() {
    float volume = app.getVolume();
    volumeIcon.setGraphic(SvgIcons.createSoundIcon(16, Color.GRAY, volume));
  }

  /** Sets up keyboard shortcuts. */
  private void setupKeyboardShortcuts(Scene scene) {
    // Space for play/pause
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE), this::togglePlayPause);

    // R for reset
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R), app::resetSimulation);

    // F for add food
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F), () -> app.addFood(10));

    // +/- for speed control
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN), this::increaseSpeed);
    scene
        .getAccelerators()
        .put(
            new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN),
            this::decreaseSpeed);

    // M for mute/unmute
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M), () -> app.toggleSound());
  }

  /** Toggles play/pause state. */
  private void togglePlayPause() {
    boolean wasPaused = app.isPaused();
    app.setPaused(!wasPaused);
  }

  /** Updates the play/pause button icon. */
  private void updatePlayPauseButton() {
    if (app.isPaused()) {
      playButton.setGraphic(SvgIcons.createPlayIcon(16, Color.GRAY));
      playButton.setTooltip(new Tooltip("Resume simulation (Space)"));
    } else {
      playButton.setGraphic(SvgIcons.createPauseIcon(16, Color.GRAY));
      playButton.setTooltip(new Tooltip("Pause simulation (Space)"));
    }
  }

  /** Increases simulation speed. */
  private void increaseSpeed() {
    speedSlider.setValue(Math.min(speedSlider.getValue() * 1.5, speedSlider.getMax()));
  }

  /** Decreases simulation speed. */
  private void decreaseSpeed() {
    speedSlider.setValue(Math.max(speedSlider.getValue() / 1.5, speedSlider.getMin()));
  }

  /** Starts the timer for updating statistics. */
  private void startStatsUpdateTimer() {
    javafx.animation.AnimationTimer timer =
        new javafx.animation.AnimationTimer() {
          @Override
          public void handle(long now) {
            updateStats();
          }
        };
    timer.start();
  }

  /** Updates the statistics labels. */
  private void updateStats() {
    generationLabel.setText(String.format("Generation: %d", app.getGeneration()));
    populationLabel.setText(String.format("Population: %d", app.getPopulationSize()));
    bestFitnessLabel.setText(String.format("Best Fitness: %.1f", app.getBestFitness()));

    // Update progress bar
    double progress = app.getGenerationProgress();
    generationProgress.setProgress(progress);
  }
}
