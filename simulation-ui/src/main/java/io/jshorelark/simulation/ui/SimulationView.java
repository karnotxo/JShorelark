/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.physics.Vector2D;
import io.jshorelark.simulation.ui.evolution.EvolutionManager;
import io.jshorelark.simulation.ui.render.BirdInspectorRenderer;
import io.jshorelark.simulation.ui.render.RenderTheme;
import io.jshorelark.simulation.ui.render.SceneTransform;
import io.jshorelark.simulation.ui.render.WorldRenderer;
import java.util.Objects;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * Canvas that displays the simulation state.
 *
 * <p>This type owns the canvas lifecycle, the hover state and the decision to repaint. What to
 * paint lives in {@link WorldRenderer} and {@link BirdInspectorRenderer}, and how model space maps
 * to pixels lives in {@link SceneTransform}; the view itself never touches a coordinate.
 *
 * <p>Hit-testing stays here because it is input handling, not drawing: it needs the pointer and the
 * population, not the canvas contents.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public final class SimulationView extends Pane {

  /** The simulation state to render; all values are read through it, never recomputed here. */
  private final EvolutionManager state;

  /** Paints grid, food and birds. */
  private final WorldRenderer worldRenderer = new WorldRenderer();

  /** Paints the details box for the hovered bird. */
  private final BirdInspectorRenderer inspectorRenderer = new BirdInspectorRenderer();

  /** Notified when the hovered bird changes; may be {@code null}. */
  private Runnable onHoveredBirdChanged;

  /** The canvas for drawing. */
  private final Canvas canvas;

  /** Mouse position in screen coordinates. */
  private Vector2D screenMousePos = Vector2D.ZERO;

  /** Currently hovered bird, or {@code null} when the pointer is not over one. */
  private Bird hoveredBird;

  /** The active palette. */
  private RenderTheme theme = RenderTheme.DARK;

  /**
   * Creates a new simulation view.
   *
   * <p>The constructor only wires listeners; the canvas is attached by {@link #initializeView()},
   * which the app calls after the view is part of the scene graph. Deferring keeps the constructor
   * free of overridable {@code Pane} calls ({@code getChildren()}, {@code requestLayout()}), which
   * would otherwise run before a subclass could finish initialising.
   *
   * @param manager the evolution manager this view renders
   * @throws NullPointerException if {@code manager} is {@code null}
   */
  public SimulationView(EvolutionManager manager) {
    this.state = Objects.requireNonNull(manager, "manager");
    this.canvas = new Canvas();
    log.debug("Created simulation view");
  }

  /**
   * Selects the palette.
   *
   * @param dark {@code true} for the dark theme
   */
  public void setTheme(boolean dark) {
    this.theme = dark ? RenderTheme.DARK : RenderTheme.LIGHT;
    draw();
  }

  /** Attaches the canvas and starts tracking size and mouse input. */
  public void initializeView() {
    getChildren().add(canvas);

    // Bind canvas size to parent size
    canvas.widthProperty().bind(widthProperty());
    canvas.heightProperty().bind(heightProperty());

    // Handle parent resizing
    widthProperty().addListener((obs, oldVal, newVal) -> requestLayout());
    heightProperty().addListener((obs, oldVal, newVal) -> requestLayout());

    setupMouseHandlers();
  }

  /** Sets up mouse event handlers. */
  private void setupMouseHandlers() {
    canvas.setOnMouseMoved(this::handleMouseMoved);
    canvas.setOnMouseExited(
        e -> {
          screenMousePos = Vector2D.ZERO;
          if (hoveredBird != null) {
            hoveredBird = null;
            notifyHoveredBirdChanged();
            draw();
          }
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
   * @param callback the callback to run, or {@code null} to clear it
   */
  public void setOnHoveredBirdChanged(Runnable callback) {
    this.onHoveredBirdChanged = callback;
  }

  /**
   * Handles mouse movement events.
   *
   * @param event mouse event
   */
  private void handleMouseMoved(MouseEvent event) {
    screenMousePos = new Vector2D((float) event.getX(), (float) event.getY());
    updateHoveredBird();
  }

  /** Updates the currently hovered bird based on mouse position. */
  private void updateHoveredBird() {
    SceneTransform transform = transform();
    Vector2D worldMousePos = transform.toWorld(screenMousePos);
    double worldBirdSize = state.getBirdSize();

    Bird newHoveredBird = null;
    double closestDistance = Double.MAX_VALUE;
    for (Bird bird : state.getBirds()) {
      double distance = worldMousePos.subtract(bird.getPosition()).length();
      // Twice the bird size, so the target is usable on a small window without stealing clicks
      // from a neighbour: the loop keeps the nearest match.
      if (distance < worldBirdSize * 2.0 && distance < closestDistance) {
        newHoveredBird = bird;
        closestDistance = distance;
      }
    }

    // Bird does not override equals, so this is an identity check spelled without operator !=.
    if (!Objects.equals(hoveredBird, newHoveredBird)) {
      hoveredBird = newHoveredBird;
      notifyHoveredBirdChanged();
      draw();
    }
  }

  /** Invokes the hover callback when one is registered. */
  private void notifyHoveredBirdChanged() {
    if (onHoveredBirdChanged != null) {
      onHoveredBirdChanged.run();
    }
  }

  /** Updates the view by redrawing. */
  public void update() {
    draw();
  }

  /** Draws the simulation state. */
  private void draw() {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

    SceneTransform transform = transform();
    worldRenderer.draw(gc, state, theme, transform);
    if (hoveredBird != null) {
      inspectorRenderer.draw(gc, hoveredBird, theme, transform);
    }
  }

  /**
   * Returns the model-to-screen mapping for the canvas's current size.
   *
   * @return the transform for this frame
   */
  private SceneTransform transform() {
    return new SceneTransform(canvas.getWidth(), canvas.getHeight());
  }
}
