/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.render;

import io.jshorelark.simulation.bird.Bird;
import io.jshorelark.simulation.physics.Vector2D;
import io.jshorelark.simulation.ui.Messages;
import java.util.Locale;
import java.util.Objects;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Draws the details box for the bird under the mouse.
 *
 * <p>Kept apart from {@link WorldRenderer} because it is a tooltip, not part of the world: it
 * follows the pointer, it has its own layout rules, and it must survive being asked for a bird that
 * is outside the frame. Separating it also keeps the world's paint path free of text layout, which
 * is the slowest thing on the canvas.
 */
public final class BirdInspectorRenderer {

  /** Width of the details box, in pixels. */
  private static final double BOX_WIDTH = 150.0d;

  /** Height of the details box, in pixels. */
  private static final double BOX_HEIGHT = 80.0d;

  /** Gap between the bird and the box, in pixels. */
  private static final double BOX_OFFSET = 20.0d;

  /** Gap between the box and the canvas edge, in pixels. */
  private static final double EDGE_MARGIN = 10.0d;

  /** Gap between the box border and its text, in pixels. */
  private static final double TEXT_INSET = 10.0d;

  /** Distance between two text baselines, in pixels. */
  private static final double LINE_HEIGHT = 20.0d;

  /** Corner radius of the box, in pixels. */
  private static final double CORNER_RADIUS = 5.0d;

  /** Font size of the details text. */
  private static final double FONT_SIZE = 12.0d;

  /**
   * Draws the details box for a bird.
   *
   * @param gc the canvas to paint on
   * @param bird the bird to describe
   * @param theme the active palette
   * @param transform the model-to-screen mapping
   * @throws NullPointerException if any argument is {@code null}
   */
  public void draw(GraphicsContext gc, Bird bird, RenderTheme theme, SceneTransform transform) {
    Objects.requireNonNull(gc, "gc");
    Objects.requireNonNull(bird, "bird");
    Objects.requireNonNull(theme, "theme");
    Objects.requireNonNull(transform, "transform");

    if (!transform.isUsable()) {
      return;
    }

    Vector2D pos = transform.toScreen(bird.getPosition());
    // Clamp inside the frame so a bird at the edge does not push its box off-canvas.
    double x = Math.min(pos.x() + BOX_OFFSET, transform.width() - BOX_WIDTH - EDGE_MARGIN);
    double y = Math.min(pos.y() + BOX_OFFSET, transform.height() - BOX_HEIGHT - EDGE_MARGIN);

    gc.setFill(theme.infoBoxBackground());
    gc.setStroke(theme.bird());
    gc.setLineWidth(1.0d);
    gc.fillRoundRect(x, y, BOX_WIDTH, BOX_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);
    gc.strokeRoundRect(x, y, BOX_WIDTH, BOX_HEIGHT, CORNER_RADIUS, CORNER_RADIUS);

    gc.setFill(theme.infoText());
    gc.setFont(Font.font("Monospaced", FONT_SIZE));
    double textX = x + TEXT_INSET;
    gc.fillText(positionLine(pos), textX, y + LINE_HEIGHT);
    gc.fillText(rotationLine(bird), textX, y + LINE_HEIGHT * 2.0d);
    gc.fillText(fitnessLine(bird), textX, y + LINE_HEIGHT * 3.0d);
  }

  private static String positionLine(Vector2D pos) {
    return Messages.formatted(
        Messages.INSPECTOR_POSITION, format("%.0f", pos.x()), format("%.0f", pos.y()));
  }

  private static String rotationLine(Bird bird) {
    return Messages.formatted(
        Messages.INSPECTOR_ROTATION, format("%.1f°", Math.toDegrees(bird.getRotation())));
  }

  private static String fitnessLine(Bird bird) {
    return Messages.formatted(Messages.INSPECTOR_FITNESS, format("%.2f", bird.getSatiation()));
  }

  /**
   * Formats a number for the inspector.
   *
   * <p>The pattern stays here because it is presentation, not prose: the sentence around the value
   * and the precision it needs are separate decisions, and a translation supplies only the former.
   *
   * @param pattern a {@code String.format} pattern
   * @param value the number to format
   * @return the formatted number
   */
  private static String format(String pattern, double value) {
    return String.format(Locale.ROOT, pattern, value);
  }
}
