/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui.controls;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Builder for JavaFX controls with fluent interface.
 *
 * @author Jose
 * @version $Id: $Id
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ControlBuilder {

  /**
   * Creates a new button builder.
   *
   * @return a {@link io.jshorelark.simulation.ui.controls.ControlBuilder.ButtonBuilder} object
   */
  public static ButtonBuilder button() {
    return new ButtonBuilder();
  }

  /**
   * Creates a new slider builder.
   *
   * @return a {@link io.jshorelark.simulation.ui.controls.ControlBuilder.SliderBuilder} object
   */
  public static SliderBuilder slider() {
    return new SliderBuilder();
  }

  /** Builder for JavaFX buttons. */
  public static class ButtonBuilder {
    private String text;
    private String tooltip;
    private boolean primary;
    private EventHandler<ActionEvent> onAction;

    /** Sets the button text. */
    public ButtonBuilder text(String text) {
      this.text = text;
      return this;
    }

    /** Makes the button a primary button. */
    public ButtonBuilder primary() {
      primary = true;
      return this;
    }

    /** Sets the button tooltip. */
    public ButtonBuilder tooltip(String text) {
      this.tooltip = text;
      return this;
    }

    /** Sets the button action. */
    public ButtonBuilder onAction(EventHandler<ActionEvent> handler) {
      this.onAction = handler;
      return this;
    }

    /** Builds the button. */
    public Button build() {
      Button button = new Button();
      button.setText(text);
      if (primary) {
        button.getStyleClass().add("primary");
      }
      button.setTooltip(new Tooltip(tooltip));
      button.setOnAction(onAction);
      return button;
    }
  }

  /** Builder for JavaFX sliders. */
  public static class SliderBuilder {
    private double minValue;
    private double maxValue;
    private double value;
    private String tooltip;
    private ChangeListener<Number> valueChangeListener;
    private boolean showTickLabels;
    private boolean showTickMarks;
    private double blockIncrement;

    /** Sets the minimum value. */
    public SliderBuilder min(double value) {
      this.minValue = value;
      return this;
    }

    /** Sets the maximum value. */
    public SliderBuilder max(double value) {
      this.maxValue = value;
      return this;
    }

    /** Sets the current value. */
    public SliderBuilder value(double value) {
      this.value = value;
      return this;
    }

    /** Sets the tooltip. */
    public SliderBuilder tooltip(String text) {
      this.tooltip = text;
      return this;
    }

    /** Sets the value change listener. */
    public SliderBuilder onValueChanged(ChangeListener<Number> listener) {
      this.valueChangeListener = listener;
      return this;
    }

    /** Shows tick labels. */
    public SliderBuilder showTickLabels() {
      this.showTickLabels = true;
      return this;
    }

    /** Shows tick marks. */
    public SliderBuilder showTickMarks() {
      this.showTickMarks = true;
      return this;
    }

    /** Sets the block increment. */
    public SliderBuilder blockIncrement(double value) {
      this.blockIncrement = value;
      return this;
    }

    /** Builds the slider. */
    public Slider build() {
      Slider slider = new Slider();
      slider.setMin(minValue);
      slider.setMax(maxValue);
      slider.setValue(value);
      if (tooltip != null) {
        slider.setTooltip(new Tooltip(tooltip));
      }
      if (valueChangeListener != null) {
        slider.valueProperty().addListener(valueChangeListener);
      }
      slider.setShowTickLabels(showTickLabels);
      slider.setShowTickMarks(showTickMarks);
      slider.setBlockIncrement(blockIncrement);
      return slider;
    }
  }
}
