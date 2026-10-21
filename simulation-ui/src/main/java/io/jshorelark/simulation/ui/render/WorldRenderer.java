/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.render;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.food.Food;
import io.jshorelark.simulation.physics.Vector2D;
import io.jshorelark.simulation.ui.evolution.EvolutionManager;
import java.util.Objects;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Paints the world: grid, food and birds.
 *
 * <p>Every method is a function of the state passed in, the theme and the transform; the renderer
 * holds no mutable state of its own. That makes the paint path independent of the hover logic, the
 * canvas lifecycle and the JavaFX timers, and leaves {@link
 * io.jshorelark.simulation.ui.SimulationView} to own only those.
 *
 * <p>Model space is the unit square with Y up; {@link SceneTransform} performs the flip, so nothing
 * in this class special-cases the vertical axis except the rotation conversion, which is a genuine
 * difference between the two coordinate systems rather than a mapping detail.
 */
public final class WorldRenderer {

  /** Distance in pixels between grid lines. */
  private static final double GRID_SIZE = 50.0d;

  /** Line width of the bird outline. */
  private static final double BIRD_OUTLINE_WIDTH = 1.5d;

  /** Fraction of the bird size used as the eye radius. */
  private static final double EYE_RADIUS_RATIO = 0.3d;

  /** Fraction of the bird size used as the trail width. */
  private static final double TRAIL_WIDTH_RATIO = 0.8d;

  /** Wing length relative to the bird size. */
  private static final double WING_LENGTH_RATIO = 1.5d;

  /** Tail length relative to the bird size. */
  private static final double TAIL_LENGTH_RATIO = 1.8d;

  /**
   * Draws the whole world.
   *
   * @param gc the canvas to paint on
   * @param state the simulation state to read
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @throws NullPointerException if any argument is {@code null}
   */
  public void draw(
      GraphicsContext gc, EvolutionManager state, RenderTheme theme, SceneTransform transform) {
    Objects.requireNonNull(gc, "gc");
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(theme, "theme");
    Objects.requireNonNull(transform, "transform");

    gc.setImageSmoothing(true);
    if (!transform.isUsable()) {
      // Nothing sensible can be drawn before the canvas has a size; leaving the previous frame
      // cleared is better than mapping every point to the origin.
      return;
    }

    drawGrid(gc, theme, transform);
    for (Food food : state.getFoods()) {
      drawFood(gc, food, theme, transform, state.getFoodSize());
    }
    for (Bird bird : state.getBirds()) {
      drawBird(gc, bird, theme, transform, state);
    }
  }

  /**
   * Draws the background grid.
   *
   * @param gc the canvas to paint on
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   */
  private void drawGrid(GraphicsContext gc, RenderTheme theme, SceneTransform transform) {
    gc.setStroke(theme.grid());
    gc.setLineWidth(1.0d);

    double extent = transform.scale();
    // Count the lines once and step by index: an accumulating `x += size` loop over a non-finite
    // bound would never terminate.
    int lines = (int) Math.ceil(extent / GRID_SIZE);
    for (int i = 0; i <= lines; i++) {
      double offset = i * GRID_SIZE;
      gc.strokeLine(offset, 0.0d, offset, extent);
      gc.strokeLine(0.0d, offset, extent, offset);
    }
  }

  /**
   * Draws one food item.
   *
   * @param gc the canvas to paint on
   * @param food the item to draw
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @param worldSize the collision radius in model space, reused as the drawn size
   */
  private void drawFood(
      GraphicsContext gc,
      Food food,
      RenderTheme theme,
      SceneTransform transform,
      double worldSize) {
    Vector2D screen = transform.toScreen(food.position());
    double size = transform.toScreenLength(worldSize);
    gc.setFill(theme.food());
    gc.fillOval(screen.x() - size / 2.0d, screen.y() - size / 2.0d, size, size);
  }

  /**
   * Draws one bird: trail, sensors, body and field of view.
   *
   * @param gc the canvas to paint on
   * @param bird the bird to draw
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @param state the simulation state, read for the eye geometry and sizes
   */
  private void drawBird(
      GraphicsContext gc,
      Bird bird,
      RenderTheme theme,
      SceneTransform transform,
      EvolutionManager state) {
    double size = transform.toScreenLength(state.getBirdSize());

    // Trail and sensors are drawn in screen coordinates, before the local transform is applied.
    drawTrail(gc, bird, theme, transform, size);
    drawEyeSensors(gc, bird, theme, transform, state);

    Vector2D screen = transform.toScreen(bird.getPosition());
    gc.save();
    gc.translate(screen.x(), screen.y());
    // Model space has 0 degrees pointing up and angles growing counter-clockwise; the canvas has
    // 0 degrees pointing right and growing clockwise, so the angle is negated implicitly by
    // subtracting 90 rather than 270.
    gc.rotate(Math.toDegrees(bird.getRotation()) - 90.0d);

    drawBody(gc, size, theme);
    drawFieldOfView(gc, theme, transform, state);

    gc.restore();
  }

  /**
   * Draws the body, wings, tail and eye in the bird's local frame.
   *
   * @param gc the canvas to paint on
   * @param size the bird size in pixels
   * @param theme the active palette
   */
  private void drawBody(GraphicsContext gc, double size, RenderTheme theme) {
    gc.setFill(theme.bird());
    gc.setStroke(theme.birdOutline());
    gc.setLineWidth(BIRD_OUTLINE_WIDTH);

    // Main body (triangle)
    gc.beginPath();
    gc.moveTo(size, 0.0d);
    gc.lineTo(-size, -size / 2.0d);
    gc.lineTo(-size, size / 2.0d);
    gc.closePath();
    gc.fill();
    gc.stroke();

    // Wings
    gc.beginPath();
    gc.moveTo(-size, -size / 2.0d);
    gc.lineTo(-size * WING_LENGTH_RATIO, -size);
    gc.moveTo(-size, size / 2.0d);
    gc.lineTo(-size * WING_LENGTH_RATIO, size);
    gc.stroke();

    // Tail
    gc.beginPath();
    gc.moveTo(-size, 0.0d);
    gc.lineTo(-size * TAIL_LENGTH_RATIO, -size / 4.0d);
    gc.lineTo(-size * TAIL_LENGTH_RATIO, size / 4.0d);
    gc.closePath();
    gc.fill();
    gc.stroke();

    // Eye
    double eyeRadius = size * EYE_RADIUS_RATIO;
    gc.setFill(theme.eye());
    gc.fillOval(0.0d, -eyeRadius, eyeRadius * 2.0d, eyeRadius * 2.0d);
  }

  /**
   * Draws the bird's movement trail.
   *
   * @param gc the canvas to paint on
   * @param bird the bird to draw for
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @param size the bird size in pixels
   */
  private void drawTrail(
      GraphicsContext gc, Bird bird, RenderTheme theme, SceneTransform transform, double size) {
    Vector2D current = transform.toScreen(bird.getPosition());
    Vector2D previous = transform.toScreen(bird.getPreviousPosition());

    gc.setStroke(theme.trail());
    gc.setLineWidth(size * TRAIL_WIDTH_RATIO);
    gc.strokeLine(previous.x(), previous.y(), current.x(), current.y());
  }

  /**
   * Draws the field-of-view lines in the bird's local frame.
   *
   * @param gc the canvas to paint on
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @param state the simulation state, read for the eye geometry
   */
  private void drawFieldOfView(
      GraphicsContext gc, RenderTheme theme, SceneTransform transform, EvolutionManager state) {
    gc.setStroke(theme.eye());
    gc.setLineWidth(1.0d);
    gc.setLineDashes(2.0d);

    double fovStart = -state.getEyeFovAngle() / 2.0d;
    double cellAngle = state.getEyeFovAngle() / state.getEyeCells();
    double range = transform.toScreenLength(state.getEyeFovRange());

    // Only draw lines for actual cell boundaries (cells, not cells + 1).
    for (int i = 0; i <= state.getEyeCells(); i++) {
      // Inside the translated and rotated frame, the local X axis points along the heading. The
      // fan is symmetric around it, so the local angle is the offset from the centre negated to
      // turn counter-clockwise model angles into clockwise canvas angles.
      double screenAngle = -(fovStart + cellAngle * i);
      double x = Math.cos(screenAngle) * range;
      double y = Math.sin(screenAngle) * range;
      gc.strokeLine(0.0d, 0.0d, x, y);
    }

    gc.setLineDashes(null);
  }

  /**
   * Draws the eye sensors as arcs, showing what each cell currently detects.
   *
   * @param gc the canvas to paint on
   * @param bird the bird whose vision is drawn
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @param state the simulation state, read for the eye geometry
   */
  private void drawEyeSensors(
      GraphicsContext gc,
      Bird bird,
      RenderTheme theme,
      SceneTransform transform,
      EvolutionManager state) {
    Vector2D screen = transform.toScreen(bird.getPosition());
    float[] vision = bird.getVision();
    double range = transform.toScreenLength(state.getEyeFovRange());
    double fovStart = bird.getRotation() - state.getEyeFovAngle() / 2.0d;
    double cellAngle = state.getEyeFovAngle() / state.getEyeCells();
    double sweep = -Math.toDegrees(cellAngle);

    for (int i = 0; i < state.getEyeCells(); i++) {
      gc.setFill(sensorColor(vision[i], theme));
      gc.beginPath();
      gc.moveTo(screen.x(), screen.y());
      // Canvas arcs start at 3 o'clock and sweep clockwise, hence the 90-degree offset and the
      // negative sweep.
      double start = 90.0d - Math.toDegrees(fovStart + cellAngle * i);
      gc.arc(screen.x(), screen.y(), range, range, start, sweep);
      gc.lineTo(screen.x(), screen.y());
      gc.closePath();
      gc.fill();
    }
  }

  /**
   * Maps one sensor reading to a colour.
   *
   * <p>Positive strength means food and negative means an obstacle; alpha tracks the magnitude so a
   * distant item is drawn fainter, and zero uses the theme's empty colour.
   *
   * @param input the cell reading
   * @param theme the active palette
   * @return the fill colour for the cell
   */
  private Color sensorColor(float input, RenderTheme theme) {
    if (input == 0.0f) {
      return theme.sensorEmpty();
    }
    return input > 0.0f
        ? Color.rgb(0, 255, 0, Math.clamp(input, 0.0f, 1.0f))
        : Color.rgb(255, 0, 0, Math.clamp(-input, 0.0f, 1.0f));
  }
}
