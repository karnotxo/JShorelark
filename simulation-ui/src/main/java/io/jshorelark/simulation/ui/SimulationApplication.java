/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import io.jshorelark.simulation.Config;
import io.jshorelark.simulation.events.CollisionEvent;
import io.jshorelark.simulation.ui.audio.SoundManager;
import io.jshorelark.simulation.ui.dialogs.AboutDialog;
import io.jshorelark.simulation.ui.evolution.EvolutionManager;
import io.jshorelark.simulation.ui.icons.AppIcon;
import java.util.Locale;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Main simulation UI application.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public class SimulationApplication extends Application implements SimulationCommands {
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

    /**
     * Advances the wrapped manager by one step.
     *
     * @param random the random source
     */
    public void update(Random random) {
      manager.update(random);
    }

    /**
     * Adds food to the wrapped manager.
     *
     * @param count how many items to add
     * @param random the random source
     */
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

  /**
   * Wall-clock budget of one simulation frame at speed 1, i.e. roughly 60 steps per second.
   *
   * <p>Nanos rather than millis so the animation timer can compare against {@code
   * AnimationTimer#handle}, which reports nanoseconds.
   */
  private static final long BASE_FRAME_NANOS = 16_666_667L;

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

  /**
   * The scene once {@link #start(Stage)} has built it.
   *
   * <p>Held so theme toggling does not have to read it back from the stage wrapper.
   */
  private Scene scene;

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
  @Override
  public boolean isPaused() {
    return paused.get();
  }

  /**
   * Sets whether the simulation is paused.
   *
   * @param value a boolean
   */
  @Override
  public void setPaused(boolean value) {
    paused.set(value);
  }

  /**
   * Returns a read-only view of the paused property.
   *
   * @return a {@link javafx.beans.property.ReadOnlyBooleanProperty} object
   */
  @Override
  public ReadOnlyBooleanProperty pausedProperty() {
    return paused.getReadOnlyProperty();
  }

  /**
   * Returns the current generation number.
   *
   * @return a int
   */
  @Override
  public int getGeneration() {
    return evolutionManager.getGeneration();
  }

  /**
   * Returns the current population size.
   *
   * @return a int
   */
  @Override
  public int getPopulationSize() {
    return evolutionManager.getBirds().size();
  }

  /**
   * Returns the best fitness in the current generation.
   *
   * @return a float
   */
  @Override
  public double getBestFitness() {
    var stats = evolutionManager.getStatistics();
    return stats != null ? stats.maxFitness() : 0.0f;
  }

  /**
   * Sets the simulation speed multiplier.
   *
   * @param speed a double
   */
  @Override
  public void setSimulationSpeed(double speed) {
    this.simulationSpeed = speed;
  }

  /**
   * Creates an evolution manager and wires it to the UI's collision feedback.
   *
   * <p>The handler is set here, in one place, so a reset cannot silently lose the sound effects.
   *
   * @param config the configuration for the new manager
   * @return a fully wired manager
   */
  private EvolutionManager createEvolutionManager(Config config) {
    var manager = new EvolutionManager(random, config);
    manager.setCollisionHandler(event -> Platform.runLater(() -> handleCollision(event)));
    return manager;
  }

  /** Resets the simulation to the reference configuration. */
  @Override
  public void resetSimulation() {
    boolean wasPaused = isPaused();
    setPaused(true);

    Platform.runLater(() -> resetSimulationWithConfig(Config.defaults(), wasPaused));
  }

  /**
   * Adds food to the simulation.
   *
   * @param count a int
   */
  @Override
  public void addFood(int count) {
    evolutionManager.addFoods(count, random);
    println("Added " + count + " food items");
  }

  /** {@inheritDoc} */
  @Override
  public void init() {
    // Build the world before the scene exists. The splash screen already enforces its own minimum
    // visible time, so blocking here would only delay startup.
    evolutionManager = createEvolutionManager(Config.defaults());
  }

  /** {@inheritDoc} */
  @Override
  public void start(final Stage primaryStage) {
    log.info("Starting simulation UI");

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

    HBox content = new HBox(10, leftPanel, rightPanel);
    content.setPadding(new Insets(10));
    HBox.setHgrow(leftPanel, Priority.NEVER);
    HBox.setHgrow(rightPanel, Priority.ALWAYS);

    this.scene = attachScene(primaryStage, content);
    printWelcome();
    startAnimationLoop();
    primaryStage.show();
  }

  /**
   * Builds the window's scene graph and installs it on the stage.
   *
   * @param stage the primary stage
   * @param content the simulation panel
   * @return the scene, so the theme toggle can swap stylesheets
   */
  private Scene attachScene(Stage stage, Region content) {
    var mainLayout = new VBox(createMenuBar(stage), content);
    VBox.setVgrow(content, Priority.ALWAYS);

    var scene = new Scene(mainLayout);
    scene.getStylesheets().add(getThemeStylesheet());
    stage.setTitle(Messages.text(Messages.WINDOW_TITLE));
    stage.getIcons().addAll(AppIcon.createIcons());
    stage.setScene(scene);
    return scene;
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
    simulationView.initializeView();
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

  /**
   * Creates the menu bar.
   *
   * @param owner the window that owns the About dialog
   * @return the populated menu bar
   */
  private MenuBar createMenuBar(Stage owner) {
    var actions =
        new SimulationMenuBar.Actions(
            () -> handleReset(new String[] {"r"}),
            this::toggleTheme,
            enabled -> {
              soundEnabled = enabled;
              soundManager.setEnabled(enabled);
            },
            () -> AboutDialog.show(owner));
    return new SimulationMenuBar(owner, darkTheme, soundEnabled, actions);
  }

  /** Gets the current theme stylesheet. */
  private String getThemeStylesheet() {
    return darkTheme ? "/css/dark-theme.css" : "/css/light-theme.css";
  }

  /** Toggles the theme. */
  private void toggleTheme() {
    darkTheme = !darkTheme;
    if (scene != null) {
      scene.getStylesheets().clear();
      scene.getStylesheets().add(getThemeStylesheet());
    }
    // Update simulation view theme
    if (simulationView != null) {
      simulationView.setTheme(darkTheme);
    }
  }

  /**
   * Starts the animation loop.
   *
   * <p>An {@link AnimationTimer} ticks on the FX thread, so no work crosses threads and the scene
   * graph is only ever touched where it is allowed to be. The timer accumulates the elapsed time
   * and advances the simulation whenever the accumulated budget covers one frame at the current
   * speed, which keeps the pace independent of the display refresh rate.
   */
  private void startAnimationLoop() {
    var timer =
        new AnimationTimer() {
          private long lastTick;
          private boolean primed;

          @Override
          public void handle(long now) {
            // JavaFX reports nanoseconds since an arbitrary origin, so zero is a valid timestamp:
            // a flag separates "not started yet" from a real tick instead of a sentinel value.
            if (!primed) {
              primed = true;
              lastTick = now;
              return;
            }
            long elapsedNanos = now - lastTick;
            long budgetNanos = (long) (BASE_FRAME_NANOS / simulationSpeed);
            if (elapsedNanos < budgetNanos) {
              return;
            }
            lastTick = now;
            redraw();
          }
        };
    timer.start();
  }

  /** Redraws the simulation. */
  private void redraw() {
    if (!paused.get()) {
      var stats = evolutionManager.update(random);
      if (stats != null) {
        handleGenerationComplete();
        println(stats.toString());
        fitnessChart.update(
            stats.generation(),
            stats.maxFitness(),
            stats.avgFitness(),
            stats.medianFitness(),
            stats.minFitness());
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

    // Locale.ROOT: the commands are ASCII keywords, and a Turkish locale would lowercase "I" to a
    // dotless i, silently rejecting every uppercase command.
    String[] parts = command.trim().toLowerCase(Locale.ROOT).split("\\s+");
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
    } catch (IllegalArgumentException | IllegalStateException e) {
      log.error("Command failed: {}", command, e);
      println("Error: " + e.getMessage());
    }
  }

  /** Handles the reset command. */
  private void handleReset(String[] args) {
    // Parse before touching the world: a bad argument must not stop a running simulation.
    Config config;
    try {
      config = parseResetArguments(args);
    } catch (IllegalArgumentException e) {
      println("Error: " + e.getMessage());
      return;
    }

    boolean wasPaused = isPaused();
    setPaused(true);
    Platform.runLater(() -> resetSimulationWithConfig(config, wasPaused));
  }

  /**
   * Applies {@code key=value} overrides to the reference configuration.
   *
   * @param args the raw command tokens, starting with the command itself
   * @return the derived configuration
   * @throws IllegalArgumentException if a token is malformed or the key is unknown
   */
  private static Config parseResetArguments(String[] args) {
    Config config = Config.defaults();
    for (int i = 1; i < args.length; i++) {
      String[] parts = args[i].split("=");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid argument format: " + args[i]);
      }
      int value;
      try {
        value = Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Not a whole number: " + parts[1], e);
      }
      config = applyOverride(config, parts[0], value);
    }
    return config;
  }

  private static Config applyOverride(Config config, String key, int value) {
    return switch (key) {
      case "a", "animals" -> config.withWorldAnimals(value);
      case "f", "foods" -> config.withWorldFoods(value);
      case "n", "neurons" -> config.withBrainNeurons(value);
      case "p", "photoreceptors" -> config.withEyeCells(value);
      default -> throw new IllegalArgumentException("Unknown parameter: " + key);
    };
  }

  /** Resets the simulation with the given config. */
  private void resetSimulationWithConfig(Config config, boolean wasPaused) {
    // Create new evolution manager with the updated config
    evolutionManager = createEvolutionManager(config);

    // Create new simulation view
    var newSimulationView = new SimulationView(evolutionManager);
    newSimulationView.setPrefSize(800, 600);
    newSimulationView.initializeView();

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
      var stats = evolutionManager.train(random);
      println(stats.toString());
      fitnessChart.update(
          stats.generation(),
          stats.maxFitness(),
          stats.avgFitness(),
          stats.medianFitness(),
          stats.minFitness());
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
  @Override
  public double getGenerationProgress() {
    return evolutionManager == null ? 0.0 : evolutionManager.getGenerationProgress();
  }

  /** Handles generation completion. */
  private void handleGenerationComplete() {
    if (soundEnabled) {
      soundManager.playGenerationSound();
    }
  }

  /** Toggles sound on/off. */
  @Override
  public void toggleSound() {
    soundEnabled = !soundEnabled;
    soundManager.setEnabled(soundEnabled);
  }

  /** Returns whether sound is enabled. */
  @Override
  public boolean isSoundEnabled() {
    return soundEnabled;
  }

  /** Sets the volume level (0.0 to 1.0). */
  @Override
  public void setVolume(float volume) {
    this.volume = volume;
    soundManager.setVolume(volume);
  }

  /** Gets the current volume level (0.0 to 1.0). */
  @Override
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
