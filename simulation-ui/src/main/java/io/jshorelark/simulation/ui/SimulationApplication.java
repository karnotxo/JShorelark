/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui;

import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.ui.audio.SoundManager;
import io.jshorelark.simulation.ui.dialogs.AboutDialog;
import io.jshorelark.simulation.ui.evolution.EvolutionManager;
import io.jshorelark.simulation.ui.icons.AppIcon;

import lombok.extern.slf4j.Slf4j;

/**
 * Main simulation UI application.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public class SimulationApplication extends Application {
  /** Wrapper for EvolutionManager to prevent external modification. */
  public static class EvolutionManagerWrapper {
    private final EvolutionManager manager;

    private EvolutionManagerWrapper(EvolutionManager manager) {
      this.manager = manager;
    }

    public int getGeneration() {
      return manager.getGeneration();
    }

    public Iterable<?> getBirds() {
      return manager.getBirds();
    }

    public Object getStatistics() {
      return manager.getStatistics();
    }

    public void update(Random random) {
      manager.update(random);
    }

    public void addFoods(int count, Random random) {
      manager.addFoods(count, random);
    }

    public Iterable<?> getFoods() {
      return manager.getFoods();
    }

    public double getFoodSize() {
      return manager.getFoodSize();
    }
  }

  /** Wrapper for Stage to prevent external modification. */
  private static class StageWrapper {
    private final Stage stage;

    private StageWrapper(Stage stage) {
      this.stage = stage;
    }

    public Stage getStage() {
      return stage;
    }

    public void setTitle(String title) {
      stage.setTitle(title);
    }

    public void setIcons(javafx.collections.ObservableList<javafx.scene.image.Image> icons) {
      stage.getIcons().addAll(icons);
    }

    public void setScene(Scene scene) {
      stage.setScene(scene);
    }

    public Scene getScene() {
      return stage.getScene();
    }

    public void show() {
      stage.show();
    }
  }

  /** The evolution manager. */
  private EvolutionManager evolutionManager;

  /** Whether the simulation is paused. */
  private final ReadOnlyBooleanWrapper paused = new ReadOnlyBooleanWrapper(false);

  /** The simulation speed multiplier. */
  private double simulationSpeed = 1.0;

  /** The terminal output area. */
  private TextArea terminalOutput;

  /** The terminal input field. */
  private TextField terminalInput;

  /** The simulation view. */
  private SimulationView simulationView;

  /** The fitness chart. */
  private FitnessChart fitnessChart;

  /** The random number generator. */
  private final Random random = new Random();

  /** The current theme (dark/light). */
  private boolean darkTheme = true;

  /** The primary stage. */
  private StageWrapper primaryStage;

  /** The sound manager. */
  private final SoundManager soundManager = new SoundManager();

  /** Whether sound is enabled. */
  private boolean soundEnabled = true;

  /** The current volume level (0.0 to 1.0). */
  private float volume = 0.5f;

  /**
   * Returns a wrapped view of the evolution manager.
   *
   * @return a {@link io.jshorelark.simulation.ui.SimulationApplication.EvolutionManagerWrapper}
   *     object
   */
  public EvolutionManagerWrapper getEvolutionManager() {
    return new EvolutionManagerWrapper(evolutionManager);
  }

  /**
   * Returns whether the simulation is paused.
   *
   * @return a boolean
   */
  public boolean isPaused() {
    return paused.get();
  }

  /**
   * Sets whether the simulation is paused.
   *
   * @param value a boolean
   */
  public void setPaused(boolean value) {
    paused.set(value);
  }

  /**
   * Returns a read-only view of the paused property.
   *
   * @return a {@link javafx.beans.property.ReadOnlyBooleanProperty} object
   */
  public ReadOnlyBooleanProperty pausedProperty() {
    return paused.getReadOnlyProperty();
  }

  /**
   * Returns the current generation number.
   *
   * @return a int
   */
  public int getGeneration() {
    return evolutionManager.getGeneration();
  }

  /**
   * Returns the current population size.
   *
   * @return a int
   */
  public int getPopulationSize() {
    return evolutionManager.getBirds().size();
  }

  /**
   * Returns the best fitness in the current generation.
   *
   * @return a float
   */
  public float getBestFitness() {
    var stats = evolutionManager.getStatistics();
    return stats != null ? stats.getMaxFitness() : 0.0f;
  }

  /**
   * Sets the simulation speed multiplier.
   *
   * @param speed a double
   */
  public void setSimulationSpeed(double speed) {
    this.simulationSpeed = speed;
  }

  /** Resets the simulation. */
  public void resetSimulation() {
    // Pause the simulation during reset
    boolean wasPaused = isPaused();
    setPaused(true);

    Platform.runLater(
        () -> {
          try {
            // Create new evolution manager
            evolutionManager = new EvolutionManager(random);

            // Create new simulation view
            var newSimulationView = new SimulationView(evolutionManager);
            newSimulationView.setPrefSize(800, 600);

            // Find the parent and replace the old view with the new one
            VBox parent = (VBox) simulationView.getParent();
            int index = parent.getChildren().indexOf(simulationView);
            parent.getChildren().set(index, newSimulationView);

            // Update the reference
            simulationView = newSimulationView;

            // Clear the chart
            fitnessChart.clear();

            // Restore previous pause state
            setPaused(wasPaused);

            println("Simulation reset");
          } catch (Exception e) {
            log.error("Failed to reset simulation", e);
            println("Error: Failed to reset simulation - " + e.getMessage());
          }
        });
  }

  /**
   * Adds food to the simulation.
   *
   * @param count a int
   */
  public void addFood(int count) {
    evolutionManager.addFoods(count, random);
    println("Added " + count + " food items");
  }

  /** {@inheritDoc} */
  @Override
  public void init() {
    // Initialize evolution manager before UI
    evolutionManager = new EvolutionManager(random);

    // Ensure splash screen shows for at least 3 seconds
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Subscribe to collision events
    evolutionManager
        .getCollisionEvents()
        .subscribe(event -> Platform.runLater(() -> handleCollision(event)));
  }

  /** {@inheritDoc} */
  @Override
  public void start(final Stage primaryStage) {
    log.info("Starting simulation UI");
    this.primaryStage = new StageWrapper(primaryStage);

    // Create UI components
    createTerminal();
    createSimulationView();
    createFitnessChart();
    var controls = createControls();

    // Create main layout
    VBox leftPanel = new VBox(10, terminalOutput, terminalInput);
    leftPanel.setPadding(new Insets(10));
    VBox.setVgrow(terminalOutput, Priority.ALWAYS);

    VBox rightPanel = new VBox(10, controls, simulationView, fitnessChart);
    rightPanel.setPadding(new Insets(10));
    VBox.setVgrow(simulationView, Priority.ALWAYS);

    HBox root = new HBox(10, leftPanel, rightPanel);
    root.setPadding(new Insets(10));
    HBox.setHgrow(leftPanel, Priority.NEVER);
    HBox.setHgrow(rightPanel, Priority.ALWAYS);

    // Create menu bar
    MenuBar menuBar = createMenuBar();
    VBox mainLayout = new VBox(menuBar, root);
    VBox.setVgrow(root, Priority.ALWAYS);

    // Create scene
    Scene scene = new Scene(mainLayout);
    scene.getStylesheets().add(getThemeStylesheet());
    this.primaryStage.setTitle("JShorelark");
    this.primaryStage.setIcons(AppIcon.createIcons());
    this.primaryStage.setScene(scene);

    // Print welcome message
    printWelcome();

    // Start animation loop
    startAnimationLoop();

    this.primaryStage.show();
  }

  /** Creates the terminal components. */
  private void createTerminal() {
    terminalOutput = new TextArea();
    terminalOutput.setEditable(false);
    terminalOutput.setWrapText(true);
    terminalOutput.setPrefRowCount(20);
    terminalOutput.setPrefColumnCount(51);
    terminalOutput.setMinWidth(204);
    terminalOutput.setFont(javafx.scene.text.Font.font("Monospaced", 12));
    terminalOutput.getStyleClass().add("terminal");
    terminalOutput.setFocusTraversable(false); // Prevent focus traversal
    terminalOutput.setMouseTransparent(true); // Prevent mouse focus

    terminalInput = new TextField();
    terminalInput.setOnAction(e -> handleCommand(terminalInput.getText()));
    terminalInput.setFont(javafx.scene.text.Font.font("Monospaced", 12));
    terminalInput.getStyleClass().add("terminal");
  }

  /** Creates the simulation view. */
  private void createSimulationView() {
    simulationView = new SimulationView(evolutionManager);
    simulationView.setPrefSize(800, 600);
  }

  /** Creates the fitness chart. */
  private void createFitnessChart() {
    fitnessChart = new FitnessChart();
    fitnessChart.setPrefHeight(200);
  }

  /** Creates the control buttons. */
  private SimulationControls createControls() {
    var controls = new SimulationControls(this);
    controls.getStyleClass().add("controls");
    return controls;
  }

  /** Creates the menu bar. */
  private MenuBar createMenuBar() {
    Menu fileMenu = new Menu("File");
    fileMenu
        .getItems()
        .addAll(
            new MenuItem("Reset") {
              {
                setOnAction(e -> handleReset(new String[] {"r"}));
              }
            },
            new SeparatorMenuItem(),
            new MenuItem("Exit") {
              {
                setOnAction(e -> Platform.exit());
              }
            });

    Menu viewMenu = new Menu("View");
    var themeMenuItem = new CheckMenuItem("Dark Theme");
    themeMenuItem.setSelected(darkTheme);
    themeMenuItem.setOnAction(e -> toggleTheme());

    var soundMenuItem = new CheckMenuItem("Sound Effects");
    soundMenuItem.setSelected(soundEnabled);
    soundMenuItem.setOnAction(
        e -> {
          soundEnabled = soundMenuItem.isSelected();
          soundManager.setEnabled(soundEnabled);
        });

    viewMenu.getItems().addAll(themeMenuItem, soundMenuItem);

    Menu helpMenu = new Menu("Help");
    helpMenu
        .getItems()
        .addAll(
            new MenuItem("About") {
              {
                setOnAction(e -> AboutDialog.show(primaryStage.getStage()));
              }
            });

    return new MenuBar(fileMenu, viewMenu, helpMenu);
  }

  /** Gets the current theme stylesheet. */
  private String getThemeStylesheet() {
    return darkTheme ? "/css/dark-theme.css" : "/css/light-theme.css";
  }

  /** Toggles the theme. */
  private void toggleTheme() {
    darkTheme = !darkTheme;
    Scene scene = primaryStage.getScene();
    if (scene != null) {
      scene.getStylesheets().clear();
      scene.getStylesheets().add(getThemeStylesheet());
    }
    // Update simulation view theme
    if (simulationView != null) {
      simulationView.setTheme(darkTheme);
    }
  }

  /** Starts the animation loop. */
  private void startAnimationLoop() {
    Thread animationThread =
        new Thread(
            () -> {
              while (true) {
                try {
                  Thread.sleep((long) (16 / simulationSpeed)); // ~60 FPS adjusted for speed
                  Platform.runLater(this::redraw);
                } catch (InterruptedException e) {
                  break;
                }
              }
            },
            "AnimationLoop");
    animationThread.setDaemon(true);
    animationThread.start();
  }

  /** Redraws the simulation. */
  private void redraw() {
    if (!paused.get()) {
      var stats = evolutionManager.update(random);
      if (stats != null) {
        handleGenerationComplete();
        println(stats.toString());
        fitnessChart.update(
            stats.getGeneration(),
            stats.getMaxFitness(),
            stats.getAvgFitness(),
            stats.getMedianFitness(),
            stats.getMinFitness());
      }
    }

    simulationView.update();
  }

  /** Handles a command from the terminal. */
  private void handleCommand(String command) {
    terminalInput.clear();

    if (command.isEmpty()) {
      return;
    }

    String[] parts = command.trim().toLowerCase().split("\\s+");
    String cmd = parts[0];

    try {
      switch (cmd) {
        case "p":
        case "pause":
          setPaused(!isPaused());
          break;

        case "r":
        case "reset":
          handleReset(parts);
          break;

        case "t":
        case "train":
          handleTrain(parts);
          break;

        default:
          println("Unknown command: " + cmd);
          break;
      }
    } catch (Exception e) {
      println("Error: " + e.getMessage());
    }
  }

  /** Handles the reset command. */
  private void handleReset(String[] args) {
    // Pause the simulation during reset
    boolean wasPaused = isPaused();
    setPaused(true);

    Platform.runLater(
        () -> {
          try {
            Config config = Config.getDefault();

            // Parse arguments
            for (int i = 1; i < args.length; i++) {
              String[] parts = args[i].split("=");
              if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid argument format: " + args[i]);
              }

              String key = parts[0];
              float value = Float.parseFloat(parts[1]);

              switch (key) {
                case "a":
                case "animals":
                  config = config.withWorldAnimals((int) value);
                  break;
                case "f":
                case "foods":
                  config = config.withWorldFoods((int) value);
                  break;
                case "n":
                case "neurons":
                  config = config.withBrainNeurons((int) value);
                  break;
                case "p":
                case "photoreceptors":
                  config = config.withEyeCells((int) value);
                  break;
                default:
                  throw new IllegalArgumentException("Unknown parameter: " + key);
              }
            }

            resetSimulationWithConfig(config, wasPaused);
          } catch (IllegalArgumentException e) {
            log.error("Failed to reset simulation", e);
            println("Error: " + e.getMessage());
          }
        });
  }

  /** Resets the simulation with the given config. */
  private void resetSimulationWithConfig(Config config, boolean wasPaused) {
    // Create new evolution manager with the updated config
    evolutionManager = new EvolutionManager(random, config);

    // Create new simulation view
    var newSimulationView = new SimulationView(evolutionManager);
    newSimulationView.setPrefSize(800, 600);

    // Find the parent and replace the old view with the new one
    VBox parent = (VBox) simulationView.getParent();
    int index = parent.getChildren().indexOf(simulationView);
    parent.getChildren().set(index, newSimulationView);

    // Update the reference
    simulationView = newSimulationView;

    // Clear the chart
    fitnessChart.clear();

    // Restore previous pause state
    setPaused(wasPaused);

    println("Simulation reset with new configuration");
  }

  /** Handles the train command. */
  private void handleTrain(String[] args) {
    if (args.length > 2) {
      throw new IllegalArgumentException("Too many arguments");
    }

    int generations = args.length == 1 ? 1 : Integer.parseInt(args[1]);

    for (int i = 0; i < generations; i++) {
      if (i > 0) {
        println("");
      }
      var stats = evolutionManager.update(random);
      println(stats.toString());
      fitnessChart.update(
          stats.getGeneration(),
          stats.getMaxFitness(),
          stats.getAvgFitness(),
          stats.getMedianFitness(),
          stats.getMinFitness());
    }
  }

  /** Prints a line to the terminal. */
  private void println(String text) {
    terminalOutput.appendText(text + "\n");
  }

  /** Prints the welcome message. */
  private void printWelcome() {
    println("     ________                __         __  ");
    println(" __ / / __/ /  ___  _______ / /__ _____/ /__");
    println("/ // /\\ \\/ _ \\/ _ \\/ __/ -_) / _ `/ __/  '_/");
    println("\\___/___/_//_/\\___/_/  \\__/_/\\_,_/_/ /_/\\_\\ ");
    println("");
    println(
        "Simulation of evolution, powered by neural network, genetic algorithm and high school"
            + " math.");
    println("");
    println("Would you like to learn how it works?");
    println("https://pwy.io/posts/learning-to-fly-pt1/");
    println("");
    println("Would you like to read the source code?");
    println("https://github.com/Patryk27/shorelark");
    println("");
    println("Have fun!");
    println("");
    println("---- Commands ----");
    println("");
    println("- p / pause");
    println("  Pauses (or resumes) the simulation");
    println("");
    println("- r / reset [animals=15] [f=40] [...]");
    println("  Starts simulation from scratch with given optional parameters:");
    println("");
    println("  * a / animals (default=15)");
    println("    number of animals");
    println("");
    println("  * f / foods (default=40)");
    println("    number of foods");
    println("");
    println("  * n / neurons (default=8)");
    println("    number of brain neurons per each animal");
    println("");
    println("  * p / photoreceptors (default=9)");
    println("    number of eye cells per each animal");
    println("");
    println("- t / train [generations=1]");
    println("    trains the simulation for given number of generations");
    println("");
  }

  /**
   * Main entry point.
   *
   * @param args an array of {@link java.lang.String} objects
   */
  public static void main(String[] args) {
    // Set up splash screen
    System.setProperty("javafx.preloader", SplashScreen.class.getName());
    launch(args);
  }

  /**
   * Returns the progress of the current generation (0.0 to 1.0).
   *
   * @return a double representing the progress
   */
  public double getGenerationProgress() {
    if (evolutionManager == null) return 0.0;

    double maxSatiation = evolutionManager.getSimGenerationLength();
    double currentMaxSatiation = 0.0;

    for (final var bird : evolutionManager.getBirds()) {
      currentMaxSatiation = Math.max(currentMaxSatiation, bird.getSatiation());
    }

    return Math.min(currentMaxSatiation / maxSatiation, 1.0);
  }

  /** Handles generation completion. */
  private void handleGenerationComplete() {
    if (soundEnabled) {
      soundManager.playGenerationSound();
    }
  }

  /** Toggles sound on/off. */
  public void toggleSound() {
    soundEnabled = !soundEnabled;
    soundManager.setEnabled(soundEnabled);
  }

  /** Returns whether sound is enabled. */
  public boolean isSoundEnabled() {
    return soundEnabled;
  }

  /** Sets the volume level (0.0 to 1.0). */
  public void setVolume(float volume) {
    this.volume = volume;
    soundManager.setVolume(volume);
  }

  /** Gets the current volume level (0.0 to 1.0). */
  public float getVolume() {
    return volume;
  }

  /** Handles a collision event. */
  private void handleCollision(CollisionEvent event) {
    if (soundEnabled) {
      soundManager.playFoodSound();
    }
  }
}
