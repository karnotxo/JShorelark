/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://codeberg.org/pwy/shorelark
 */
package io.jshorelark.simulation.ui;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Chart that displays fitness statistics over time.
 *
 * <p>The axes are created here and kept in fields instead of being read back from {@code
 * getXAxis()}/{@code getYAxis()}: touching overridable accessors inside a constructor is unsafe for
 * subclasses, so the chart owns the two axes it builds.
 */
public final class FitnessChart extends LineChart<Number, Number> {

  private static final int INITIAL_GENERATIONS = 100;
  private static final double INITIAL_FITNESS = 1.0;

  private final NumberAxis horizontalAxis;
  private final NumberAxis verticalAxis;

  /** The series for maximum fitness. */
  private final XYChart.Series<Number, Number> maxSeries;

  /** The series for average fitness. */
  private final XYChart.Series<Number, Number> avgSeries;

  /** The series for median fitness. */
  private final XYChart.Series<Number, Number> medianSeries;

  /** The series for minimum fitness. */
  private final XYChart.Series<Number, Number> minSeries;

  /** Creates a new fitness chart. */
  public FitnessChart() {
    super(
        new NumberAxis(Messages.text(Messages.CHART_GENERATION), 0, INITIAL_GENERATIONS, 10),
        new NumberAxis(Messages.text(Messages.CHART_FITNESS), 0, INITIAL_FITNESS, 0.1));

    horizontalAxis = (NumberAxis) getXAxis();
    verticalAxis = (NumberAxis) getYAxis();
    horizontalAxis.setAnimated(false);
    verticalAxis.setAnimated(false);
    setAnimated(false);

    maxSeries = namedSeries(Messages.text(Messages.CHART_MAX));
    avgSeries = namedSeries(Messages.text(Messages.CHART_AVG));
    medianSeries = namedSeries(Messages.text(Messages.CHART_MEDIAN));
    minSeries = namedSeries(Messages.text(Messages.CHART_MIN));

    setCreateSymbols(false);
    setLegendVisible(true);
  }

  private XYChart.Series<Number, Number> namedSeries(String name) {
    var series = new XYChart.Series<Number, Number>();
    series.setName(name);
    getData().add(series);
    return series;
  }

  /**
   * Appends one generation to every series.
   *
   * @param generation the generation number
   * @param maxFitness the highest fitness in the generation
   * @param avgFitness the mean fitness
   * @param medianFitness the median fitness
   * @param minFitness the lowest fitness
   */
  public void update(
      int generation, float maxFitness, float avgFitness, float medianFitness, float minFitness) {
    maxSeries.getData().add(new XYChart.Data<>(generation, maxFitness));
    avgSeries.getData().add(new XYChart.Data<>(generation, avgFitness));
    medianSeries.getData().add(new XYChart.Data<>(generation, medianFitness));
    minSeries.getData().add(new XYChart.Data<>(generation, minFitness));

    // Grow the horizontal axis only when a point would fall outside it.
    if (generation > horizontalAxis.getUpperBound()) {
      horizontalAxis.setUpperBound(generation + 10);
    }
    if (maxFitness > verticalAxis.getUpperBound()) {
      verticalAxis.setUpperBound(maxFitness * 1.1);
    }
  }

  /** Clears all data and restores the initial axis bounds. */
  public void clear() {
    maxSeries.getData().clear();
    avgSeries.getData().clear();
    medianSeries.getData().clear();
    minSeries.getData().clear();

    horizontalAxis.setUpperBound(INITIAL_GENERATIONS);
    verticalAxis.setUpperBound(INITIAL_FITNESS);
  }
}
