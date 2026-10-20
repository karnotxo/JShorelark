/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui.dialogs;

import io.jshorelark.simulation.ui.controls.ControlBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

/**
 * About dialog showing version information and credits.
 *
 * <p>The animated background lives in {@link AboutBackground}; this class owns the window, the text
 * and the close button.
 *
 * @author Jose
 * @version $Id: $Id
 */
@Slf4j
public final class AboutDialog {

  /** Version number. */
  private static final String VERSION = "1.0.0";

  /** Original Rust project URL. */
  private static final String ORIGINAL_PROJECT_URL = "https://github.com/patryk27/shorelark";

  /** Blog post URL. */
  private static final String BLOG_URL = "https://pwy.io/posts/learning-to-fly-pt1/";

  /** Dialog width, in pixels. */
  private static final double WIDTH = 500.0;

  /** Dialog height, in pixels. */
  private static final double HEIGHT = 400.0;

  /**
   * Shows the about dialog.
   *
   * @param owner the window the dialog is modal to
   */
  public static void show(Stage owner) {
    var dialog = new Stage();
    dialog.initOwner(owner);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.TRANSPARENT);
    dialog.setTitle("JShorelark");
    dialog.setResizable(false);
    dialog.getIcons().addAll(owner.getIcons());

    var closeButton = ControlBuilder.button().text("×").build();
    closeButton.getStyleClass().add("about-close-button");
    closeButton.setOnAction(e -> dialog.close());

    var root = new StackPane(new AboutBackground().create(), createContent(), closeButton);
    root.getStyleClass().add("about-dialog");
    StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
    StackPane.setMargin(closeButton, new Insets(10));
    makeDraggable(root, dialog);

    var scene = new Scene(root, WIDTH, HEIGHT);
    scene.setFill(null);
    if (owner.getScene() != null) {
      scene.getStylesheets().addAll(owner.getScene().getStylesheets());
    }

    dialog.setScene(scene);
    dialog.show();
  }

  private static VBox createContent() {
    var title = new Text("JShorelark");
    title.getStyleClass().add("about-title");

    var version = new Text("Version " + VERSION);
    version.getStyleClass().add("about-version");

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

    var content = new VBox(20, title, version, description);
    content.setAlignment(Pos.CENTER);
    content.setPadding(new Insets(30));
    content.getStyleClass().add("about-content");
    return content;
  }

  /** Makes the undecorated window follow the mouse while the user drags it. */
  private static void makeDraggable(StackPane root, Stage dialog) {
    double[] offset = new double[2];
    root.setOnMousePressed(
        e -> {
          offset[0] = dialog.getX() - e.getScreenX();
          offset[1] = dialog.getY() - e.getScreenY();
        });
    root.setOnMouseDragged(
        e -> {
          dialog.setX(e.getScreenX() + offset[0]);
          dialog.setY(e.getScreenY() + offset[1]);
        });
  }

  /** Creates a styled text node that respects the current theme. */
  private static Text createStyledText(String content) {
    var text = new Text(content);
    text.getStyleClass().addAll("about-description", "text");
    return text;
  }

  /**
   * Creates a hyperlink that opens a URL in the system browser.
   *
   * <p>Opening a browser can legitimately fail (headless session, no registered handler), so the
   * failure is logged instead of silently swallowed.
   *
   * @param text the link label
   * @param url the address to open
   * @return the styled hyperlink
   */
  private static Hyperlink createLink(String text, String url) {
    var link = new Hyperlink(text);
    link.getStyleClass().addAll("about-description", "hyperlink");
    link.setOnAction(e -> openInBrowser(url));
    return link;
  }

  private static void openInBrowser(String url) {
    // HeadlessException extends UnsupportedOperationException, so the latter covers both.
    try {
      java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
    } catch (java.io.IOException
        | UnsupportedOperationException
        | IllegalArgumentException
        | SecurityException e) {
      log.warn("Could not open {} in the browser", url, e);
    }
  }
}
