/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import org.apache.commons.math3.util.FastMath;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import io.jshorelark.simulation.ui.evolution.EvolutionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Canvas that displays the simulation state.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public class SimulationView extends Pane {
  /** Colors for dark theme. */
  private static final class DarkTheme {
    static final Color BIRD_COLOR = Color.web("#3498db");
    static final Color BIRD_OUTLINE_COLOR = Color.web("#2980b9");
    static final Color EYE_COLOR = Color.web("#2ecc71");
    static final Color FOOD_COLOR = Color.web("#e74c3c");
    static final Color TRAIL_COLOR = Color.rgb(255, 255, 255, 0.2);
    static final Color SENSOR_EMPTY_COLOR = Color.rgb(255, 255, 255, 0.1);
    static final Color GRID_COLOR = Color.rgb(255, 255, 255, 0.1);
    static final Color INFO_BOX_BG = Color.rgb(0, 0, 0, 0.8);
    static final Color INFO_TEXT = Color.WHITE;
  }

  /** Colors for light theme. */
  private static final class LightTheme {
    static final Color BIRD_COLOR = Color.web("#2980b9");
    static final Color BIRD_OUTLINE_COLOR = Color.web("#1a5276");
    static final Color EYE_COLOR = Color.web("#27ae60");
    static final Color FOOD_COLOR = Color.web("#c0392b");
    static final Color TRAIL_COLOR = Color.rgb(0, 0, 0, 0.2);
    static final Color SENSOR_EMPTY_COLOR = Color.rgb(0, 0, 0, 0.1);
    static final Color GRID_COLOR = Color.rgb(0, 0, 0, 0.1);
    static final Color INFO_BOX_BG = Color.rgb(255, 255, 255, 0.9);
    static final Color INFO_TEXT = Color.BLACK;
  }

  /** Wrapper for simulation state needed by the view. */
  private static class ViewState {
    private final EvolutionManager source;

    private ViewState(EvolutionManager source) {
      this.source = source;
    }

    public Iterable<Food> getFoods() {
      return (Iterable<Food>) source.getFoods();
    }

    public Iterable<Bird> getBirds() {
      return (Iterable<Bird>) source.getBirds();
    }

    public double getFoodSize() {
      return source.getFoodSize();
    }

    public double getBirdSize() {
      return source.getBirdSize();
    }

    public double getEyeFovAngle() {
      return source.getEyeFovAngle();
    }

    public int getEyeCells() {
      return source.getEyeCells();
    }

    public double getEyeFovRange() {
      return source.getEyeFovRange();
    }
  }

  /** The simulation state. */
  private final ViewState state;

  private Runnable onHoveredBirdChanged;

  /** The canvas for drawing. */
  private final Canvas canvas;

  /** Mouse position in screen coordinates. */
  private Vector2D screenMousePos = Vector2D.ZERO;

  /** Currently hovered bird. */
  private Bird hoveredBird = null;

  /** Whether dark theme is enabled. */
  private boolean darkTheme = true;

  /** Sets the theme. */
  public void setTheme(boolean dark) {
    this.darkTheme = dark;
    draw();
  }

  /** Gets the current theme colors. */
  private Object getCurrentTheme() {
    return darkTheme ? DarkTheme.class : LightTheme.class;
  }

  /**
   * Creates a new simulation view.
   *
   * @param manager a {@link
   *     io.jshorelark.simulation.ui.SimulationApplication.EvolutionManagerWrapper} object
   */
  public SimulationView(EvolutionManager manager) {
    this.state = new ViewState(manager);
    this.canvas = new Canvas();
    getChildren().add(canvas);

    // Bind canvas size to parent size
    canvas.widthProperty().bind(widthProperty());
    canvas.heightProperty().bind(heightProperty());

    // Handle parent resizing
    widthProperty().addListener((obs, oldVal, newVal) -> requestLayout());
    heightProperty().addListener((obs, oldVal, newVal) -> requestLayout());

    // Mouse event handlers
    setupMouseHandlers();

    log.debug("Created simulation view");
  }

  /** Sets up mouse event handlers. */
  private void setupMouseHandlers() {
    canvas.setOnMouseMoved(this::handleMouseMoved);
    canvas.setOnMouseExited(
        e -> {
          screenMousePos = Vector2D.ZERO;
          hoveredBird = null;
          draw();
        });
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();

    // Ensure canvas fills the available space
    double width = getWidth();
    double height = getHeight();

    if (width > 0 && height > 0) {
      // Unbind properties before setting them directly
      canvas.widthProperty().unbind();
      canvas.heightProperty().unbind();

      // Now set the dimensions
      canvas.setWidth(width);
      canvas.setHeight(height);

      // Rebind for future updates
      canvas.widthProperty().bind(widthProperty());
      canvas.heightProperty().bind(heightProperty());
    }
  }

  /**
   * Sets a callback to be called when the hovered bird changes.
   *
   * @param callback the callback to run
   */
  public void setOnHoveredBirdChanged(Runnable callback) {
    this.onHoveredBirdChanged = callback;
  }

  /**
   * Handles mouse movement events.
   *
   * @param event mouse event
   */
  private void handleMouseMoved(javafx.scene.input.MouseEvent event) {
    screenMousePos = new Vector2D((float) event.getX(), (float) event.getY());
    updateHoveredBird();
  }

  /** Updates the currently hovered bird based on mouse position. */
  private void updateHoveredBird() {
    Bird newHoveredBird = null;
    double closestDistance = Double.MAX_VALUE;

    // Convert screen mouse position to world coordinates for distance calculation
    Vector2D worldMousePos = screenToWorld(screenMousePos);
    double worldBirdSize = state.getBirdSize(); // Already in world coordinates

    for (Bird bird : state.getBirds()) {
      Vector2D worldBirdPos = bird.getPosition(); // Already in world coordinates
      double distance = worldMousePos.subtract(worldBirdPos).length();
      if (distance < worldBirdSize * 2 && distance < closestDistance) {
        newHoveredBird = bird;
        closestDistance = distance;
      }
    }

    if (hoveredBird != newHoveredBird) {
      hoveredBird = newHoveredBird;
      if (onHoveredBirdChanged != null) {
        onHoveredBirdChanged.run();
      }
      draw();
    }
  }

  /** Updates the view by redrawing. */
  public void update() {
    draw();
  }

  /** Draws the simulation state. */
  private void draw() {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    // Enable antialiasing
    gc.setImageSmoothing(true);
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

    // Draw background grid
    drawGrid(gc);

    // Draw food
    gc.setFill(darkTheme ? DarkTheme.FOOD_COLOR : LightTheme.FOOD_COLOR);
    for (Food food : state.getFoods()) {
      drawFood(gc, food);
    }

    // Draw birds
    for (Bird bird : state.getBirds()) {
      drawBird(gc, bird);
    }

    // Draw hover effects
    if (hoveredBird != null) {
      drawBirdDetails(gc, hoveredBird);
    }
  }

  /** Draws a background grid. */
  private void drawGrid(GraphicsContext gc) {
    gc.setStroke(darkTheme ? DarkTheme.GRID_COLOR : LightTheme.GRID_COLOR);
    gc.setLineWidth(1);

    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());

    double gridSize = 50;
    for (double x = 0.0d; x < worldScale; x += gridSize) {
      gc.strokeLine(x, 0.0d, x, worldScale);
    }
    for (double y = 0.0d; y < worldScale; y += gridSize) {
      gc.strokeLine(0.0d, y, worldScale, y);
    }
  }

  /** Draws a food item. */
  private void drawFood(GraphicsContext gc, Food food) {
    gc.setFill(darkTheme ? DarkTheme.FOOD_COLOR : LightTheme.FOOD_COLOR);
    Vector2D screenPos = worldToScreen(food.getPosition());
    double screenSize = getScreenFoodSize();
    gc.fillOval(
        screenPos.x() - screenSize / 2, screenPos.y() - screenSize / 2, screenSize, screenSize);
  }

  /**
   * Draws detailed information about a bird.
   *
   * @param bird bird to draw details for
   */
  private void drawBirdDetails(GraphicsContext gc, Bird bird) {
    Vector2D pos = worldToScreen(bird.getPosition());

    // Draw info box
    double boxWidth = 150;
    double boxHeight = 80;
    double boxX = Math.min(pos.x() + 20, canvas.getWidth() - boxWidth - 10);
    double boxY = Math.min(pos.y() + 20, canvas.getHeight() - boxHeight - 10);

    // Draw box background
    gc.setFill(darkTheme ? DarkTheme.INFO_BOX_BG : LightTheme.INFO_BOX_BG);
    gc.setStroke(darkTheme ? DarkTheme.BIRD_COLOR : LightTheme.BIRD_COLOR);
    gc.setLineWidth(1);
    gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 5, 5);
    gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 5, 5);

    // Draw info text
    gc.setFill(darkTheme ? DarkTheme.INFO_TEXT : LightTheme.INFO_TEXT);
    gc.setFont(javafx.scene.text.Font.font("Monospaced", 12));
    gc.fillText(String.format("Position: (%.0f, %.0f)", pos.x(), pos.y()), boxX + 10, boxY + 20);
    gc.fillText(
        String.format("Rotation: %.1f°", Math.toDegrees(bird.getRotation())), boxX + 10, boxY + 40);
    gc.fillText(String.format("Fitness: %.2f", bird.getSatiation()), boxX + 10, boxY + 60);
  }

  /** Draws a bird. */
  private void drawBird(GraphicsContext gc, Bird bird) {
    Vector2D screenPos = worldToScreen(bird.getPosition());
    double rotation = bird.getRotation();
    double screenSize = getScreenBirdSize();

    // Draw trail
    drawBirdTrail(gc, bird);

    // Draw eye sensors
    drawEyeSensors(gc, bird);

    // Save current transform
    gc.save();

    // Translate and rotate
    gc.translate(screenPos.x(), screenPos.y());
    // Convert from model space (0° up, CCW) to screen space (0° right, CW)
    gc.rotate(Math.toDegrees(rotation) - 90);

    // Draw bird body
    gc.setFill(darkTheme ? DarkTheme.BIRD_COLOR : LightTheme.BIRD_COLOR);
    gc.setStroke(darkTheme ? DarkTheme.BIRD_OUTLINE_COLOR : LightTheme.BIRD_OUTLINE_COLOR);
    gc.setLineWidth(1.5);

    // Main body (triangle)
    gc.beginPath();
    gc.moveTo(screenSize, 0);
    gc.lineTo(-screenSize, -screenSize / 2);
    gc.lineTo(-screenSize, screenSize / 2);
    gc.closePath();
    gc.fill();
    gc.stroke();

    // Draw wings
    gc.beginPath();
    gc.moveTo(-screenSize, -screenSize / 2);
    gc.lineTo(-screenSize * 1.5, -screenSize);
    gc.moveTo(-screenSize, screenSize / 2);
    gc.lineTo(-screenSize * 1.5, screenSize);
    gc.stroke();

    // Draw tail
    gc.beginPath();
    gc.moveTo(-screenSize, 0);
    gc.lineTo(-screenSize * 1.8, -screenSize / 4);
    gc.lineTo(-screenSize * 1.8, screenSize / 4);
    gc.closePath();
    gc.fill();
    gc.stroke();

    // Draw eye
    double screenEyeRadius = screenSize * 0.3;
    gc.setFill(darkTheme ? DarkTheme.EYE_COLOR : LightTheme.EYE_COLOR);
    gc.fillOval(0, -screenEyeRadius, screenEyeRadius * 2, screenEyeRadius * 2);

    // Draw field of view lines
    drawFieldOfView(gc, bird);

    // Restore transform
    gc.restore();
  }

  /** Draws the bird's field of view lines. */
  private void drawFieldOfView(GraphicsContext gc, Bird bird) {
    double fovStart = -state.getEyeFovAngle() / 2;
    double cellAngle = state.getEyeFovAngle() / state.getEyeCells();

    // Scale FOV range to screen coordinates
    double screenRange = getScreenFovRange();

    gc.setStroke(darkTheme ? DarkTheme.EYE_COLOR : LightTheme.EYE_COLOR);
    gc.setLineWidth(1);
    gc.setLineDashes(2);

    // Only draw lines for actual cell boundaries (cells, not cells+1)
    for (int i = 0; i <= state.getEyeCells(); i++) {
      // In model space: 0° is up, positive angles go CCW
      // In screen space: 0° is right, positive angles go CW
      // So we need to:
      // 1. Rotate 90° to align up (0°) with right
      // 2. Negate the angle to change direction (CCW -> CW)
      double angle = fovStart + cellAngle * i;
      double screenAngle = -angle;
      double x = FastMath.cos(screenAngle) * screenRange;
      double y = FastMath.sin(screenAngle) * screenRange;
      gc.strokeLine(0, 0, x, y);
    }

    gc.setLineDashes(null);
  }

  /**
   * Draws the bird's eye sensors, showing what each cell detects.
   *
   * @param bird bird to draw sensors for
   */
  private void drawEyeSensors(GraphicsContext gc, Bird bird) {
    Vector2D screenPos = worldToScreen(bird.getPosition());
    float rotation = bird.getRotation();
    float[] eyeInputs = bird.getVision();

    // Scale FOV range to screen coordinates
    double screenRange = getScreenFovRange();

    double fovStart = rotation - state.getEyeFovAngle() / 2;
    double cellAngle = state.getEyeFovAngle() / state.getEyeCells();

    for (int i = 0; i < state.getEyeCells(); i++) {
      double startAngle = fovStart + cellAngle * i;
      double endAngle = startAngle + cellAngle;
      float input = eyeInputs[i];

      // Choose color based on what the eye cell detects
      Color sensorColor;
      if (input == 0) {
        sensorColor = darkTheme ? DarkTheme.SENSOR_EMPTY_COLOR : LightTheme.SENSOR_EMPTY_COLOR;
      } else if (input > 0) {
        // Food detected - green with alpha based on distance
        // Clamp alpha to [0.0, 1.0]
        double alpha = Math.min(Math.max(input, 0.0), 1.0);
        sensorColor = Color.rgb(0, 255, 0, alpha);
      } else {
        // Wall detected - red with alpha based on distance
        // Clamp alpha to [0.0, 1.0]
        double alpha = Math.min(Math.max(-input, 0.0), 1.0);
        sensorColor = Color.rgb(255, 0, 0, alpha);
      }

      // Draw sensor arc
      gc.setFill(sensorColor);
      gc.beginPath();
      gc.moveTo(screenPos.x(), screenPos.y());

      // Convert from model space (0° up, CCW) to screen space (0° right, CW)
      double startScreenAngle = 90 - Math.toDegrees(startAngle);
      double sweepAngle = -Math.toDegrees(cellAngle); // Negative because we're going CW

      gc.arc(screenPos.x(), screenPos.y(), screenRange, screenRange, startScreenAngle, sweepAngle);
      gc.lineTo(screenPos.x(), screenPos.y());
      gc.closePath();
      gc.fill();
    }
  }

  /**
   * Draws the bird's movement trail.
   *
   * @param bird bird to draw trail for
   */
  private void drawBirdTrail(GraphicsContext gc, Bird bird) {
    Vector2D screenPos = worldToScreen(bird.getPosition());
    Vector2D screenPrevPos = worldToScreen(bird.getPreviousPosition());

    gc.setStroke(darkTheme ? DarkTheme.TRAIL_COLOR : LightTheme.TRAIL_COLOR);
    gc.setLineWidth(getScreenBirdSize() * 0.8);
    gc.strokeLine(screenPrevPos.x(), screenPrevPos.y(), screenPos.x(), screenPos.y());
  }

  /** Converts model space coordinates (0.0 to 1.0) to screen coordinates. */
  private Vector2D worldToScreen(Vector2D modelPos) {
    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());
    return new Vector2D(
        (float) (modelPos.x() * worldScale), (float) ((1 - modelPos.y()) * worldScale));
  }

  /** Converts screen coordinates to model space coordinates (0.0 to 1.0). */
  private Vector2D screenToWorld(Vector2D screenPos) {
    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());
    return new Vector2D(
        (float) (screenPos.x() / worldScale), (float) (1 - screenPos.y() / worldScale));
  }

  /** Gets the bird size in screen coordinates. */
  private double getScreenBirdSize() {
    // Scale bird size relative to smallest screen dimension to maintain proportions
    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());
    return state.getBirdSize() * worldScale;
  }

  /** Gets the food size in screen coordinates. */
  private double getScreenFoodSize() {
    // Scale food size relative to smallest screen dimension to maintain proportions
    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());
    return state.getFoodSize() * worldScale;
  }

  /** Gets the field of view range in screen coordinates. */
  private double getScreenFovRange() {
    // Scale fov range relative to smallest screen dimension to maintain proportions
    double worldScale = Math.min(canvas.getWidth(), canvas.getHeight());
    return state.getEyeFovRange() * worldScale;
  }
}
