/*
 * Copyright (c) 2025 JShorelark Contributors
 *
 * Licensed under the same terms as the original Shorelark project.
 * See: https://github.com/patryk27/shorelark
 */
package io.jshorelark.simulation.ui;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Chart that displays fitness statistics over time.
 *
 * @author Jose
 * @version $Id: $Id
 */
public class FitnessChart extends LineChart<Number, Number> {

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
    super(new NumberAxis("Generation", 0, 100, 10), new NumberAxis("Fitness", 0, 1, 0.1));

    // Configure axes
    getXAxis().setAnimated(false);
    getYAxis().setAnimated(false);
    setAnimated(false);

    // Create series
    maxSeries = new XYChart.Series<>();
    maxSeries.setName("Max");
    getData().add(maxSeries);

    avgSeries = new XYChart.Series<>();
    avgSeries.setName("Avg");
    getData().add(avgSeries);

    medianSeries = new XYChart.Series<>();
    medianSeries.setName("Median");
    getData().add(medianSeries);

    minSeries = new XYChart.Series<>();
    minSeries.setName("Min");
    getData().add(minSeries);

    // Configure chart
    setCreateSymbols(false);
    setLegendVisible(true);
  }

  /**
   * Updates the chart with new statistics.
   *
   * @param generation the current generation
   * @param maxFitness the maximum fitness
   * @param avgFitness the average fitness
   * @param medianFitness the median fitness
   * @param minFitness the minimum fitness
   */
  public void update(
      int generation, float maxFitness, float avgFitness, float medianFitness, float minFitness) {
    maxSeries.getData().add(new XYChart.Data<>(generation, maxFitness));
    avgSeries.getData().add(new XYChart.Data<>(generation, avgFitness));
    medianSeries.getData().add(new XYChart.Data<>(generation, medianFitness));
    minSeries.getData().add(new XYChart.Data<>(generation, minFitness));

    // Update x-axis range
    NumberAxis xAxis = (NumberAxis) getXAxis();
    if (generation > xAxis.getUpperBound()) {
      xAxis.setUpperBound(generation + 10);
    }

    // Update y-axis range
    NumberAxis yAxis = (NumberAxis) getYAxis();
    if (maxFitness > yAxis.getUpperBound()) {
      yAxis.setUpperBound(maxFitness * 1.1);
    }
  }

  /** Clears all data from the chart. */
  public void clear() {
    maxSeries.getData().clear();
    avgSeries.getData().clear();
    medianSeries.getData().clear();
    minSeries.getData().clear();

    // Reset axes
    NumberAxis xAxis = (NumberAxis) getXAxis();
    xAxis.setUpperBound(100);
    NumberAxis yAxis = (NumberAxis) getYAxis();
    yAxis.setUpperBound(1);
  }
}
